package com.fitapp.appfit.feature.workout.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.feature.workout.model.request.SaveWorkoutSessionRequest
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.enums.SyncStatus
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Repository para gestión de workouts con soporte offline-first.
 *
 * Estrategia:
 * 1. Guardar siempre localmente primero (Room)
 * 2. Intentar sync con backend si hay conexión
 * 3. Si falla, marcar como PENDING_CREATE para sync posterior
 * 4. WorkoutSyncManager se encarga de sincronizar pendientes
 */
class WorkoutRepository(private val context: Context) {

    private val service = WorkoutService.instance
    private val db by lazy { AppDatabase.getInstance(context) }
    private val sessionDao by lazy { db.workoutSessionDao() }
    private val resultDao by lazy { db.workoutSetResultDao() }

    companion object {
        private const val TAG = "WorkoutRepository"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    // ── Guardar sesión (offline-first) ────────────────────────────────────────

    /**
     * Guarda una sesión de workout.
     *
     * Flujo:
     * 1. Guardar en Room (siempre)
     * 2. Intentar enviar al backend
     * 3. Si tiene éxito → marcar como SYNCED
     * 4. Si falla → dejar como PENDING_CREATE para sync posterior
     *
     * @return sessionId local (Room) siempre, independientemente del sync
     */
    suspend fun saveWorkoutSession(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        startedAt: Long = System.currentTimeMillis(),
        finishedAt: Long = System.currentTimeMillis(),
        performanceScore: Int? = null
    ): Result<Long> {

        Log.i(TAG, "SAVE_WORKOUT_START | routineId=$routineId | setCount=${setParamState.size}")

        val session = WorkoutSessionEntity(
            routineId = routineId,
            userId = userId,
            startedAt = startedAt,
            finishedAt = finishedAt,
            syncStatus = SyncStatus.PENDING_CREATE,
            lastModifiedLocally = System.currentTimeMillis()
        )

        val sessionId = try {
            sessionDao.insertSession(session)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_SAVING_SESSION_LOCALLY | error=${e.message}", e)
            return Result.failure(e)
        }

        Log.d(TAG, "SESSION_SAVED_LOCALLY | sessionId=$sessionId")

        val results = buildSetResults(sessionId, setParamState)
        try {
            resultDao.insertResults(results)
            Log.d(TAG, "SET_RESULTS_SAVED_LOCALLY | count=${results.size}")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_SAVING_RESULTS | error=${e.message}", e)
            sessionDao.deleteSession(sessionId)
            return Result.failure(e)
        }

        val syncResult = syncSessionToBackend(sessionId, routineId, startedAt, finishedAt, performanceScore, results, setParamState)

        when (syncResult) {
            is Resource.Success -> {
                Log.i(TAG, "SYNC_SUCCESS | sessionId=$sessionId | backendId=${syncResult.data?.id}")
                sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
                resultDao.markSessionResultsAsSynced(sessionId)
            }
            is Resource.Error -> {
                Log.w(TAG, "SYNC_FAILED | sessionId=$sessionId | error=${syncResult.message}")
            }
            else -> {}
        }

        return Result.success(sessionId)
    }

    suspend fun syncSession(sessionId: Long): Boolean {
        Log.d(TAG, "SYNC_SESSION | sessionId=$sessionId")

        val session = sessionDao.getSessionById(sessionId) ?: run {
            Log.w(TAG, "SYNC_FAILED_SESSION_NOT_FOUND | sessionId=$sessionId")
            return false
        }

        val results = resultDao.getResultsBySession(sessionId)
        if (results.isEmpty()) {
            Log.w(TAG, "SYNC_SKIPPED_NO_RESULTS | sessionId=$sessionId")
            sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
            return true
        }

        val syncResult = syncSessionToBackend(
            sessionId = sessionId,
            routineId = session.routineId,
            startedAt = session.startedAt,
            finishedAt = session.finishedAt,
            performanceScore = null,
            results = results
        )

        return when (syncResult) {
            is Resource.Success -> {
                sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
                resultDao.markSessionResultsAsSynced(sessionId)
                Log.i(TAG, "SYNC_SESSION_SUCCESS | sessionId=$sessionId")
                true
            }
            else -> {
                Log.w(TAG, "SYNC_SESSION_FAILED | sessionId=$sessionId")
                false
            }
        }
    }

    /**
     * Sincroniza todas las sesiones pendientes.
     */
    suspend fun syncAllPendingSessions(): Int {
        val pending = sessionDao.getPendingSync()

        if (pending.isEmpty()) {
            Log.i(TAG, "NO_PENDING_SESSIONS")
            return 0
        }

        Log.i(TAG, "SYNCING_PENDING_SESSIONS | count=${pending.size}")

        var synced = 0
        pending.forEach { session ->
            if (syncSession(session.id)) synced++
        }

        Log.i(TAG, "SYNC_COMPLETED | synced=$synced/${pending.size}")
        return synced
    }

    // ── Consultas (backend con fallback a Room) ──────────────────────────────

    /**
     * Obtiene historial de workouts.
     */
    suspend fun getWorkoutHistory(
        routineId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): Resource<PageResponse<WorkoutSessionSummaryResponse>> {

        val networkResult = call {
            service.getWorkoutHistory(
                routineId = routineId,
                page = page,
                size = size
            )
        }

        if (networkResult is Resource.Success) {
            return networkResult
        }

        return try {
            val local = if (routineId != null) {
                sessionDao.getSessionsByRoutine(routineId)
            } else {
                sessionDao.getAllSessions()
            }

            val summaries = local.map { it.toSummary() }

            Resource.Success(
                PageResponse(
                    content = summaries,
                    pageNumber = 0,
                    pageSize = summaries.size,
                    totalElements = summaries.size.toLong(),
                    totalPages = 1,
                    first = true,
                    last = true,
                    numberOfElements = summaries.size,
                    sort = null
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "FALLBACK_FAILED | error=${e.message}", e)
            networkResult
        }
    }

    /**
     * Obtiene sesiones recientes.
     */
    suspend fun getRecentWorkouts(limit: Int = 10): Resource<PageResponse<WorkoutSessionSummaryResponse>> {
        val networkResult = call { service.getRecentWorkouts(limit) }

        if (networkResult is Resource.Success) {
            return networkResult
        }

        return try {
            val local = sessionDao.getRecentSessions(limit)
            val summaries = local.map { it.toSummary() }

            Resource.Success(
                PageResponse(
                    content = summaries,
                    pageNumber = 0,
                    pageSize = summaries.size,
                    totalElements = summaries.size.toLong(),
                    totalPages = 1,
                    first = true,
                    last = true,
                    numberOfElements = summaries.size,
                    sort = null
                )
            )
        } catch (e: Exception) {
            networkResult
        }
    }

    /**
     * Obtiene detalles de una sesión.
     */
    suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse> {
        return call { service.getWorkoutSessionDetails(sessionId) }
    }

    /**
     * Elimina una sesión.
     */
    suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit> {
        // Eliminar localmente primero
        try {
            sessionDao.deleteSession(sessionId)
            Log.d(TAG, "SESSION_DELETED_LOCALLY | sessionId=$sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_DELETING_LOCALLY | sessionId=$sessionId | error=${e.message}", e)
        }

        // Intentar eliminar en backend
        return callUnit { service.deleteWorkoutSession(sessionId) }
    }

    /**
     * Obtiene volumen total.
     */
    suspend fun getTotalVolume(): Resource<Double> {
        return call { service.getTotalVolume() }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private fun buildSetResults(
        sessionId: Long,
        setParamState: Map<Long, Map<String, Any?>>
    ): List<WorkoutSetResultEntity> {
        val results = mutableListOf<WorkoutSetResultEntity>()

        setParamState.forEach { (setTemplateId, setData) ->
            @Suppress("UNCHECKED_CAST")
            val paramMap = setData["parameters"] as? Map<Long, Map<String, Any?>> ?: return@forEach

            paramMap.forEach { (parameterId, values) ->
                Log.d(TAG, "BUILD_RESULT | setId=$setTemplateId | paramId=$parameterId | values=$values")

                results.add(
                    WorkoutSetResultEntity(
                        workoutSessionId = sessionId,
                        setTemplateId = setTemplateId,
                        parameterId = parameterId,
                        repetitions = values["repetitions"] as? Int,
                        numericValue = values["numericValue"] as? Double,
                        durationValue = values["durationValue"] as? Long,
                        integerValue = values["integerValue"] as? Int,
                        syncStatus = SyncStatus.PENDING_CREATE
                    )
                )
            }
        }

        Log.i(TAG, "BUILD_SET_RESULTS_COMPLETE | totalResults=${results.size}")
        return results
    }
    private suspend fun syncSessionToBackend(
        sessionId: Long,
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>,
        setParamState: Map<Long, Map<String, Any?>> = emptyMap()
    ): Resource<WorkoutSessionResponse> {

        try {
            val request = buildSaveWorkoutSessionRequest(
                routineId, startedAt, finishedAt, performanceScore, results, setParamState
            )

            Log.d(TAG, "SYNC_TO_BACKEND | routineId=$routineId | setExecutions=${request.setExecutions.size}")

            request.setExecutions.forEachIndexed { index, execution ->
                Log.d(TAG, "SET_$index | setTemplateId=${execution.setTemplateId} | exerciseId=${execution.exerciseId} | params=${execution.parameters.size}")
                execution.parameters.forEach { param ->
                    Log.d(TAG, "  PARAM | id=${param.parameterId} | numeric=${param.numericValue} | int=${param.integerValue} | duration=${param.durationValue}")
                }
            }

            return call { service.saveWorkoutSession(request) }

        } catch (e: Exception) {
            Log.w(TAG, "SYNC_EXCEPTION | error=${e.message}", e)
            return Resource.Error(exceptionMessage(e))
        }
    }

    private fun buildSaveWorkoutSessionRequest(
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>,
        setParamState: Map<Long, Map<String, Any?>> // ⭐ NUEVO parámetro
    ): SaveWorkoutSessionRequest {

        val startTime = timestampToIso(startedAt)
        val endTime = timestampToIso(finishedAt)

        val setExecutions = results
            .groupBy { it.setTemplateId }
            .map { (setTemplateId, params) ->

                // ⭐ OBTENER exerciseId del estado
                val exerciseId = (setParamState[setTemplateId]?.get("exerciseId") as? Long) ?: 0L

                // Filtrar parámetros que tienen AL MENOS UN valor
                val validParams = params.mapNotNull { result ->
                    val hasValue = result.numericValue != null && result.numericValue != 0.0 ||
                            result.integerValue != null && result.integerValue != 0 ||
                            result.durationValue != null && result.durationValue != 0L ||
                            result.repetitions != null && result.repetitions != 0

                    if (hasValue) {
                        SaveWorkoutSessionRequest.ParameterValueRequest(
                            parameterId = result.parameterId,
                            numericValue = result.numericValue?.takeIf { it != 0.0 },
                            integerValue = result.integerValue?.takeIf { it != 0 },
                            durationValue = result.durationValue?.takeIf { it != 0L },
                            stringValue = null
                        )
                    } else {
                        null
                    }
                }

                if (validParams.isNotEmpty()) {
                    SaveWorkoutSessionRequest.SetExecutionRequest(
                        setTemplateId = setTemplateId,
                        exerciseId = exerciseId, // ⭐ USAR exerciseId REAL
                        position = 1,
                        setType = "NORMAL",
                        status = "COMPLETED",
                        parameters = validParams
                    )
                } else {
                    null
                }
            }
            .filterNotNull()

        return SaveWorkoutSessionRequest(
            routineId = routineId,
            startTime = startTime,
            endTime = endTime,
            performanceScore = performanceScore,
            setExecutions = setExecutions
        )
    }

    private fun timestampToIso(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return dateTime.format(ISO_FORMATTER)
    }

    private fun WorkoutSessionEntity.toSummary() = WorkoutSessionSummaryResponse(
        id = id,
        routineId = routineId,
        routineName = "Routine $routineId",
        startTime = timestampToIso(startedAt),
        endTime = timestampToIso(finishedAt),
        durationSeconds = (finishedAt - startedAt) / 1000,
        performanceScore = null,
        totalVolume = null,
        exerciseCount = 0,
        setCount = 0
    )

    // ── Helpers de red ────────────────────────────────────────────────────────

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("El servidor respondió sin datos")
            } else {
                Resource.Error(httpErrorMessage(response.code(), response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<Unit>): Resource<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(httpErrorMessage(response.code(), response.errorBody()?.string()))
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int, body: String?): String = when (code) {
        401 -> "Sesión expirada. Vuelve a iniciar sesión."
        403 -> "No tienes permisos para realizar esta acción."
        404 -> "Recurso no encontrado."
        500 -> "Error del servidor. Intenta nuevamente."
        else -> "Error $code: ${body ?: "Error desconocido"}"
    }

    private fun exceptionMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado. Verifica tu conexión."
        is ConnectException -> "Sin conexión. Verifica tu internet."
        is HttpException -> httpErrorMessage(e.code(), e.message())
        else -> "Error: ${e.message ?: "Error desconocido"}"
    }
}
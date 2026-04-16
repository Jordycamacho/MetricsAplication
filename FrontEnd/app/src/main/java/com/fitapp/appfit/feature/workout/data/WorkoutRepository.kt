package com.fitapp.appfit.feature.workout.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.feature.workout.model.request.SaveWorkoutSessionRequest
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse
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

class WorkoutRepository(private val context: Context) {

    private val service = WorkoutService.instance
    private val db by lazy { AppDatabase.getInstance(context) }
    private val sessionDao by lazy { db.workoutSessionDao() }
    private val resultDao by lazy { db.workoutSetResultDao() }

    companion object {
        private const val TAG = "WorkoutRepository"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GUARDAR SESIÓN DE WORKOUT
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun saveWorkoutSession(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>,
        startedAt: Long = System.currentTimeMillis(),
        finishedAt: Long = System.currentTimeMillis(),
        performanceScore: Int? = null
    ): Result<Long> {

        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "SAVE_WORKOUT_START")
        Log.i(TAG, "routineId: $routineId")
        Log.i(TAG, "userId: $userId")
        Log.i(TAG, "completedSets: ${setCompletionState.count { it.value }}")
        Log.i(TAG, "totalSets: ${setCompletionState.size}")
        Log.i(TAG, "modifiedSets: ${setParamState.size}")
        Log.i(TAG, "performanceScore: $performanceScore")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        // ─────────────────────────────────────────────────────────────────────
        // 1. GUARDAR LOCALMENTE (Room)
        // ─────────────────────────────────────────────────────────────────────

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
            Log.e(TAG, "❌ ERROR_SAVING_SESSION_LOCALLY | error=${e.message}", e)
            return Result.failure(e)
        }

        Log.i(TAG, "✅ SESSION_SAVED_LOCALLY | sessionId=$sessionId")

        // ─────────────────────────────────────────────────────────────────────
        // 2. CONSTRUIR SET RESULTS
        // ─────────────────────────────────────────────────────────────────────

        val results = buildSetResults(sessionId, setParamState, setCompletionState)

        if (results.isEmpty()) {
            Log.w(TAG, "⚠️ NO_RESULTS_TO_SAVE | sessionId=$sessionId")
            sessionDao.deleteSession(sessionId)
            return Result.failure(Exception("No hay sets completados para guardar"))
        }

        try {
            resultDao.insertResults(results)
            Log.i(TAG, "✅ SET_RESULTS_SAVED_LOCALLY | count=${results.size}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR_SAVING_RESULTS | error=${e.message}", e)
            sessionDao.deleteSession(sessionId)
            return Result.failure(e)
        }

        // ─────────────────────────────────────────────────────────────────────
        // 3. SINCRONIZAR AL BACKEND
        // ─────────────────────────────────────────────────────────────────────

        val syncResult = syncSessionToBackend(
            sessionId, routineId, startedAt, finishedAt, performanceScore, results
        )

        when (syncResult) {
            is Resource.Success -> {
                Log.i(TAG, "✅ SESSION_SYNC_SUCCESS | sessionId=$sessionId | backendId=${syncResult.data?.id}")
                syncResult.data?.id?.let { remoteId ->
                    sessionDao.updateRemoteId(sessionId, remoteId)
                }
                sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
                resultDao.markSessionResultsAsSynced(sessionId)
            }
            is Resource.Error -> {
                Log.w(TAG, "⚠️ SESSION_SYNC_FAILED | sessionId=$sessionId | error=${syncResult.message}")
                Log.w(TAG, "   → Datos guardados localmente. Se reintentará después.")
            }
            else -> {}
        }

        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "SAVE_WORKOUT_COMPLETE | sessionId=$sessionId")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        return Result.success(sessionId)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // OBTENER ÚLTIMOS VALORES PARA RUTINA
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun getLastValuesForRoutine(routineId: Long): Resource<Map<Long, LastExerciseValuesResponse>> {
        Log.i(TAG, "GET_LAST_VALUES_FOR_ROUTINE | routineId=$routineId")

        return try {
            val response = service.getLastValuesForRoutine(routineId)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    // El backend devuelve Map<String, LastExerciseValuesResponse>
                    // Convertir a Map<Long, LastExerciseValuesResponse>
                    val convertedMap = body.mapKeys { it.key.toLong() }

                    Log.i(TAG, "✅ LAST_VALUES_FETCHED | exerciseCount=${convertedMap.size}")
                    convertedMap.forEach { (exerciseId, values) ->
                        Log.d(TAG, "   Exercise $exerciseId → ${values.sets.size} sets")
                    }

                    Resource.Success(convertedMap)
                } else {
                    Log.w(TAG, "⚠️ LAST_VALUES_EMPTY_BODY")
                    Resource.Success(emptyMap())
                }
            } else {
                val errorMsg = httpErrorMessage(response.code(), response.errorBody()?.string())
                Log.e(TAG, "❌ LAST_VALUES_ERROR | code=${response.code()} | error=$errorMsg")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = exceptionMessage(e)
            Log.e(TAG, "❌ LAST_VALUES_EXCEPTION | error=$errorMsg", e)
            Resource.Error(errorMsg)
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONSULTAS (backend con fallback a Room)
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun getWorkoutHistory(
        routineId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): Resource<PageResponse<WorkoutSessionSummaryResponse>> {

        Log.i(TAG, "GET_WORKOUT_HISTORY | routineId=$routineId | page=$page | size=$size")

        val networkResult = call {
            service.getWorkoutHistory(
                routineId = routineId,
                page = page,
                size = size
            )
        }

        if (networkResult is Resource.Success) {
            Log.i(TAG, "✅ HISTORY_FROM_NETWORK | count=${networkResult.data?.content?.size}")
            return networkResult
        }

        Log.w(TAG, "⚠️ NETWORK_FAILED_USING_FALLBACK")

        return try {
            val local = if (routineId != null) {
                sessionDao.getSessionsByRoutine(routineId)
            } else {
                sessionDao.getAllSessions()
            }

            Log.i(TAG, "✅ HISTORY_FROM_LOCAL | count=${local.size}")

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
            Log.e(TAG, "❌ FALLBACK_FAILED | error=${e.message}", e)
            networkResult
        }
    }

    suspend fun getRecentWorkouts(limit: Int = 10): Resource<PageResponse<WorkoutSessionSummaryResponse>> {
        Log.i(TAG, "GET_RECENT_WORKOUTS | limit=$limit")

        val networkResult = call { service.getRecentWorkouts(limit) }

        if (networkResult is Resource.Success) {
            Log.i(TAG, "✅ RECENT_FROM_NETWORK | count=${networkResult.data?.content?.size}")
            return networkResult
        }

        Log.w(TAG, "⚠️ NETWORK_FAILED_USING_FALLBACK")

        return try {
            val local = sessionDao.getRecentSessions(limit)

            Log.i(TAG, "✅ RECENT_FROM_LOCAL | count=${local.size}")

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
            Log.e(TAG, "❌ FALLBACK_FAILED | error=${e.message}", e)
            networkResult
        }
    }

    suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse> {
        Log.i(TAG, "GET_WORKOUT_DETAILS | sessionId=$sessionId")
        return call { service.getWorkoutSessionDetails(sessionId) }
    }

    suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit> {
        Log.i(TAG, "DELETE_WORKOUT_SESSION | sessionId=$sessionId")

        try {
            sessionDao.deleteSession(sessionId)
            Log.i(TAG, "✅ SESSION_DELETED_LOCALLY | sessionId=$sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR_DELETING_LOCALLY | sessionId=$sessionId | error=${e.message}", e)
        }

        return callUnit { service.deleteWorkoutSession(sessionId) }
    }

    suspend fun getTotalVolume(): Resource<Double> {
        Log.i(TAG, "GET_TOTAL_VOLUME")
        return call { service.getTotalVolume() }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════════════════

    private suspend fun syncSessionToBackend(
        sessionId: Long,
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>
    ): Resource<WorkoutSessionResponse> {

        Log.i(TAG, "───────────────────────────────────────────────────────────")
        Log.i(TAG, "SYNC_SESSION_TO_BACKEND")
        Log.i(TAG, "sessionId: $sessionId")
        Log.i(TAG, "routineId: $routineId")
        Log.i(TAG, "results: ${results.size}")
        Log.i(TAG, "───────────────────────────────────────────────────────────")

        try {
            val request = buildSaveWorkoutSessionRequest(
                routineId, startedAt, finishedAt, performanceScore, results
            )

            Log.i(TAG, "REQUEST_BUILT | setExecutions=${request.setExecutions.size}")

            request.setExecutions.forEachIndexed { index, execution ->
                Log.d(TAG, "  SET[$index] → setTemplateId=${execution.setTemplateId} " +
                        "| exerciseId=${execution.exerciseId} " +
                        "| position=${execution.position} " +
                        "| status=${execution.status} " +
                        "| params=${execution.parameters.size}")

                execution.parameters.forEach { param ->
                    Log.d(TAG, "    PARAM → id=${param.parameterId} " +
                            "| numeric=${param.numericValue} " +
                            "| integer=${param.integerValue} " +
                            "| duration=${param.durationValue}")
                }
            }

            return call { service.saveWorkoutSession(request) }

        } catch (e: Exception) {
            Log.e(TAG, "❌ SYNC_SESSION_EXCEPTION | error=${e.message}", e)
            return Resource.Error(exceptionMessage(e))
        }
    }

    private fun buildSetResults(
        sessionId: Long,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>
    ): List<WorkoutSetResultEntity> {

        Log.i(TAG, "───────────────────────────────────────────────────────────")
        Log.i(TAG, "BUILD_SET_RESULTS")
        Log.i(TAG, "sessionId: $sessionId")
        Log.i(TAG, "setParamState entries: ${setParamState.size}")
        Log.i(TAG, "setCompletionState entries: ${setCompletionState.size}")
        Log.i(TAG, "───────────────────────────────────────────────────────────")

        val results = mutableListOf<WorkoutSetResultEntity>()
        val completedSetIds = setCompletionState.filter { it.value }.keys

        Log.i(TAG, "COMPLETED_SETS: ${completedSetIds.size}")

        completedSetIds.forEach { setTemplateId ->
            Log.d(TAG, "  Processing setId=$setTemplateId")

            val setData = setParamState[setTemplateId]

            if (setData == null) {
                Log.w(TAG, "  ⚠️ SKIP_SET_NO_DATA | setId=$setTemplateId")
                return@forEach
            }

            val exerciseId = (setData["exerciseId"] as? Long) ?: run {
                Log.w(TAG, "  ⚠️ SKIP_SET_NO_EXERCISE_ID | setId=$setTemplateId | setData=$setData")
                return@forEach
            }

            @Suppress("UNCHECKED_CAST")
            val paramMap = setData["parameters"] as? Map<Long, Map<String, Any?>> ?: run {
                Log.w(TAG, "  ⚠️ SKIP_SET_NO_PARAMS | setId=$setTemplateId")
                return@forEach
            }

            Log.d(TAG, "  ✓ exerciseId=$exerciseId | params=${paramMap.size}")

            paramMap.forEach { (parameterId, values) ->
                Log.d(TAG, "    PARAM[$parameterId] → $values")

                results.add(
                    WorkoutSetResultEntity(
                        workoutSessionId = sessionId,
                        setTemplateId = setTemplateId,
                        exerciseId = exerciseId,
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

        Log.i(TAG, "✅ BUILD_SET_RESULTS_COMPLETE | totalResults=${results.size}")
        Log.i(TAG, "───────────────────────────────────────────────────────────")

        return results
    }

    private fun buildSaveWorkoutSessionRequest(
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>
    ): SaveWorkoutSessionRequest {

        val startTime = timestampToIso(startedAt)
        val endTime = timestampToIso(finishedAt)

        Log.d(TAG, "BUILD_REQUEST | startTime=$startTime | endTime=$endTime")

        val groupedBySet = results.groupBy { Triple(it.setTemplateId, it.exerciseId, it.workoutSessionId) }

        Log.d(TAG, "GROUPED_SETS | count=${groupedBySet.size}")

        val setExecutions = groupedBySet.entries.mapIndexed { index, (key, params) ->
            val (setTemplateId, exerciseId, _) = key

            Log.d(TAG, "SET[$index] → setTemplateId=$setTemplateId | exerciseId=$exerciseId | params=${params.size}")

            val validParams = params.mapNotNull { result ->
                val hasValue = result.numericValue != null ||
                        result.integerValue != null ||
                        result.durationValue != null ||
                        result.repetitions != null

                if (hasValue) {
                    SaveWorkoutSessionRequest.ParameterValueRequest(
                        parameterId = result.parameterId,
                        numericValue = result.numericValue?.takeIf { it != 0.0 },
                        integerValue = (result.integerValue ?: result.repetitions)?.takeIf { it != 0 },
                        durationValue = result.durationValue?.takeIf { it != 0L },
                        stringValue = null
                    )
                } else {
                    Log.w(TAG, "  ⚠️ SKIP_EMPTY_PARAM | parameterId=${result.parameterId}")
                    null
                }
            }

            if (validParams.isEmpty()) {
                Log.w(TAG, "  ⚠️ SET_HAS_NO_VALID_PARAMS | setTemplateId=$setTemplateId")
            }

            SaveWorkoutSessionRequest.SetExecutionRequest(
                setTemplateId = setTemplateId,
                exerciseId = exerciseId,
                position = index + 1,
                setType = "NORMAL",
                status = "COMPLETED",
                parameters = validParams
            )
        }

        Log.i(TAG, "✅ REQUEST_BUILT | setExecutions=${setExecutions.size}")

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
        id = remoteId ?: id,
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

    suspend fun syncAllPendingSessions(): Int {
        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "SYNC_ALL_PENDING_SESSIONS")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        val pending = sessionDao.getPendingSync()

        if (pending.isEmpty()) {
            Log.i(TAG, "✅ NO_PENDING_SESSIONS")
            return 0
        }

        Log.i(TAG, "SYNCING_PENDING_SESSIONS | count=${pending.size}")

        var synced = 0
        pending.forEach { session ->
            Log.d(TAG, "  Processing session ${session.id}...")

            val results = resultDao.getResultsBySession(session.id)

            if (results.isEmpty()) {
                Log.w(TAG, "  ⚠️ SESSION_HAS_NO_RESULTS | sessionId=${session.id}")
                sessionDao.updateSyncStatus(session.id, SyncStatus.SYNCED)
                synced++
                return@forEach
            }

            val syncResult = syncSessionToBackend(
                sessionId = session.id,
                routineId = session.routineId,
                startedAt = session.startedAt,
                finishedAt = session.finishedAt,
                performanceScore = null,
                results = results
            )

            when (syncResult) {
                is Resource.Success -> {
                    Log.i(TAG, "  ✅ SESSION_SYNCED | sessionId=${session.id}")
                    sessionDao.updateSyncStatus(session.id, SyncStatus.SYNCED)
                    resultDao.markSessionResultsAsSynced(session.id)

                    syncResult.data?.id?.let { remoteId ->
                        sessionDao.updateRemoteId(session.id, remoteId)
                    }

                    synced++
                }
                is Resource.Error -> {
                    Log.w(TAG, "  ⚠️ SESSION_SYNC_FAILED | sessionId=${session.id} | error=${syncResult.message}")
                }
                else -> {}
            }
        }

        Log.i(TAG, "═══════════════════════════════════════════════════════════")
        Log.i(TAG, "SYNC_COMPLETED | synced=$synced/${pending.size}")
        Log.i(TAG, "═══════════════════════════════════════════════════════════")

        return synced
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
package com.fitapp.appfit.feature.workout.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.feature.workout.model.request.SaveWorkoutSessionRequest
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.feature.routine.model.setemplate.request.BulkUpdateSetParametersRequest
import com.fitapp.appfit.feature.routine.data.RoutineSetTemplateService
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
    private val setTemplateService = RoutineSetTemplateService.instance
    private val db by lazy { AppDatabase.getInstance(context) }
    private val sessionDao by lazy { db.workoutSessionDao() }
    private val resultDao by lazy { db.workoutSetResultDao() }

    companion object {
        private const val TAG = "WorkoutRepository"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    suspend fun saveWorkoutSession(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>,
        startedAt: Long = System.currentTimeMillis(),
        finishedAt: Long = System.currentTimeMillis(),
        performanceScore: Int? = null
    ): Result<Long> {

        Log.i(TAG, "SAVE_WORKOUT_START | routineId=$routineId | modifiedSets=${setParamState.size} | completedSets=${setCompletionState.count { it.value }}")

        // ═══════════════════════════════════════════════════════════════════════
        // 1. GUARDAR LOCALMENTE (Room)
        // ═══════════════════════════════════════════════════════════════════════

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

        val results = buildSetResults(sessionId, setParamState, setCompletionState)

        try {
            resultDao.insertResults(results)
            Log.d(TAG, "SET_RESULTS_SAVED_LOCALLY | count=${results.size}")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_SAVING_RESULTS | error=${e.message}", e)
            sessionDao.deleteSession(sessionId)
            return Result.failure(e)
        }

        var sessionSyncSuccess = false
        var setResultsSyncSuccess = false

        val sessionSyncResult = syncSessionToBackend(
            sessionId, routineId, startedAt, finishedAt, performanceScore, results, setParamState, setCompletionState
        )

        when (sessionSyncResult) {
            is Resource.Success -> {
                sessionSyncSuccess = true
                Log.i(TAG, "SESSION_SYNC_SUCCESS | sessionId=$sessionId | backendId=${sessionSyncResult.data?.id}")
                sessionSyncResult.data?.id?.let { remoteId ->
                    sessionDao.updateRemoteId(sessionId, remoteId)
                } ?: run {
                    sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
                }
            }
            is Resource.Error -> {
                Log.w(TAG, "SESSION_SYNC_FAILED | sessionId=$sessionId | error=${sessionSyncResult.message}")
            }
            else -> {}
        }

        val setResultsSyncResult = syncSetResultsToBackend(results)

        when (setResultsSyncResult) {
            is Resource.Success -> {
                setResultsSyncSuccess = true
                Log.i(TAG, "SET_RESULTS_SYNC_SUCCESS | sessionId=$sessionId")
                resultDao.markSessionResultsAsSynced(sessionId)
            }
            is Resource.Error -> {
                Log.w(TAG, "SET_RESULTS_SYNC_FAILED | sessionId=$sessionId | error=${setResultsSyncResult.message}")
            }
            else -> {}
        }

        if (sessionSyncSuccess && setResultsSyncSuccess) {
            sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
            Log.i(TAG, "FULL_SYNC_SUCCESS | sessionId=$sessionId")
        } else if (sessionSyncSuccess || setResultsSyncSuccess) {
            Log.w(TAG, "PARTIAL_SYNC | sessionId=$sessionId | session=$sessionSyncSuccess | sets=$setResultsSyncSuccess")
        } else {
            Log.w(TAG, "FULL_SYNC_FAILED | sessionId=$sessionId | ambas llamadas fallaron")
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

        var sessionSyncSuccess = false
        var setResultsSyncSuccess = false

        val sessionSyncResult = syncSessionToBackend(
            sessionId = sessionId,
            routineId = session.routineId,
            startedAt = session.startedAt,
            finishedAt = session.finishedAt,
            performanceScore = null,
            results = results
        )

        if (sessionSyncResult is Resource.Success) {
            sessionSyncSuccess = true
            sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
        }

        val setResultsSyncResult = syncSetResultsToBackend(results)

        if (setResultsSyncResult is Resource.Success) {
            setResultsSyncSuccess = true
            resultDao.markSessionResultsAsSynced(sessionId)
        }

        val success = sessionSyncSuccess && setResultsSyncSuccess

        if (success) {
            Log.i(TAG, "SYNC_SESSION_SUCCESS | sessionId=$sessionId")
        } else {
            Log.w(TAG, "SYNC_SESSION_PARTIAL | sessionId=$sessionId | session=$sessionSyncSuccess | sets=$setResultsSyncSuccess")
        }

        return success
    }

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

    suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse> {
        return call { service.getWorkoutSessionDetails(sessionId) }
    }

    suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit> {
        try {
            sessionDao.deleteSession(sessionId)
            Log.d(TAG, "SESSION_DELETED_LOCALLY | sessionId=$sessionId")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_DELETING_LOCALLY | sessionId=$sessionId | error=${e.message}", e)
        }

        return callUnit { service.deleteWorkoutSession(sessionId) }
    }

    suspend fun getTotalVolume(): Resource<Double> {
        return call { service.getTotalVolume() }
    }

    private suspend fun syncSessionToBackend(
        sessionId: Long,
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>,
        setParamState: Map<Long, Map<String, Any?>> = emptyMap(),
        setCompletionState: Map<Long, Boolean> = emptyMap()
    ): Resource<WorkoutSessionResponse> {

        try {
            val request = buildSaveWorkoutSessionRequest(
                routineId, startedAt, finishedAt, performanceScore, results, setParamState, setCompletionState
            )

            Log.d(TAG, "SYNC_SESSION_TO_BACKEND | routineId=$routineId | setExecutions=${request.setExecutions.size}")

            request.setExecutions.forEachIndexed { index, execution ->
                Log.d(TAG, "SET_$index | setTemplateId=${execution.setTemplateId} | exerciseId=${execution.exerciseId} | status=${execution.status} | params=${execution.parameters.size}")
            }

            return call { service.saveWorkoutSession(request) }

        } catch (e: Exception) {
            Log.w(TAG, "SYNC_SESSION_EXCEPTION | error=${e.message}", e)
            return Resource.Error(exceptionMessage(e))
        }
    }

    private suspend fun syncSetResultsToBackend(
        results: List<WorkoutSetResultEntity>
    ): Resource<Unit> {

        try {
            val request = buildBulkUpdateSetParametersRequest(results)

            Log.d(TAG, "SYNC_SET_RESULTS_TO_BACKEND | setResults=${request.setResults.size}")

            request.setResults.forEachIndexed { index, setResult ->
                Log.d(TAG, "SET_RESULT_$index | setTemplateId=${setResult.setTemplateId} | params=${setResult.parameters.size}")
            }

            val response = setTemplateService.bulkSaveSetParameters(request)

            return if (response.isSuccessful) {
                Log.i(TAG, "BULK_SAVE_SET_PARAMETERS_SUCCESS")
                Resource.Success(Unit)
            } else {
                val errorMsg = httpErrorMessage(response.code(), response.errorBody()?.string())
                Log.w(TAG, "BULK_SAVE_SET_PARAMETERS_FAILED | $errorMsg")
                Resource.Error(errorMsg)
            }

        } catch (e: Exception) {
            Log.w(TAG, "BULK_SAVE_SET_PARAMETERS_EXCEPTION | error=${e.message}", e)
            return Resource.Error(exceptionMessage(e))
        }
    }

    private fun buildSetResults(
        sessionId: Long,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>
    ): List<WorkoutSetResultEntity> {
        val results = mutableListOf<WorkoutSetResultEntity>()

        val completedSetIds = setCompletionState.filter { it.value }.keys

        Log.d(TAG, "BUILD_RESULTS | completedSets=${completedSetIds.size} | modifiedSets=${setParamState.size}")

        completedSetIds.forEach { setTemplateId ->
            val setData = setParamState[setTemplateId]

            val exerciseId = (setData?.get("exerciseId") as? Long) ?: run {
                Log.w(TAG, "SKIP_SET_NO_EXERCISE_ID | setId=$setTemplateId")
                return@forEach
            }

            @Suppress("UNCHECKED_CAST")
            val paramMap = setData["parameters"] as? Map<Long, Map<String, Any?>> ?: emptyMap()

            if (paramMap.isEmpty()) {
                Log.w(TAG, "SKIP_SET_NO_PARAMS | setId=$setTemplateId")
                return@forEach
            }

            paramMap.forEach { (parameterId, values) ->
                Log.d(TAG, "BUILD_RESULT | setId=$setTemplateId | paramId=$parameterId | values=$values")

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

        Log.i(TAG, "BUILD_SET_RESULTS_COMPLETE | totalResults=${results.size}")
        return results
    }

    private fun buildSaveWorkoutSessionRequest(
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>
    ): SaveWorkoutSessionRequest {

        val startTime = timestampToIso(startedAt)
        val endTime = timestampToIso(finishedAt)

        val setExecutions = results
            .groupBy { it.setTemplateId }
            .map { (setTemplateId, params) ->

                val exerciseId = (setParamState[setTemplateId]?.get("exerciseId") as? Long) ?: 0L
                val isCompleted = setCompletionState[setTemplateId] ?: false

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

                val status = if (isCompleted) "COMPLETED" else "SKIPPED"

                SaveWorkoutSessionRequest.SetExecutionRequest(
                    setTemplateId = setTemplateId,
                    exerciseId = exerciseId,
                    position = 1,
                    setType = "NORMAL",
                    status = status,
                    parameters = validParams
                )
            }
            .filterNotNull()

        Log.i(TAG, "BUILD_REQUEST | setExecutions=${setExecutions.size} | completed=${setExecutions.count { it.status == "COMPLETED" }} | skipped=${setExecutions.count { it.status == "SKIPPED" }}")

        return SaveWorkoutSessionRequest(
            routineId = routineId,
            startTime = startTime,
            endTime = endTime,
            performanceScore = performanceScore,
            setExecutions = setExecutions
        )
    }

    private fun buildBulkUpdateSetParametersRequest(
        results: List<WorkoutSetResultEntity>
    ): BulkUpdateSetParametersRequest {

        val grouped = results.groupBy { it.setTemplateId }

        val setResults = grouped.map { (setTemplateId, params) ->
            BulkUpdateSetParametersRequest.SetResultRequest(
                setTemplateId = setTemplateId,
                parameters = params.map { r ->
                    BulkUpdateSetParametersRequest.ParameterResultRequest(
                        parameterId = r.parameterId,
                        repetitions = r.repetitions,
                        numericValue = r.numericValue,
                        durationValue = r.durationValue,
                        integerValue = r.integerValue
                    )
                }
            )
        }

        return BulkUpdateSetParametersRequest(setResults)
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
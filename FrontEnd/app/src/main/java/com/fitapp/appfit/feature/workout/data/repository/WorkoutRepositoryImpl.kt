package com.fitapp.appfit.feature.workout.data.repository

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.feature.workout.data.datasource.WorkoutService
import com.fitapp.appfit.feature.workout.domain.repository.IWorkoutRepository
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

class WorkoutRepositoryImpl(private val context: Context) : IWorkoutRepository {

    private val service = WorkoutService.instance
    private val db by lazy { AppDatabase.getInstance(context) }
    private val sessionDao by lazy { db.workoutSessionDao() }
    private val resultDao by lazy { db.workoutSetResultDao() }

    companion object {
        private const val TAG = "WorkoutRepository"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    // ── Save session ──────────────────────────────────────────────────────────

    override suspend fun saveWorkoutSession(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?
    ): Result<Long> {

        Log.i(TAG, "SAVE_WORKOUT_START | routineId=$routineId | userId=$userId " +
                "| completedSets=${setCompletionState.count { it.value }} " +
                "| totalSets=${setCompletionState.size}")

        // 1. Save locally
        val sessionId = try {
            val session = WorkoutSessionEntity(
                routineId = routineId,
                userId = userId,
                startedAt = startedAt,
                finishedAt = finishedAt,
                syncStatus = SyncStatus.PENDING_CREATE,
                lastModifiedLocally = System.currentTimeMillis()
            )
            sessionDao.insertSession(session)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_SAVING_SESSION_LOCALLY | error=${e.message}", e)
            return Result.failure(e)
        }

        Log.i(TAG, "SESSION_SAVED_LOCALLY | sessionId=$sessionId")

        // 2. Build set results
        val results = buildSetResults(sessionId, setParamState, setCompletionState)
        if (results.isEmpty()) {
            Log.w(TAG, "NO_RESULTS_TO_SAVE | sessionId=$sessionId")
            sessionDao.deleteSession(sessionId)
            return Result.failure(Exception("No hay sets completados para guardar"))
        }

        try {
            resultDao.insertResults(results)
            Log.i(TAG, "SET_RESULTS_SAVED_LOCALLY | count=${results.size}")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_SAVING_RESULTS | error=${e.message}", e)
            sessionDao.deleteSession(sessionId)
            return Result.failure(e)
        }

        // 3. Sync to backend
        val syncResult = syncSessionToBackend(
            sessionId, routineId, startedAt, finishedAt, performanceScore, results
        )

        when (syncResult) {
            is Resource.Success -> {
                syncResult.data?.id?.let { remoteId ->
                    sessionDao.updateRemoteId(sessionId, remoteId)
                }
                sessionDao.updateSyncStatus(sessionId, SyncStatus.SYNCED)
                resultDao.markSessionResultsAsSynced(sessionId)
                Log.i(TAG, "SESSION_SYNC_SUCCESS | sessionId=$sessionId | backendId=${syncResult.data?.id}")
            }
            is Resource.Error -> {
                Log.w(TAG, "SESSION_SYNC_FAILED | sessionId=$sessionId | error=${syncResult.message} " +
                        "→ saved locally, will retry later")
            }
            else -> {}
        }

        Log.i(TAG, "SAVE_WORKOUT_COMPLETE | sessionId=$sessionId")
        return Result.success(sessionId)
    }

    // ── Last values ───────────────────────────────────────────────────────────

    override suspend fun getLastValuesForRoutine(routineId: Long): Resource<Map<Long, LastExerciseValuesResponse>> {
        Log.i(TAG, "GET_LAST_VALUES_FOR_ROUTINE | routineId=$routineId")
        return try {
            val response = service.getLastValuesForRoutine(routineId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val converted = body.mapKeys { it.key.toLong() }
                    Log.i(TAG, "LAST_VALUES_FETCHED | exerciseCount=${converted.size}")
                    Resource.Success(converted)
                } else {
                    Log.w(TAG, "LAST_VALUES_EMPTY_BODY")
                    Resource.Success(emptyMap())
                }
            } else {
                val msg = httpErrorMessage(response.code(), response.errorBody()?.string())
                Log.e(TAG, "LAST_VALUES_HTTP_ERROR | code=${response.code()} | error=$msg")
                Resource.Error(msg)
            }
        } catch (e: Exception) {
            val msg = exceptionMessage(e)
            Log.e(TAG, "LAST_VALUES_EXCEPTION | error=$msg", e)
            Resource.Error(msg)
        }
    }

    // ── Query methods ─────────────────────────────────────────────────────────

    override suspend fun getWorkoutHistory(
        routineId: Long?,
        page: Int,
        size: Int
    ): Resource<PageResponse<WorkoutSessionSummaryResponse>> {

        Log.i(TAG, "GET_WORKOUT_HISTORY | routineId=$routineId | page=$page | size=$size")

        val networkResult = call {
            service.getWorkoutHistory(routineId = routineId, page = page, size = size)
        }

        if (networkResult is Resource.Success) return networkResult

        Log.w(TAG, "NETWORK_FAILED_USING_LOCAL_FALLBACK")
        return try {
            val local = if (routineId != null) sessionDao.getSessionsByRoutine(routineId)
            else sessionDao.getAllSessions()
            val summaries = local.map { it.toSummary() }
            Resource.Success(summaries.toPageResponse())
        } catch (e: Exception) {
            Log.e(TAG, "FALLBACK_FAILED | error=${e.message}", e)
            networkResult
        }
    }

    override suspend fun getRecentWorkouts(limit: Int): Resource<PageResponse<WorkoutSessionSummaryResponse>> {
        Log.i(TAG, "GET_RECENT_WORKOUTS | limit=$limit")

        val networkResult = call { service.getRecentWorkouts(limit) }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val local = sessionDao.getRecentSessions(limit)
            Resource.Success(local.map { it.toSummary() }.toPageResponse())
        } catch (e: Exception) {
            networkResult
        }
    }

    override suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse> {
        Log.i(TAG, "GET_WORKOUT_DETAILS | sessionId=$sessionId")
        return call { service.getWorkoutSessionDetails(sessionId) }
    }

    override suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit> {
        Log.i(TAG, "DELETE_WORKOUT_SESSION | sessionId=$sessionId")
        try { sessionDao.deleteSession(sessionId) } catch (e: Exception) {
            Log.e(TAG, "ERROR_DELETING_LOCALLY | sessionId=$sessionId | error=${e.message}", e)
        }
        return callUnit { service.deleteWorkoutSession(sessionId) }
    }

    override suspend fun getTotalVolume(): Resource<Double> = call { service.getTotalVolume() }

    // ── Pending sync ──────────────────────────────────────────────────────────

    override suspend fun syncAllPendingSessions(): Int {
        Log.i(TAG, "SYNC_ALL_PENDING_SESSIONS")
        val pending = sessionDao.getPendingSync()
        if (pending.isEmpty()) { Log.i(TAG, "NO_PENDING_SESSIONS"); return 0 }

        var synced = 0
        for (session in pending) {
            val results = resultDao.getResultsBySession(session.id)
            if (results.isEmpty()) {
                sessionDao.updateSyncStatus(session.id, SyncStatus.SYNCED)
                synced++
                continue
            }

            val syncResult = syncSessionToBackend(
                sessionId = session.id,
                routineId = session.routineId,
                startedAt = session.startedAt,
                finishedAt = session.finishedAt,
                performanceScore = null,
                results = results
            )

            if (syncResult is Resource.Success) {
                sessionDao.updateSyncStatus(session.id, SyncStatus.SYNCED)
                resultDao.markSessionResultsAsSynced(session.id)
                syncResult.data?.id?.let { sessionDao.updateRemoteId(session.id, it) }
                synced++
            } else {
                Log.w(TAG, "PENDING_SYNC_FAILED | sessionId=${session.id} | error=${(syncResult as? Resource.Error)?.message}")
            }
        }

        Log.i(TAG, "SYNC_COMPLETED | synced=$synced/${pending.size}")
        return synced
    }

    // ── Private: sync ─────────────────────────────────────────────────────────

    private suspend fun syncSessionToBackend(
        sessionId: Long,
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>
    ): Resource<WorkoutSessionResponse> {
        return try {
            val request = buildSaveWorkoutSessionRequest(
                routineId, startedAt, finishedAt, performanceScore, results
            )
            Log.i(TAG, "SYNC_REQUEST_BUILT | setExecutions=${request.setExecutions.size}")
            call { service.saveWorkoutSession(request) }
        } catch (e: Exception) {
            Log.e(TAG, "SYNC_SESSION_EXCEPTION | error=${e.message}", e)
            Resource.Error(exceptionMessage(e))
        }
    }

    // ── Private: build set results ────────────────────────────────────────────

    private fun buildSetResults(
        sessionId: Long,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>
    ): List<WorkoutSetResultEntity> {

        val results = mutableListOf<WorkoutSetResultEntity>()
        val completedSetIds = setCompletionState.filter { it.value }.keys

        for (setTemplateId in completedSetIds) {
            val setData = setParamState[setTemplateId] ?: continue

            val exerciseId = setData["exerciseId"] as? Long ?: continue

            @Suppress("UNCHECKED_CAST")
            val paramMap = setData["parameters"] as? Map<Long, Map<String, Any?>> ?: continue

            for ((parameterId, values) in paramMap) {
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

    // ── Private: build request ────────────────────────────────────────────────

    private fun buildSaveWorkoutSessionRequest(
        routineId: Long,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int?,
        results: List<WorkoutSetResultEntity>
    ): SaveWorkoutSessionRequest {

        val startTime = timestampToIso(startedAt)
        val endTime = timestampToIso(finishedAt)

        val groupedBySet = results.groupBy { it.setTemplateId to it.exerciseId }
        val groupedByExercise = results.groupBy { it.exerciseId }
        val setOrderByExercise: Map<Long, List<Long>> = groupedByExercise.mapValues { (_, rows) ->
            rows.map { it.setTemplateId }.distinct()
        }

        val setExecutions = groupedBySet.map { (key, params) ->
            val (setTemplateId, exerciseId) = key

            val positionInExercise =
                (setOrderByExercise[exerciseId]?.indexOf(setTemplateId) ?: 0) + 1

            val validParams = params.mapNotNull { result ->
                val hasValue = result.numericValue != null ||
                        result.integerValue != null ||
                        result.durationValue != null ||
                        result.repetitions != null

                if (!hasValue) return@mapNotNull null

                SaveWorkoutSessionRequest.ParameterValueRequest(
                    parameterId = result.parameterId,
                    numericValue = result.numericValue?.takeIf { it != 0.0 },
                    integerValue = (result.integerValue ?: result.repetitions)?.takeIf { it != 0 },
                    durationValue = result.durationValue?.takeIf { it != 0L },
                    stringValue = null
                )
            }

            SaveWorkoutSessionRequest.SetExecutionRequest(
                setTemplateId = setTemplateId,
                exerciseId = exerciseId,
                position = positionInExercise,
                setType = "NORMAL",
                status = "COMPLETED",
                parameters = validParams
            )
        }

        return SaveWorkoutSessionRequest(
            routineId = routineId,
            startTime = startTime,
            endTime = endTime,
            performanceScore = performanceScore,
            setExecutions = setExecutions
        )
    }

    // ── Private: helpers ──────────────────────────────────────────────────────

    private fun timestampToIso(timestamp: Long): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()
        )
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

    private fun List<WorkoutSessionSummaryResponse>.toPageResponse() = PageResponse(
        content = this,
        pageNumber = 0,
        pageSize = size,
        totalElements = size.toLong(),
        totalPages = 1,
        first = true,
        last = true,
        numberOfElements = size,
        sort = null
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
package com.fitapp.appfit.feature.metrics.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.metrics.data.calculator.DaySessionInferrer
import com.fitapp.appfit.feature.metrics.data.calculator.VolumeCalculator
import com.fitapp.appfit.feature.metrics.domain.model.SessionHistoryFilter
import com.fitapp.appfit.feature.metrics.domain.repository.IMetricsReadRepository
import com.fitapp.appfit.feature.metrics.domain.util.DaySessionLabelFormatter
import com.fitapp.appfit.feature.routine.database.dao.RoutineDao
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.feature.workout.data.datasource.WorkoutService
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MetricsReadRepositoryImpl(context: Context) : IMetricsReadRepository {

    private val appContext = context.applicationContext
    private val service = WorkoutService.instance
    private val db by lazy { AppDatabase.getInstance(appContext) }
    private val sessionDao by lazy { db.workoutSessionDao() }
    private val resultDao by lazy { db.workoutSetResultDao() }
    private val routineDao by lazy { db.routineDao() }
    private val daySessionInferrer by lazy { DaySessionInferrer(db) }

    companion object {
        private const val TAG = "MetricsReadRepository"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    override suspend fun getWorkoutHistory(filter: SessionHistoryFilter): Resource<PageResponse<WorkoutSessionSummaryResponse>> {
        Log.i(TAG, "GET_HISTORY | filter=$filter")

        val networkResult = call {
            service.getWorkoutHistory(
                routineId = filter.routineId,
                fromDate = filter.fromDate,
                toDate = filter.toDate,
                dayOfWeek = filter.dayOfWeek,
                sessionNumber = filter.sessionNumber,
                page = filter.page,
                size = filter.size
            )
        }

        if (networkResult is Resource.Success) return networkResult

        Log.w(TAG, "NETWORK_FAILED_USING_LOCAL_FALLBACK")
        return try {
            val local = when {
                filter.routineId != null -> sessionDao.getSessionsByRoutine(filter.routineId)
                else -> sessionDao.getAllSessions()
            }
            val filtered = local.filter { session ->
                matchesDateRange(session.startedAt, filter.fromDate, filter.toDate) &&
                    matchesDaySession(session, filter.dayOfWeek, filter.sessionNumber)
            }
            val summaries = filtered.map { session ->
                buildLocalSummary(session)
            }
            Resource.Success(summaries.toPageResponse())
        } catch (e: Exception) {
            Log.e(TAG, "FALLBACK_FAILED | error=${e.message}", e)
            networkResult
        }
    }

    override suspend fun getRecentWorkouts(limit: Int): Resource<PageResponse<WorkoutSessionSummaryResponse>> {
        val networkResult = call { service.getRecentWorkouts(limit) }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val local = sessionDao.getRecentSessions(limit)
            Resource.Success(local.map { buildLocalSummary(it) }.toPageResponse())
        } catch (e: Exception) {
            networkResult
        }
    }

    override suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse> {
        return call { service.getWorkoutSessionDetails(sessionId) }
    }

    override suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit> {
        try {
            val local = sessionDao.getSessionByRemoteId(sessionId)
                ?: sessionDao.getSessionById(sessionId)
            local?.let { sessionDao.deleteSession(it.id) }
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_DELETING_LOCALLY | sessionId=$sessionId", e)
        }
        return callUnit { service.deleteWorkoutSession(sessionId) }
    }

    override suspend fun getTotalVolume(): Resource<Double> = call { service.getTotalVolume() }

    private suspend fun buildLocalSummary(session: WorkoutSessionEntity): WorkoutSessionSummaryResponse {
        val results = resultDao.getResultsBySession(session.id)
        val routineName = routineDao.getRoutineById(session.routineId)?.name
            ?: "Rutina ${session.routineId}"
        val label = daySessionInferrer.inferForSession(session, results)

        return WorkoutSessionSummaryResponse(
            id = session.remoteId ?: session.id,
            routineId = session.routineId,
            routineName = routineName,
            startTime = timestampToIso(session.startedAt),
            endTime = timestampToIso(session.finishedAt),
            durationSeconds = (session.finishedAt - session.startedAt) / 1000,
            performanceScore = null,
            totalVolume = VolumeCalculator.calculateSessionVolume(results),
            exerciseCount = VolumeCalculator.countDistinctExercises(results),
            setCount = VolumeCalculator.countDistinctSets(results),
            dayOfWeek = label.dayOfWeek,
            sessionNumber = label.sessionNumber,
            dayLabel = label.displayLabel
        )
    }

    private fun matchesDaySession(
        session: WorkoutSessionEntity,
        dayOfWeek: String?,
        sessionNumber: Int?
    ): Boolean {
        if (dayOfWeek != null && session.dayOfWeek != dayOfWeek) return false
        if (sessionNumber != null && session.sessionNumber != sessionNumber) return false
        return true
    }

    private fun matchesDateRange(startedAt: Long, fromDate: String?, toDate: String?): Boolean {
        if (fromDate == null && toDate == null) return true
        val sessionDate = Instant.ofEpochMilli(startedAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val from = fromDate?.let { LocalDate.parse(it) }
        val to = toDate?.let { LocalDate.parse(it) }
        if (from != null && sessionDate.isBefore(from)) return false
        if (to != null && sessionDate.isAfter(to)) return false
        return true
    }

    private fun timestampToIso(timestamp: Long): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()
        )
        return dateTime.format(ISO_FORMATTER)
    }

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

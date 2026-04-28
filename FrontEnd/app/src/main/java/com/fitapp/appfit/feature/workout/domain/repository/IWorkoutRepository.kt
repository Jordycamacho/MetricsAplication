package com.fitapp.appfit.feature.workout.domain.repository

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse

interface IWorkoutRepository {

    suspend fun saveWorkoutSession(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>,
        startedAt: Long = System.currentTimeMillis(),
        finishedAt: Long = System.currentTimeMillis(),
        performanceScore: Int? = null
    ): Result<Long>

    suspend fun getLastValuesForRoutine(routineId: Long): Resource<Map<Long, LastExerciseValuesResponse>>

    suspend fun getWorkoutHistory(
        routineId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): Resource<PageResponse<WorkoutSessionSummaryResponse>>

    suspend fun getRecentWorkouts(limit: Int = 10): Resource<PageResponse<WorkoutSessionSummaryResponse>>

    suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse>

    suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit>

    suspend fun getTotalVolume(): Resource<Double>

    suspend fun syncAllPendingSessions(): Int
}
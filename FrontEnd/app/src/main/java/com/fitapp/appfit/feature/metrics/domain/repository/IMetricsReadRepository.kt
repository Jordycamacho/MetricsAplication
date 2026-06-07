package com.fitapp.appfit.feature.metrics.domain.repository

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.metrics.domain.model.SessionHistoryFilter
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse

/**
 * Read-only repository for metrics/analytics.
 * Execution writes via [com.fitapp.appfit.feature.workout.domain.repository.IWorkoutRepository].
 *
 * Future v2 endpoints (not implemented yet):
 * - GET api/metrics/exercise-progress
 * - GET api/metrics/snapshots
 * - GET api/metrics/personal-records
 */
interface IMetricsReadRepository {

    suspend fun getWorkoutHistory(
        filter: SessionHistoryFilter = SessionHistoryFilter()
    ): Resource<PageResponse<WorkoutSessionSummaryResponse>>

    suspend fun getRecentWorkouts(limit: Int = 10): Resource<PageResponse<WorkoutSessionSummaryResponse>>

    suspend fun getWorkoutSessionDetails(sessionId: Long): Resource<WorkoutSessionResponse>

    suspend fun deleteWorkoutSession(sessionId: Long): Resource<Unit>

    suspend fun getTotalVolume(): Resource<Double>
}

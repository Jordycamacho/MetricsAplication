package com.fitapp.appfit.feature.workout.data.datasource

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.workout.model.request.SaveWorkoutSessionRequest
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkoutService {

    @POST("api/workouts/sessions")
    suspend fun saveWorkoutSession(
        @Body request: SaveWorkoutSessionRequest
    ): Response<WorkoutSessionResponse>

    @GET("api/workouts/sessions/{sessionId}")
    suspend fun getWorkoutSessionDetails(
        @Path("sessionId") sessionId: Long
    ): Response<WorkoutSessionResponse>

    @GET("api/workouts/history")
    suspend fun getWorkoutHistory(
        @Query("routineId") routineId: Long? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
        @Query("minPerformanceScore") minPerformanceScore: Int? = null,
        @Query("maxPerformanceScore") maxPerformanceScore: Int? = null,
        @Query("sortBy") sortBy: String = "startTime",
        @Query("sortDirection") sortDirection: String = "DESC",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<WorkoutSessionSummaryResponse>>

    @GET("api/workouts/recent")
    suspend fun getRecentWorkouts(
        @Query("limit") limit: Int = 10
    ): Response<PageResponse<WorkoutSessionSummaryResponse>>

    @DELETE("api/workouts/sessions/{sessionId}")
    suspend fun deleteWorkoutSession(
        @Path("sessionId") sessionId: Long
    ): Response<Unit>

    @GET("api/workouts/stats/total-volume")
    suspend fun getTotalVolume(): Response<Double>

    // ── History endpoints ─────────────────────────────────────────────────────

    @GET("api/workouts/history/exercises/{exerciseId}/last-values")
    suspend fun getLastExerciseValues(
        @Path("exerciseId") exerciseId: Long
    ): Response<LastExerciseValuesResponse>

    @POST("api/workouts/history/exercises/last-values")
    suspend fun getLastValuesForExercises(
        @Body exerciseIds: List<Long>
    ): Response<Map<String, LastExerciseValuesResponse>>

    @GET("api/workouts/start/{routineId}/last-values")
    suspend fun getLastValuesForRoutine(
        @Path("routineId") routineId: Long
    ): Response<Map<String, LastExerciseValuesResponse>>

    companion object {
        val instance: WorkoutService by lazy {
            ApiClient.instance.create(WorkoutService::class.java)
        }
    }
}
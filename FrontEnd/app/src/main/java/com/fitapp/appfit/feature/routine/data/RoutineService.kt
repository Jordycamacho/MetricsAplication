package com.fitapp.appfit.feature.routine.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.routine.model.rutine.request.CreateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.request.UpdateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineStatisticsResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoutineService {

    @POST("api/routines/create")
    suspend fun createRoutine(@Body request: CreateRoutineRequest): Response<RoutineResponse>

    @GET("api/routines/{id}")
    suspend fun getRoutine(@Path("id") id: Long): Response<RoutineResponse>

    @PUT("api/routines/{id}")
    suspend fun updateRoutine(
        @Path("id") id: Long,
        @Body request: UpdateRoutineRequest
    ): Response<RoutineResponse>

    @DELETE("api/routines/{id}")
    suspend fun deleteRoutine(@Path("id") id: Long): Response<Unit>

    @POST("api/routines/generate-default")
    suspend fun generateDefaultRoutine(
        @Query("type") type: String
    ): Response<Map<String, Long>>

    // Listados
    @GET("api/routines")
    suspend fun getRoutines(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDirection") sortDirection: String = "DESC"
    ): Response<PageResponse<RoutineSummaryResponse>>

    @GET("api/routines/recent")
    suspend fun getRecentRoutines(
        @Query("limit") limit: Int = 5
    ): Response<List<RoutineSummaryResponse>>

    @GET("api/routines/active")
    suspend fun getActiveRoutines(): Response<List<RoutineSummaryResponse>>

    // Filtros
    @GET("api/routines/filter")
    suspend fun getRoutinesWithFilters(
        @Query("sportId") sportId: Long? = null,
        @Query("name") name: String? = null,
        @Query("isActive") isActive: Boolean? = null,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDirection") sortDirection: String = "DESC",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<RoutineSummaryResponse>>

    // Estado activo
    @PATCH("api/routines/{id}/active")
    suspend fun toggleRoutineActiveStatus(
        @Path("id") id: Long,
        @Query("active") active: Boolean
    ): Response<Unit>

    // Estadísticas
    @GET("api/routines/statistics")
    suspend fun getRoutineStatistics(): Response<RoutineStatisticsResponse>

    @GET("api/routines/last-used")
    suspend fun getLastUsedRoutines(
        @Query("limit") limit: Int = 3
    ): Response<List<RoutineSummaryResponse>>

    @PATCH("api/routines/{id}/mark-as-used")
    suspend fun markRoutineAsUsed(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/routines/{id}/for-training")
    suspend fun getRoutineForTraining(@Path("id") id: Long): Response<RoutineResponse>

    companion object {
        val instance: RoutineService by lazy {
            ApiClient.instance.create(RoutineService::class.java)
        }
    }
}
package com.fitapp.appfit.feature.exercise.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExercisePageResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseService {

    @POST("api/exercises/search")
    suspend fun searchExercises(
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    @POST("api/exercises/my/search")
    suspend fun searchMyExercises(
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    @POST("api/exercises/available/search")
    suspend fun searchAvailableExercises(
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    @POST("api/exercises/sport/{sportId}/search")
    suspend fun searchExercisesBySport(
        @Path("sportId") sportId: Long,
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    @GET("api/exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    @POST("api/exercises")
    suspend fun createExercise(
        @Body exerciseRequest: ExerciseRequest
    ): Response<ExerciseResponse>

    @PUT("api/exercises/{id}")
    suspend fun updateExercise(
        @Path("id") id: Long,
        @Body exerciseRequest: ExerciseRequest
    ): Response<ExerciseResponse>

    @DELETE("api/exercises/{id}")
    suspend fun deleteExercise(
        @Path("id") id: Long
    ): Response<Void>

    @PATCH("api/exercises/{id}/toggle-status")
    suspend fun toggleExerciseStatus(
        @Path("id") id: Long
    ): Response<Void>

    @POST("api/exercises/{id}/rate")
    suspend fun rateExercise(
        @Path("id") id: Long,
        @Query("rating") rating: Double
    ): Response<Void>

    @POST("api/exercises/{id}/duplicate")
    suspend fun duplicateExercise(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    @PATCH("api/exercises/{id}/make-public")
    suspend fun makeExercisePublic(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    @GET("api/exercises/recently-used")
    suspend fun getRecentlyUsedExercises(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ExercisePageResponse>

    @GET("api/exercises/most-popular")
    suspend fun getMostPopularExercises(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ExercisePageResponse>

    @GET("api/exercises/top-rated")
    suspend fun getTopRatedExercises(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ExercisePageResponse>

    @GET("api/exercises/count/my")
    suspend fun getMyExerciseCount(): Response<Long>

    companion object {
        val instance: ExerciseService by lazy {
            ApiClient.instance.create(ExerciseService::class.java)
        }
    }
}
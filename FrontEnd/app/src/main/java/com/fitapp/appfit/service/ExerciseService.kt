package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExercisePageResponse
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import retrofit2.Response
import retrofit2.http.*

interface ExerciseService {

    // Búsqueda general de ejercicios
    @POST("api/exercises/search")
    suspend fun searchExercises(
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    // Mis ejercicios personales
    @POST("api/exercises/my/search")
    suspend fun searchMyExercises(
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    // Ejercicios disponibles (públicos + míos)
    @POST("api/exercises/available/search")
    suspend fun searchAvailableExercises(
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    // Ejercicios por deporte
    @POST("api/exercises/sport/{sportId}/search")
    suspend fun searchExercisesBySport(
        @Path("sportId") sportId: Long,
        @Body filterRequest: ExerciseFilterRequest
    ): Response<ExercisePageResponse>

    // Obtener ejercicio por ID
    @GET("api/exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    // Obtener ejercicio por ID con relaciones
    @GET("api/exercises/{id}/detailed")
    suspend fun getExerciseByIdWithRelations(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    // Crear ejercicio
    @POST("api/exercises")
    suspend fun createExercise(
        @Body exerciseRequest: ExerciseRequest
    ): Response<ExerciseResponse>

    // Actualizar ejercicio
    @PUT("api/exercises/{id}")
    suspend fun updateExercise(
        @Path("id") id: Long,
        @Body exerciseRequest: ExerciseRequest
    ): Response<ExerciseResponse>

    // Eliminar ejercicio
    @DELETE("api/exercises/{id}")
    suspend fun deleteExercise(
        @Path("id") id: Long
    ): Response<Void>

    // Activar/Desactivar ejercicio
    @PATCH("api/exercises/{id}/toggle-status")
    suspend fun toggleExerciseStatus(
        @Path("id") id: Long
    ): Response<Void>

    // Incrementar uso
    @PATCH("api/exercises/{id}/increment-usage")
    suspend fun incrementExerciseUsage(
        @Path("id") id: Long
    ): Response<Void>

    // Calificar ejercicio
    @POST("api/exercises/{id}/rate")
    suspend fun rateExercise(
        @Path("id") id: Long,
        @Query("rating") rating: Double
    ): Response<Void>

    // Duplicar ejercicio
    @POST("api/exercises/{id}/duplicate")
    suspend fun duplicateExercise(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    // Hacer ejercicio público (solo admin)
    @PATCH("api/exercises/{id}/make-public")
    suspend fun makeExercisePublic(
        @Path("id") id: Long
    ): Response<ExerciseResponse>

    // Obtener ejercicios recientemente usados
    @GET("api/exercises/recently-used")
    suspend fun getRecentlyUsedExercises(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ExercisePageResponse>

    // Obtener ejercicios más populares
    @GET("api/exercises/most-popular")
    suspend fun getMostPopularExercises(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ExercisePageResponse>

    // Obtener ejercicios mejor calificados
    @GET("api/exercises/top-rated")
    suspend fun getTopRatedExercises(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ExercisePageResponse>

    // Obtener contador de ejercicios del usuario
    @GET("api/exercises/count/my")
    suspend fun getMyExerciseCount(): Response<Long>

    companion object {
        val instance: ExerciseService by lazy {
            ApiClient.instance.create(ExerciseService::class.java)
        }
    }
}
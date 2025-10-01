package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.exercise.CreateExerciseRequest
import com.fitapp.appfit.response.exercise.ExerciseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseService {
    @GET("api/exercises")
    suspend fun getExercises(
        @Query("sportId") sportId: Long? = null
    ): Response<List<ExerciseResponse>>

    @GET("api/exercises/{id}")
    suspend fun getExerciseById(@Path("id") exerciseId: Long): Response<ExerciseResponse>

    @POST("api/exercises")
    suspend fun createExercise(@Body request: CreateExerciseRequest): Response<ExerciseResponse>

    companion object {
        val instance: ExerciseService by lazy {
            ApiClient.instance.create(ExerciseService::class.java)
        }
    }
}
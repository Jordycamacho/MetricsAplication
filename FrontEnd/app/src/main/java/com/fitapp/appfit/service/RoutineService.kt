package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.page.PageResponse
import com.fitapp.appfit.response.routine.request.AddExercisesToRoutineRequest
import com.fitapp.appfit.response.routine.request.CreateRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RoutineService {
    @POST("api/routines/create")
    suspend fun createRoutine(@Body request: CreateRoutineRequest): Response<RoutineResponse>

    @POST("api/routines/exercises")
    suspend fun addExercisesToRoutine(@Body request: AddExercisesToRoutineRequest): Response<RoutineResponse>

    @GET("api/routines/{id}")
    suspend fun getRoutine(@Path("id") id: Long): Response<RoutineResponse>

    @GET("api/routines")
    suspend fun getRoutines(): Response<PageResponse<RoutineSummaryResponse>>

    companion object {
        val instance: RoutineService by lazy {
            ApiClient.instance.create(RoutineService::class.java)
        }
    }
}
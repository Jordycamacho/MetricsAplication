package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.sets.request.CreateSetTemplateRequest
import com.fitapp.appfit.response.sets.request.UpdateSetTemplateRequest
import retrofit2.Response
import retrofit2.http.*

interface RoutineSetTemplateService {

    // CRUD básico
    @POST("api/routine-set-templates")
    suspend fun createSetTemplate(@Body request: CreateSetTemplateRequest): Response<RoutineSetTemplateResponse>

    @PUT("api/routine-set-templates/{id}")
    suspend fun updateSetTemplate(
        @Path("id") id: Long,
        @Body request: UpdateSetTemplateRequest
    ): Response<RoutineSetTemplateResponse>

    @GET("api/routine-set-templates/{id}")
    suspend fun getSetTemplate(@Path("id") id: Long): Response<RoutineSetTemplateResponse>

    @DELETE("api/routine-set-templates/{id}")
    suspend fun deleteSetTemplate(@Path("id") id: Long): Response<Void>

    // Listados
    @GET("api/routine-set-templates/by-routine-exercise/{routineExerciseId}")
    suspend fun getSetTemplatesByRoutineExercise(
        @Path("routineExerciseId") routineExerciseId: Long
    ): Response<List<RoutineSetTemplateResponse>>

    @GET("api/routine-set-templates/by-routine-exercise/{routineExerciseId}/group/{groupId}")
    suspend fun getSetTemplatesByGroup(
        @Path("routineExerciseId") routineExerciseId: Long,
        @Path("groupId") groupId: String
    ): Response<List<RoutineSetTemplateResponse>>

    // Reordenar
    @PATCH("api/routine-set-templates/reorder/{routineExerciseId}")
    suspend fun reorderSetTemplates(
        @Path("routineExerciseId") routineExerciseId: Long,
        @Body setTemplateIds: List<Long>
    ): Response<RoutineSetTemplateResponse>

    // Eliminar por ejercicio
    @DELETE("api/routine-set-templates/by-routine-exercise/{routineExerciseId}")
    suspend fun deleteSetTemplatesByRoutineExercise(
        @Path("routineExerciseId") routineExerciseId: Long
    ): Response<Void>

    companion object {
        val instance: RoutineSetTemplateService by lazy {
            ApiClient.instance.create(RoutineSetTemplateService::class.java)
        }
    }
}
package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.routine.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import retrofit2.Response
import retrofit2.http.*

interface RoutineExerciseService {

    // Agregar ejercicio a rutina
    @POST("api/routines/{routineId}/exercises")
    suspend fun addExerciseToRoutine(
        @Path("routineId") routineId: Long,
        @Body request: AddExerciseToRoutineRequest
    ): Response<RoutineExerciseResponse>

    // Actualizar ejercicio en rutina
    @PUT("api/routines/{routineId}/exercises/{exerciseId}")
    suspend fun updateExerciseInRoutine(
        @Path("routineId") routineId: Long,
        @Path("exerciseId") exerciseId: Long,
        @Body request: AddExerciseToRoutineRequest
    ): Response<RoutineExerciseResponse>

    // Eliminar ejercicio de rutina
    @DELETE("api/routines/{routineId}/exercises/{exerciseId}")
    suspend fun removeExerciseFromRoutine(
        @Path("routineId") routineId: Long,
        @Path("exerciseId") exerciseId: Long
    ): Response<Void>

    // Obtener ejercicios por sesión
    @GET("api/routines/{routineId}/exercises/session/{sessionNumber}")
    suspend fun getExercisesBySession(
        @Path("routineId") routineId: Long,
        @Path("sessionNumber") sessionNumber: Int
    ): Response<List<RoutineExerciseResponse>>

    // Obtener ejercicios por día
    @GET("api/routines/{routineId}/exercises/day/{dayOfWeek}")
    suspend fun getExercisesByDay(
        @Path("routineId") routineId: Long,
        @Path("dayOfWeek") dayOfWeek: String
    ): Response<List<RoutineExerciseResponse>>

    // Reordenar ejercicios
    @PATCH("api/routines/{routineId}/exercises/reorder")
    suspend fun reorderExercises(
        @Path("routineId") routineId: Long,
        @Body exerciseIds: List<Long>
    ): Response<Void>

    companion object {
        val instance: RoutineExerciseService by lazy {
            ApiClient.instance.create(RoutineExerciseService::class.java)
        }
    }
}
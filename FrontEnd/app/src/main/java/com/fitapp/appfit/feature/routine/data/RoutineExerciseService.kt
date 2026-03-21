package com.fitapp.appfit.feature.routine.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @GET("api/routines/{routineId}/exercises")
    suspend fun getRoutineExercises(
        @Path("routineId") routineId: Long
    ): Response<List<RoutineExerciseResponse>>

    companion object {
        val instance: RoutineExerciseService by lazy {
            ApiClient.instance.create(RoutineExerciseService::class.java)
        }
    }
}
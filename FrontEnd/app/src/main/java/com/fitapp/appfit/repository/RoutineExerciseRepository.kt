package com.fitapp.appfit.repository

import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.service.RoutineExerciseService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response

class RoutineExerciseRepository {
    private val service = RoutineExerciseService.instance

    suspend fun getRoutineExercises(routineId: Long): Resource<List<RoutineExerciseResponse>> {
        return try {
            val response = service.getRoutineExercises(routineId)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long): Resource<Unit> {
        return try {
            val response = service.removeExerciseFromRoutine(routineId, exerciseId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Error ${response.code()}")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error eliminando ejercicio")
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let { Resource.Success(it) }
                ?: Resource.Error("Respuesta vacía")
        } else {
            Resource.Error("Error ${response.code()}")
        }
    }
}
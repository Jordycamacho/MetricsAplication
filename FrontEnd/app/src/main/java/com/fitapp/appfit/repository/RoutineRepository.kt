package com.fitapp.appfit.repository

import com.fitapp.appfit.response.routine.AddExercisesToRoutineRequest
import com.fitapp.appfit.response.routine.CreateRoutineRequest
import com.fitapp.appfit.response.routine.RoutineResponse
import com.fitapp.appfit.service.RoutineService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response

class RoutineRepository {
    private val routineService = RoutineService.instance

    suspend fun createRoutine(request: CreateRoutineRequest): Resource<RoutineResponse> {
        return try {
            val response = routineService.createRoutine(request)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error creating routine")
        }
    }

    suspend fun addExercisesToRoutine(request: AddExercisesToRoutineRequest): Resource<RoutineResponse> {
        return try {
            val response = routineService.addExercisesToRoutine(request)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error adding exercises")
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Empty response")
        } else {
            Resource.Error("Error ${response.code()}: ${response.message()}")
        }
    }
}
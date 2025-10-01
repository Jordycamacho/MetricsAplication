package com.fitapp.appfit.repository

import com.fitapp.appfit.response.exercise.CreateExerciseRequest
import com.fitapp.appfit.response.exercise.ExerciseResponse
import com.fitapp.appfit.service.ExerciseService
import com.fitapp.appfit.utils.Resource

class ExerciseRepository {
    private val exerciseService = ExerciseService.instance
    suspend fun getExercises(): Resource<List<ExerciseResponse>> {
        return try {
            val response = exerciseService.getExercises()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    suspend fun getExercisesBySport(sportId: Long?): Resource<List<ExerciseResponse>> {
        return try {
            val response = exerciseService.getExercises()
            if (response.isSuccessful) {
                response.body()?.let { exercises ->
                    val filteredExercises = if (sportId != null) {
                        exercises.filter { it.sportId == sportId }
                    } else {
                        exercises
                    }
                    Resource.Success(filteredExercises)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    suspend fun searchExercises(query: String, sportId: Long?): Resource<List<ExerciseResponse>> {
        return try {
            val response = exerciseService.getExercises()
            if (response.isSuccessful) {
                response.body()?.let { exercises ->
                    val filteredExercises = exercises.filter { exercise ->
                        (exercise.name.contains(query, ignoreCase = true) ||
                                exercise.description?.contains(query, ignoreCase = true) == true) &&
                                (sportId == null || exercise.sportId == sportId)
                    }
                    Resource.Success(filteredExercises)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    suspend fun getExerciseById(exerciseId: Long): Resource<ExerciseResponse> {
        return try {
            val response = exerciseService.getExerciseById(exerciseId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    suspend fun createExercise(request: CreateExerciseRequest): Resource<ExerciseResponse> {
        return try {
            val response = exerciseService.createExercise(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

}
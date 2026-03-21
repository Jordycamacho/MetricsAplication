package com.fitapp.appfit.feature.exercise.data

import android.util.Log
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExercisePageResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.data.ExerciseService
import retrofit2.Response

class ExerciseRepository {
    private val exerciseService = ExerciseService.Companion.instance

    companion object {
        private const val TAG = "ExerciseRepository"
    }

    suspend fun searchExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.searchExercises(filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun searchMyExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.searchMyExercises(filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchMyExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun searchAvailableExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.searchAvailableExercises(filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchAvailableExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun searchExercisesBySport(sportId: Long, filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.searchExercisesBySport(sportId, filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchExercisesBySport error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun getExerciseById(id: Long): Resource<ExerciseResponse> {
        return try {
            handleResponse(exerciseService.getExerciseById(id))
        } catch (e: Exception) {
            Log.e(TAG, "getExerciseById error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun createExercise(exerciseRequest: ExerciseRequest): Resource<ExerciseResponse> {
        return try {
            handleResponse(exerciseService.createExercise(exerciseRequest))
        } catch (e: Exception) {
            Log.e(TAG, "createExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun updateExercise(id: Long, exerciseRequest: ExerciseRequest): Resource<ExerciseResponse> {
        return try {
            handleResponse(exerciseService.updateExercise(id, exerciseRequest))
        } catch (e: Exception) {
            Log.e(TAG, "updateExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun deleteExercise(id: Long): Resource<Void> {
        return try {
            handleResponse(exerciseService.deleteExercise(id))
        } catch (e: Exception) {
            Log.e(TAG, "deleteExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun toggleExerciseStatus(id: Long): Resource<Void> {
        return try {
            handleResponse(exerciseService.toggleExerciseStatus(id))
        } catch (e: Exception) {
            Log.e(TAG, "toggleExerciseStatus error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun rateExercise(id: Long, rating: Double): Resource<Void> {
        return try {
            handleResponse(exerciseService.rateExercise(id, rating))
        } catch (e: Exception) {
            Log.e(TAG, "rateExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun duplicateExercise(id: Long): Resource<ExerciseResponse> {
        return try {
            handleResponse(exerciseService.duplicateExercise(id))
        } catch (e: Exception) {
            Log.e(TAG, "duplicateExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun makeExercisePublic(id: Long): Resource<ExerciseResponse> {
        return try {
            handleResponse(exerciseService.makeExercisePublic(id))
        } catch (e: Exception) {
            Log.e(TAG, "makeExercisePublic error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun getRecentlyUsedExercises(page: Int, size: Int): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.getRecentlyUsedExercises(page, size))
        } catch (e: Exception) {
            Log.e(TAG, "getRecentlyUsedExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun getMostPopularExercises(page: Int, size: Int): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.getMostPopularExercises(page, size))
        } catch (e: Exception) {
            Log.e(TAG, "getMostPopularExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun getTopRatedExercises(page: Int, size: Int): Resource<ExercisePageResponse> {
        return try {
            handleResponse(exerciseService.getTopRatedExercises(page, size))
        } catch (e: Exception) {
            Log.e(TAG, "getTopRatedExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun getMyExerciseCount(): Resource<Long> {
        return try {
            handleResponse(exerciseService.getMyExerciseCount())
        } catch (e: Exception) {
            Log.e(TAG, "getMyExerciseCount error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                if (response.code() == 204) {
                    @Suppress("UNCHECKED_CAST")
                    Resource.Success(null as T)
                } else {
                    Resource.Error("Respuesta vacía del servidor")
                }
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            Log.e(TAG, "handleResponse: Error ${response.code()} - $errorBody")
            Resource.Error("Error ${response.code()}: $errorBody")
        }
    }
}
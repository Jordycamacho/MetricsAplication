package com.fitapp.appfit.feature.exercise.data

import android.util.Log
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExercisePageResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import com.fitapp.appfit.feature.exercise.util.ExerciseErrorHandler
import retrofit2.Response
import java.io.IOException

class ExerciseRepository {
    private val exerciseService = ExerciseService.instance
    private val TAG = "ExerciseRepository"

    // ==================== BÚSQUEDAS ====================

    suspend fun searchExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "Searching exercises: $filterRequest")
        return safeApiCall { exerciseService.searchExercises(filterRequest) }
    }

    suspend fun searchMyExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "Searching my exercises")
        return safeApiCall { exerciseService.searchMyExercises(filterRequest) }
    }

    suspend fun searchAvailableExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "Searching available exercises")
        return safeApiCall { exerciseService.searchAvailableExercises(filterRequest) }
    }

    suspend fun searchExercisesBySport(
        sportId: Long,
        filterRequest: ExerciseFilterRequest
    ): Resource<ExercisePageResponse> {
        Log.d(TAG, "Searching exercises for sport $sportId")
        return safeApiCall { exerciseService.searchExercisesBySport(sportId, filterRequest) }
    }

    // ==================== CRUD ====================

    suspend fun getExerciseById(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "Getting exercise by ID: $id")
        return safeApiCall { exerciseService.getExerciseById(id) }
    }

    suspend fun createExercise(exerciseRequest: ExerciseRequest): Resource<ExerciseResponse> {
        Log.d(TAG, "Creating exercise: ${exerciseRequest.name} (${exerciseRequest.exerciseType})")
        return safeApiCall { exerciseService.createExercise(exerciseRequest) }
    }

    suspend fun updateExercise(id: Long, exerciseRequest: ExerciseRequest): Resource<ExerciseResponse> {
        Log.d(TAG, "Updating exercise $id: ${exerciseRequest.name}")
        return safeApiCall { exerciseService.updateExercise(id, exerciseRequest) }
    }

    suspend fun deleteExercise(id: Long): Resource<Unit> {
        Log.d(TAG, "Deleting exercise $id")
        return try {
            val response = exerciseService.deleteExercise(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Exercise $id deleted successfully")
                Resource.Success(Unit)
            } else {
                val error = ExerciseErrorHandler.getErrorMessage(response)
                Log.e(TAG, "Delete failed: $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ExerciseErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error deleting exercise: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error deleting exercise", e)
            Resource.Error(e.message ?: "Error eliminando ejercicio")
        }
    }

    suspend fun toggleExerciseStatus(id: Long): Resource<Unit> {
        Log.d(TAG, "Toggling exercise status: $id")
        return try {
            val response = exerciseService.toggleExerciseStatus(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Exercise $id status toggled")
                Resource.Success(Unit)
            } else {
                val error = ExerciseErrorHandler.getErrorMessage(response)
                Log.e(TAG, "Toggle failed: $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ExerciseErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error toggling status: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error toggling status", e)
            Resource.Error(e.message ?: "Error cambiando estado")
        }
    }

    // ==================== ACCIONES ESPECIALES ====================

    suspend fun rateExercise(id: Long, rating: Double): Resource<Unit> {
        Log.d(TAG, "Rating exercise $id: $rating")
        return try {
            val response = exerciseService.rateExercise(id, rating)
            if (response.isSuccessful) {
                Log.i(TAG, "Exercise $id rated successfully")
                Resource.Success(Unit)
            } else {
                val error = ExerciseErrorHandler.getErrorMessage(response)
                Log.e(TAG, "Rating failed: $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ExerciseErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error rating exercise: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error rating exercise", e)
            Resource.Error(e.message ?: "Error calificando ejercicio")
        }
    }

    suspend fun duplicateExercise(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "Duplicating exercise $id")
        return safeApiCall { exerciseService.duplicateExercise(id) }
    }

    suspend fun makeExercisePublic(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "Making exercise $id public")
        return safeApiCall { exerciseService.makeExercisePublic(id) }
    }

    // ==================== LISTAS ESPECIALES ====================

    suspend fun getRecentlyUsedExercises(page: Int = 0, size: Int = 10): Resource<ExercisePageResponse> {
        Log.d(TAG, "Getting recently used exercises")
        return safeApiCall { exerciseService.getRecentlyUsedExercises(page, size) }
    }

    suspend fun getMostPopularExercises(page: Int = 0, size: Int = 10): Resource<ExercisePageResponse> {
        Log.d(TAG, "Getting most popular exercises")
        return safeApiCall { exerciseService.getMostPopularExercises(page, size) }
    }

    suspend fun getTopRatedExercises(page: Int = 0, size: Int = 10): Resource<ExercisePageResponse> {
        Log.d(TAG, "Getting top rated exercises")
        return safeApiCall { exerciseService.getTopRatedExercises(page, size) }
    }

    suspend fun getMyExerciseCount(): Resource<Long> {
        Log.d(TAG, "Getting my exercise count")
        return safeApiCall { exerciseService.getMyExerciseCount() }
    }

    // ==================== HELPER ====================

    /**
     * Función genérica para llamadas seguras al API
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d(TAG, "API call successful")
                    Resource.Success(it)
                } ?: run {
                    Log.w(TAG, "Empty response body")
                    Resource.Error("Respuesta vacía del servidor")
                }
            } else {
                val error = ExerciseErrorHandler.getErrorMessage(response)
                Log.e(TAG, "API call failed (${response.code()}): $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ExerciseErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Resource.Error(e.message ?: "Error desconocido")
        }
    }
}
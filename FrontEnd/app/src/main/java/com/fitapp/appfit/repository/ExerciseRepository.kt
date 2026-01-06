package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExercisePageResponse
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.service.ExerciseService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response

class ExerciseRepository {
    private val exerciseService = ExerciseService.instance

    companion object {
        private const val TAG = "ExerciseRepository"
    }

    // Búsqueda general
    suspend fun searchExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "searchExercises: Iniciando búsqueda general")
        return try {
            handleResponse(exerciseService.searchExercises(filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Mis ejercicios
    suspend fun searchMyExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "searchMyExercises: Buscando mis ejercicios")
        return try {
            handleResponse(exerciseService.searchMyExercises(filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchMyExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Ejercicios disponibles
    suspend fun searchAvailableExercises(filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "searchAvailableExercises: Buscando ejercicios disponibles")
        return try {
            handleResponse(exerciseService.searchAvailableExercises(filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchAvailableExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Ejercicios por deporte
    suspend fun searchExercisesBySport(sportId: Long, filterRequest: ExerciseFilterRequest): Resource<ExercisePageResponse> {
        Log.d(TAG, "searchExercisesBySport: Buscando ejercicios para deporte $sportId")
        return try {
            handleResponse(exerciseService.searchExercisesBySport(sportId, filterRequest))
        } catch (e: Exception) {
            Log.e(TAG, "searchExercisesBySport error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Obtener por ID
    suspend fun getExerciseById(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "getExerciseById: Obteniendo ejercicio $id")
        return try {
            handleResponse(exerciseService.getExerciseById(id))
        } catch (e: Exception) {
            Log.e(TAG, "getExerciseById error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Obtener por ID con relaciones
    suspend fun getExerciseByIdWithRelations(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "getExerciseByIdWithRelations: Obteniendo ejercicio detallado $id")
        return try {
            handleResponse(exerciseService.getExerciseByIdWithRelations(id))
        } catch (e: Exception) {
            Log.e(TAG, "getExerciseByIdWithRelations error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Crear ejercicio
    suspend fun createExercise(exerciseRequest: ExerciseRequest): Resource<ExerciseResponse> {
        Log.d(TAG, "createExercise: Creando ejercicio ${exerciseRequest.name}")
        return try {
            handleResponse(exerciseService.createExercise(exerciseRequest))
        } catch (e: Exception) {
            Log.e(TAG, "createExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Actualizar ejercicio
    suspend fun updateExercise(id: Long, exerciseRequest: ExerciseRequest): Resource<ExerciseResponse> {
        Log.d(TAG, "updateExercise: Actualizando ejercicio $id")
        return try {
            handleResponse(exerciseService.updateExercise(id, exerciseRequest))
        } catch (e: Exception) {
            Log.e(TAG, "updateExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Eliminar ejercicio
    suspend fun deleteExercise(id: Long): Resource<Void> {
        Log.d(TAG, "deleteExercise: Eliminando ejercicio $id")
        return try {
            handleResponse(exerciseService.deleteExercise(id))
        } catch (e: Exception) {
            Log.e(TAG, "deleteExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Activar/Desactivar
    suspend fun toggleExerciseStatus(id: Long): Resource<Void> {
        Log.d(TAG, "toggleExerciseStatus: Cambiando estado ejercicio $id")
        return try {
            handleResponse(exerciseService.toggleExerciseStatus(id))
        } catch (e: Exception) {
            Log.e(TAG, "toggleExerciseStatus error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Incrementar uso
    suspend fun incrementExerciseUsage(id: Long): Resource<Void> {
        Log.d(TAG, "incrementExerciseUsage: Incrementando uso ejercicio $id")
        return try {
            handleResponse(exerciseService.incrementExerciseUsage(id))
        } catch (e: Exception) {
            Log.e(TAG, "incrementExerciseUsage error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Calificar
    suspend fun rateExercise(id: Long, rating: Double): Resource<Void> {
        Log.d(TAG, "rateExercise: Calificando ejercicio $id con $rating")
        return try {
            handleResponse(exerciseService.rateExercise(id, rating))
        } catch (e: Exception) {
            Log.e(TAG, "rateExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Duplicar
    suspend fun duplicateExercise(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "duplicateExercise: Duplicando ejercicio $id")
        return try {
            handleResponse(exerciseService.duplicateExercise(id))
        } catch (e: Exception) {
            Log.e(TAG, "duplicateExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Hacer público (solo admin)
    suspend fun makeExercisePublic(id: Long): Resource<ExerciseResponse> {
        Log.d(TAG, "makeExercisePublic: Haciendo público ejercicio $id")
        return try {
            handleResponse(exerciseService.makeExercisePublic(id))
        } catch (e: Exception) {
            Log.e(TAG, "makeExercisePublic error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Recientemente usados
    suspend fun getRecentlyUsedExercises(page: Int, size: Int): Resource<ExercisePageResponse> {
        Log.d(TAG, "getRecentlyUsedExercises: Obteniendo recientemente usados")
        return try {
            handleResponse(exerciseService.getRecentlyUsedExercises(page, size))
        } catch (e: Exception) {
            Log.e(TAG, "getRecentlyUsedExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Más populares
    suspend fun getMostPopularExercises(page: Int, size: Int): Resource<ExercisePageResponse> {
        Log.d(TAG, "getMostPopularExercises: Obteniendo más populares")
        return try {
            handleResponse(exerciseService.getMostPopularExercises(page, size))
        } catch (e: Exception) {
            Log.e(TAG, "getMostPopularExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Mejor calificados
    suspend fun getTopRatedExercises(page: Int, size: Int): Resource<ExercisePageResponse> {
        Log.d(TAG, "getTopRatedExercises: Obteniendo mejor calificados")
        return try {
            handleResponse(exerciseService.getTopRatedExercises(page, size))
        } catch (e: Exception) {
            Log.e(TAG, "getTopRatedExercises error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Contador de mis ejercicios
    suspend fun getMyExerciseCount(): Resource<Long> {
        Log.d(TAG, "getMyExerciseCount: Obteniendo mi contador")
        return try {
            handleResponse(exerciseService.getMyExerciseCount())
        } catch (e: Exception) {
            Log.e(TAG, "getMyExerciseCount error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Manejo genérico de respuesta
    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "handleResponse: Éxito - $body")
                    Resource.Success(body)
                } else {
                    Log.w(TAG, "handleResponse: Cuerpo vacío")
                    Resource.Error("Respuesta vacía del servidor")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e(TAG, "handleResponse: Error ${response.code()} - $errorBody")
                Resource.Error("Error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleResponse exception: ${e.message}", e)
            Resource.Error("Error al procesar respuesta: ${e.message}")
        }
    }
}
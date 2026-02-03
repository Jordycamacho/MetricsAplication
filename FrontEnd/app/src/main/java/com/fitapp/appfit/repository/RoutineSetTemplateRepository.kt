package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.sets.request.CreateSetTemplateRequest
import com.fitapp.appfit.response.sets.request.UpdateSetTemplateRequest
import com.fitapp.appfit.service.RoutineSetTemplateService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response

class RoutineSetTemplateRepository {
    private val setTemplateService = RoutineSetTemplateService.instance

    companion object {
        private const val TAG = "SetTemplateRepository"
    }

    // Create
    suspend fun createSetTemplate(request: CreateSetTemplateRequest): Resource<RoutineSetTemplateResponse> {
        Log.d(TAG, "createSetTemplate: Creando set template para ejercicio ${request.routineExerciseId}")
        return try {
            handleResponse(setTemplateService.createSetTemplate(request))
        } catch (e: Exception) {
            Log.e(TAG, "createSetTemplate error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Update
    suspend fun updateSetTemplate(id: Long, request: UpdateSetTemplateRequest): Resource<RoutineSetTemplateResponse> {
        Log.d(TAG, "updateSetTemplate: Actualizando set template $id")
        return try {
            handleResponse(setTemplateService.updateSetTemplate(id, request))
        } catch (e: Exception) {
            Log.e(TAG, "updateSetTemplate error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Get by ID
    suspend fun getSetTemplate(id: Long): Resource<RoutineSetTemplateResponse> {
        Log.d(TAG, "getSetTemplate: Obteniendo set template $id")
        return try {
            handleResponse(setTemplateService.getSetTemplate(id))
        } catch (e: Exception) {
            Log.e(TAG, "getSetTemplate error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Get by routine exercise
    suspend fun getSetTemplatesByRoutineExercise(routineExerciseId: Long): Resource<List<RoutineSetTemplateResponse>> {
        Log.d(TAG, "getSetTemplatesByRoutineExercise: Obteniendo sets para ejercicio $routineExerciseId")
        return try {
            handleResponseList(setTemplateService.getSetTemplatesByRoutineExercise(routineExerciseId))
        } catch (e: Exception) {
            Log.e(TAG, "getSetTemplatesByRoutineExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Get by group
    suspend fun getSetTemplatesByGroup(routineExerciseId: Long, groupId: String): Resource<List<RoutineSetTemplateResponse>> {
        Log.d(TAG, "getSetTemplatesByGroup: Obteniendo sets para grupo $groupId")
        return try {
            handleResponseList(setTemplateService.getSetTemplatesByGroup(routineExerciseId, groupId))
        } catch (e: Exception) {
            Log.e(TAG, "getSetTemplatesByGroup error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    suspend fun deleteSetTemplate(id: Long): Resource<Unit> {
        Log.d(TAG, "deleteSetTemplate: Eliminando set template $id")
        return try {
            handleResponseVoid(setTemplateService.deleteSetTemplate(id))
        } catch (e: Exception) {
            Log.e(TAG, "deleteSetTemplate error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Delete by routine exercise - Cambiado a Unit
    suspend fun deleteSetTemplatesByRoutineExercise(routineExerciseId: Long): Resource<Unit> {
        Log.d(TAG, "deleteSetTemplatesByRoutineExercise: Eliminando sets del ejercicio $routineExerciseId")
        return try {
            handleResponseVoid(setTemplateService.deleteSetTemplatesByRoutineExercise(routineExerciseId))
        } catch (e: Exception) {
            Log.e(TAG, "deleteSetTemplatesByRoutineExercise error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Reorder
    suspend fun reorderSetTemplates(routineExerciseId: Long, setTemplateIds: List<Long>): Resource<RoutineSetTemplateResponse> {
        Log.d(TAG, "reorderSetTemplates: Reordenando sets para ejercicio $routineExerciseId")
        return try {
            handleResponse(setTemplateService.reorderSetTemplates(routineExerciseId, setTemplateIds))
        } catch (e: Exception) {
            Log.e(TAG, "reorderSetTemplates error: ${e.message}", e)
            Resource.Error("Error de red: ${e.message}")
        }
    }

    // Helpers
    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "handleResponse: Éxito")
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

    private fun <T> handleResponseList(response: Response<List<T>>): Resource<List<T>> {
        return handleResponse(response)
    }

    private fun handleResponseVoid(response: Response<Void>): Resource<Unit> {
        return try {
            if (response.isSuccessful) {
                Log.d(TAG, "handleResponseVoid: Éxito")
                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e(TAG, "handleResponseVoid: Error ${response.code()} - $errorBody")
                Resource.Error("Error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleResponseVoid exception: ${e.message}", e)
            Resource.Error("Error al procesar respuesta: ${e.message}")
        }
    }
}
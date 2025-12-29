package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.request.CustomParameterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterPageResponse
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.service.ParameterService
import com.fitapp.appfit.utils.Resource

class ParameterRepository {
    private val parameterService = ParameterService.instance

    // Búsqueda general
    suspend fun searchParameters(filterRequest: CustomParameterFilterRequest): Resource<CustomParameterPageResponse> {
        return handleResponse(parameterService.searchParameters(filterRequest))
    }

    // Mis parámetros
    suspend fun searchMyParameters(filterRequest: CustomParameterFilterRequest): Resource<CustomParameterPageResponse> {
        return handleResponse(parameterService.searchMyParameters(filterRequest))
    }

    // Parámetros disponibles para un deporte
    suspend fun searchAvailableParameters(
        sportId: Long,
        filterRequest: CustomParameterFilterRequest
    ): Resource<CustomParameterPageResponse> {
        return handleResponse(parameterService.searchAvailableParameters(sportId, filterRequest))
    }

    // Obtener por ID
    suspend fun getParameterById(id: Long): Resource<CustomParameterResponse> {
        return handleResponse(parameterService.getParameterById(id))
    }

    // Crear
    suspend fun createParameter(parameterRequest: CustomParameterRequest): Resource<CustomParameterResponse> {
        return handleResponse(parameterService.createParameter(parameterRequest))
    }

    // Actualizar
    suspend fun updateParameter(id: Long, parameterRequest: CustomParameterRequest): Resource<CustomParameterResponse> {
        return handleResponse(parameterService.updateParameter(id, parameterRequest))
    }

    // Eliminar
    suspend fun deleteParameter(id: Long): Resource<Unit> {
        return try {
            val response = parameterService.deleteParameter(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error deleting parameter")
        }
    }

    // Activar/desactivar
    suspend fun toggleParameterStatus(id: Long): Resource<Unit> {
        return try {
            val response = parameterService.toggleParameterStatus(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error toggling parameter status")
        }
    }

    // Categorías
    suspend fun getCategories(): Resource<List<String>> {
        return handleResponse(parameterService.getCategories())
    }

    // Tipos
    suspend fun getParameterTypes(): Resource<List<String>> {
        return try {
            val response = parameterService.getParameterTypes()
            Log.d("ParameterRepo", "Tipos respuesta: ${response.code()} - ${response.body()}")
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    // Si viene vacío, usar valores por defecto
                    if (it.isEmpty()) {
                        Resource.Success(getDefaultParameterTypes())
                    } else {
                        Resource.Success(it)
                    }
                } ?: Resource.Success(getDefaultParameterTypes())
            } else {
                // Si falla, intentar con valores por defecto
                Log.w("ParameterRepo", "Error ${response.code()}: ${response.message()}")
                Resource.Success(getDefaultParameterTypes())
            }
        } catch (e: Exception) {
            Log.e("ParameterRepo", "Exception: ${e.message}")
            // En caso de error, usar valores por defecto
            Resource.Success(getDefaultParameterTypes())
        }
    }

    private fun getDefaultParameterTypes(): List<String> {
        return listOf(
            "NUMBER",
            "INTEGER",
            "TEXT",
            "BOOLEAN",
            "DURATION",
            "DISTANCE",
            "PERCENTAGE"
        )
    }

    // Incrementar uso
    suspend fun incrementParameterUsage(id: Long): Resource<Unit> {
        return try {
            val response = parameterService.incrementParameterUsage(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error incrementing usage")
        }
    }

    private fun <T> handleResponse(response: retrofit2.Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Empty response")
        } else {
            Resource.Error("Error ${response.code()}: ${response.message()}")
        }
    }
}
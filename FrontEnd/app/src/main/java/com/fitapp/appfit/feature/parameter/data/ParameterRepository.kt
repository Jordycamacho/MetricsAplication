package com.fitapp.appfit.feature.parameter.data

import android.util.Log
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterFilterRequest
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterRequest
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterPageResponse
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import com.fitapp.appfit.feature.parameter.data.ParameterService
import retrofit2.Response

class ParameterRepository {
    private val parameterService = ParameterService.Companion.instance

    suspend fun searchParameters(filterRequest: CustomParameterFilterRequest): Resource<CustomParameterPageResponse> {
        return handleResponse(parameterService.searchParameters(filterRequest))
    }

    suspend fun searchMyParameters(filterRequest: CustomParameterFilterRequest): Resource<CustomParameterPageResponse> {
        return handleResponse(parameterService.searchMyParameters(filterRequest))
    }

    suspend fun searchAvailableParameters(
        sportId: Long,
        filterRequest: CustomParameterFilterRequest
    ): Resource<CustomParameterPageResponse> {
        return handleResponse(parameterService.searchAvailableParameters(sportId, filterRequest))
    }

    suspend fun getParameterById(id: Long): Resource<CustomParameterResponse> {
        return handleResponse(parameterService.getParameterById(id))
    }

    suspend fun createParameter(parameterRequest: CustomParameterRequest): Resource<CustomParameterResponse> {
        return handleResponse(parameterService.createParameter(parameterRequest))
    }

    suspend fun updateParameter(id: Long, parameterRequest: CustomParameterRequest): Resource<CustomParameterResponse> {
        return handleResponse(parameterService.updateParameter(id, parameterRequest))
    }

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

    suspend fun getCategories(): Resource<List<String>> {
        return handleResponse(parameterService.getCategories())
    }

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
                Log.w("ParameterRepo", "Error ${response.code()}: ${response.message()}")
                Resource.Success(getDefaultParameterTypes())
            }
        } catch (e: Exception) {
            Log.e("ParameterRepo", "Exception: ${e.message}")
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
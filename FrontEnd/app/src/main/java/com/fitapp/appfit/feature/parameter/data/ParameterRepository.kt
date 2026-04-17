package com.fitapp.appfit.feature.parameter.data

import android.util.Log
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterFilterRequest
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterRequest
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterPageResponse
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import com.fitapp.appfit.feature.parameter.util.ParameterErrorHandler
import retrofit2.Response
import java.io.IOException

class ParameterRepository {
    private val parameterService = ParameterService.instance
    private val TAG = "ParameterRepository"

    suspend fun searchParameters(filterRequest: CustomParameterFilterRequest): Resource<CustomParameterPageResponse> {
        Log.d(TAG, "Searching parameters: $filterRequest")
        return safeApiCall { parameterService.searchParameters(filterRequest) }
    }

    suspend fun searchMyParameters(filterRequest: CustomParameterFilterRequest): Resource<CustomParameterPageResponse> {
        Log.d(TAG, "Searching my parameters: $filterRequest")
        return safeApiCall { parameterService.searchMyParameters(filterRequest) }
    }

    suspend fun searchAvailableParameters(
        sportId: Long,
        filterRequest: CustomParameterFilterRequest
    ): Resource<CustomParameterPageResponse> {
        Log.d(TAG, "Searching available parameters for sport $sportId")
        return safeApiCall { parameterService.searchAvailableParameters(sportId, filterRequest) }
    }

    suspend fun getParameterById(id: Long): Resource<CustomParameterResponse> {
        Log.d(TAG, "Getting parameter by ID: $id")
        return safeApiCall { parameterService.getParameterById(id) }
    }

    suspend fun createParameter(parameterRequest: CustomParameterRequest): Resource<CustomParameterResponse> {
        Log.d(TAG, "Creating parameter: ${parameterRequest.name} (${parameterRequest.parameterType})")
        return safeApiCall { parameterService.createParameter(parameterRequest) }
    }

    suspend fun updateParameter(id: Long, parameterRequest: CustomParameterRequest): Resource<CustomParameterResponse> {
        Log.d(TAG, "Updating parameter $id: ${parameterRequest.name}")
        return safeApiCall { parameterService.updateParameter(id, parameterRequest) }
    }

    suspend fun deleteParameter(id: Long): Resource<Unit> {
        Log.d(TAG, "Deleting parameter $id")
        return try {
            val response = parameterService.deleteParameter(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Parameter $id deleted successfully")
                Resource.Success(Unit)
            } else {
                val error = ParameterErrorHandler.getErrorMessage(response)
                Log.e(TAG, "Delete failed: $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ParameterErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error deleting parameter: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error deleting parameter", e)
            Resource.Error(e.message ?: "Error eliminando parámetro")
        }
    }

    suspend fun toggleParameterStatus(id: Long): Resource<Unit> {
        Log.d(TAG, "Toggling parameter status: $id")
        return try {
            val response = parameterService.toggleParameterStatus(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Parameter $id status toggled")
                Resource.Success(Unit)
            } else {
                val error = ParameterErrorHandler.getErrorMessage(response)
                Log.e(TAG, "Toggle failed: $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ParameterErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error toggling status: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error toggling status", e)
            Resource.Error(e.message ?: "Error cambiando estado")
        }
    }

    suspend fun toggleFavorite(id: Long): Resource<Unit> {
        Log.d(TAG, "Toggling favorite: $id")
        return try {
            val response = parameterService.toggleFavorite(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Parameter $id favorite toggled")
                Resource.Success(Unit)
            } else {
                val error = ParameterErrorHandler.getErrorMessage(response)
                Log.e(TAG, "Toggle favorite failed: $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ParameterErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error toggling favorite: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error toggling favorite", e)
            Resource.Error(e.message ?: "Error cambiando favorito")
        }
    }

    suspend fun getParameterTypes(): Resource<List<String>> {
        Log.d(TAG, "Getting parameter types")
        return try {
            val response = parameterService.getParameterTypes()
            if (response.isSuccessful) {
                val body = response.body()
                val types = body?.takeIf { it.isNotEmpty() } ?: getDefaultParameterTypes()
                Log.i(TAG, "Parameter types retrieved: ${types.size}")
                Resource.Success(types)
            } else {
                Log.w(TAG, "Failed to get types, using defaults: ${response.code()}")
                Resource.Success(getDefaultParameterTypes())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting types, using defaults", e)
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
        Log.d(TAG, "Incrementing usage for parameter $id")
        return try {
            val response = parameterService.incrementParameterUsage(id)
            if (response.isSuccessful) {
                Log.i(TAG, "Usage incremented for parameter $id")
                Resource.Success(Unit)
            } else {
                Log.w(TAG, "Failed to increment usage: ${response.code()}")
                Resource.Error("Error incrementando uso")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing usage", e)
            Resource.Error(e.message ?: "Error incrementando uso")
        }
    }

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
                val error = ParameterErrorHandler.getErrorMessage(response)
                Log.e(TAG, "API call failed (${response.code()}): $error")
                Resource.Error(error)
            }
        } catch (e: IOException) {
            val error = ParameterErrorHandler.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error: $error", e)
            Resource.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Resource.Error(e.message ?: "Error desconocido")
        }
    }
}
package com.fitapp.appfit.repository

import com.fitapp.appfit.response.sport.SportRequest
import com.fitapp.appfit.response.sport.SportResponse
import com.fitapp.appfit.service.SportService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response

class SportRepository {
    private val sportService = SportService.instance

    suspend fun getSports(): Resource<List<SportResponse>> {
        return try {
            val response = sportService.getSports()
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error getting sports")
        }
    }

    suspend fun createCustomSport(sportRequest: SportRequest): Resource<SportResponse> {
        return try {
            val response = sportService.createCustomSport(sportRequest)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error creating sport")
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
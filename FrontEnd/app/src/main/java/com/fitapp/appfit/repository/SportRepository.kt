package com.fitapp.appfit.repository

import com.fitapp.appfit.response.page.PageResponse
import com.fitapp.appfit.response.sport.request.SportFilterRequest
import com.fitapp.appfit.response.sport.request.SportRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.service.SportService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response

class SportRepository {
    private val sportService = SportService.instance

    suspend fun getSports(): Resource<List<SportResponse>> {
        return handleResponse(sportService.getSports())
    }

    suspend fun getPredefinedSports(): Resource<List<SportResponse>> {
        return handleResponse(sportService.getPredefinedSports())
    }

    suspend fun getUserSports(): Resource<List<SportResponse>> {
        return handleResponse(sportService.getUserSports())
    }

    suspend fun createCustomSport(sportRequest: SportRequest): Resource<SportResponse> {
        return handleResponse(sportService.createCustomSport(sportRequest))
    }

    suspend fun deleteCustomSport(id: Long): Resource<Unit> {
        return try {
            val response = sportService.deleteCustomSport(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error deleting sport")
        }
    }

    suspend fun searchSports(filterRequest: SportFilterRequest): Resource<PageResponse<SportResponse>> {
        return handleResponse(sportService.searchSports(filterRequest))
    }

    suspend fun searchPredefinedSports(filterRequest: SportFilterRequest): Resource<PageResponse<SportResponse>> {
        return handleResponse(sportService.searchPredefinedSports(filterRequest))
    }

    suspend fun searchUserSports(filterRequest: SportFilterRequest): Resource<PageResponse<SportResponse>> {
        return handleResponse(sportService.searchUserSports(filterRequest))
    }

    suspend fun quickSearch(
        search: String?,
        category: String?,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        direction: String = "ASC"
    ): Resource<PageResponse<SportResponse>> {
        return handleResponse(
            sportService.quickSearch(search, category, page, size, sortBy, direction)
        )
    }

    suspend fun getCategories(): Resource<List<String>> {
        return handleResponse(sportService.getCategories())
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
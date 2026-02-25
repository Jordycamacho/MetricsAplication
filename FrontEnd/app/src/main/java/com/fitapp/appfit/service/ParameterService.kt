package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.request.CustomParameterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterPageResponse
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import retrofit2.Response
import retrofit2.http.*

interface ParameterService {

    @POST("api/parameters/search")
    suspend fun searchParameters(
        @Body filterRequest: CustomParameterFilterRequest
    ): Response<CustomParameterPageResponse>

    @POST("api/parameters/my/search")
    suspend fun searchMyParameters(
        @Body filterRequest: CustomParameterFilterRequest
    ): Response<CustomParameterPageResponse>

    @POST("api/parameters/available/{sportId}/search")
    suspend fun searchAvailableParameters(
        @Path("sportId") sportId: Long,
        @Body filterRequest: CustomParameterFilterRequest
    ): Response<CustomParameterPageResponse>

    @GET("api/parameters/{id}")
    suspend fun getParameterById(
        @Path("id") id: Long
    ): Response<CustomParameterResponse>

    @POST("api/parameters")
    suspend fun createParameter(
        @Body parameterRequest: CustomParameterRequest
    ): Response<CustomParameterResponse>

    @PUT("api/parameters/{id}")
    suspend fun updateParameter(
        @Path("id") id: Long,
        @Body parameterRequest: CustomParameterRequest
    ): Response<CustomParameterResponse>

    @DELETE("api/parameters/{id}")
    suspend fun deleteParameter(
        @Path("id") id: Long
    ): Response<Void>

    @PATCH("api/parameters/{id}/toggle")
    suspend fun toggleParameterStatus(
        @Path("id") id: Long
    ): Response<Void>

    @GET("api/parameters/categories")
    suspend fun getCategories(): Response<List<String>>

    @GET("api/parameters/types")
    suspend fun getParameterTypes(): Response<List<String>>

    @POST("api/parameters/{id}/increment-usage")
    suspend fun incrementParameterUsage(
        @Path("id") id: Long
    ): Response<Void>

    companion object {
        val instance: ParameterService by lazy {
            ApiClient.instance.create(ParameterService::class.java)
        }
    }
}
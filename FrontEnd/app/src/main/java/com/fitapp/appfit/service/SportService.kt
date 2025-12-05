package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.sport.SportRequest
import com.fitapp.appfit.response.sport.SportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SportService {
    @GET("api/sports")
    suspend fun getSports(): Response<List<SportResponse>>

    @GET("api/sports/predefined")
    suspend fun getPredefinedSports(): Response<List<SportResponse>>

    @GET("api/sports/custom")
    suspend fun getUserSports(): Response<List<SportResponse>>

    @POST("api/sports/custom")
    suspend fun createCustomSport(@Body sport: SportRequest): Response<SportResponse>

    @DELETE("api/sports/custom/{id}")
    suspend fun deleteCustomSport(@Path("id") id: Long): Response<Void>

    companion object {
        val instance: SportService by lazy {
            ApiClient.instance.create(SportService::class.java)
        }
    }
}
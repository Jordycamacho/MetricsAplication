package com.fitapp.appfit.feature.sport.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.sport.model.request.SportFilterRequest
import com.fitapp.appfit.feature.sport.model.request.SportRequest
import com.fitapp.appfit.feature.sport.model.response.SportResponse
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    // Búsqueda paginada con filtros
    @POST("api/sports/search")
    suspend fun searchSports(@Body filterRequest: SportFilterRequest): Response<PageResponse<SportResponse>>

    // Búsqueda paginada de predefinidos
    @POST("api/sports/predefined/search")
    suspend fun searchPredefinedSports(@Body filterRequest: SportFilterRequest): Response<PageResponse<SportResponse>>

    // Búsqueda paginada de personales
    @POST("api/sports/custom/search")
    suspend fun searchUserSports(@Body filterRequest: SportFilterRequest): Response<PageResponse<SportResponse>>

    // Búsqueda rápida (GET)
    @GET("api/sports/quick-search")
    suspend fun quickSearch(
        @Query("search") search: String?,
        @Query("category") category: String?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "name",
        @Query("direction") direction: String = "ASC"
    ): Response<PageResponse<SportResponse>>

    // Obtener categorías
    @GET("api/sports/categories")
    suspend fun getCategories(): Response<List<String>>

    companion object {
        val instance: SportService by lazy {
            ApiClient.instance.create(SportService::class.java)
        }
    }
}
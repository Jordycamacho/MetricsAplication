package com.fitapp.appfit.feature.exercise.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.exercise.model.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.feature.exercise.model.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.feature.exercise.model.category.response.ExerciseCategoryPageResponse
import com.fitapp.appfit.feature.exercise.model.category.response.ExerciseCategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ExerciseCategoryService {

    @POST("api/categories/search")
    suspend fun searchCategories(
        @Body filterRequest: ExerciseCategoryFilterRequest
    ): Response<ExerciseCategoryPageResponse>

    @POST("api/categories/my/search")
    suspend fun searchMyCategories(
        @Body filterRequest: ExerciseCategoryFilterRequest
    ): Response<ExerciseCategoryPageResponse>

    @POST("api/categories/available/{sportId}/search")
    suspend fun searchAvailableCategories(
        @Path("sportId") sportId: Long,
        @Body filterRequest: ExerciseCategoryFilterRequest
    ): Response<ExerciseCategoryPageResponse>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Long
    ): Response<ExerciseCategoryResponse>

    @POST("api/categories")
    suspend fun createCategory(
        @Body categoryRequest: ExerciseCategoryRequest
    ): Response<ExerciseCategoryResponse>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body categoryRequest: ExerciseCategoryRequest
    ): Response<ExerciseCategoryResponse>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/categories/check-name/{name}")
    suspend fun checkCategoryName(
        @Path("name") name: String
    ): Response<Boolean>

    companion object {
        val instance: ExerciseCategoryService by lazy {
            ApiClient.instance.create(ExerciseCategoryService::class.java)
        }
    }
}
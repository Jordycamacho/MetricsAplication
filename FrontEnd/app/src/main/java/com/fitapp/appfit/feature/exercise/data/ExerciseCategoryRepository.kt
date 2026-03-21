package com.fitapp.appfit.feature.exercise.data

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.exercise.model.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.feature.exercise.model.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.feature.exercise.model.category.response.ExerciseCategoryPageResponse
import com.fitapp.appfit.feature.exercise.model.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.feature.exercise.data.ExerciseCategoryService
import retrofit2.Response

class ExerciseCategoryRepository {
    private val categoryService = ExerciseCategoryService.Companion.instance

    suspend fun searchCategories(filterRequest: ExerciseCategoryFilterRequest): Resource<ExerciseCategoryPageResponse> {
        return handleResponse(categoryService.searchCategories(filterRequest))
    }

    suspend fun searchMyCategories(filterRequest: ExerciseCategoryFilterRequest): Resource<ExerciseCategoryPageResponse> {
        return handleResponse(categoryService.searchMyCategories(filterRequest))
    }

    suspend fun searchAvailableCategories(
        sportId: Long,
        filterRequest: ExerciseCategoryFilterRequest
    ): Resource<ExerciseCategoryPageResponse> {
        return handleResponse(categoryService.searchAvailableCategories(sportId, filterRequest))
    }

    suspend fun getCategoryById(id: Long): Resource<ExerciseCategoryResponse> {
        return handleResponse(categoryService.getCategoryById(id))
    }

    suspend fun createCategory(categoryRequest: ExerciseCategoryRequest): Resource<ExerciseCategoryResponse> {
        return handleResponse(categoryService.createCategory(categoryRequest))
    }

    suspend fun updateCategory(id: Long, categoryRequest: ExerciseCategoryRequest): Resource<ExerciseCategoryResponse> {
        return handleResponse(categoryService.updateCategory(id, categoryRequest))
    }

    suspend fun deleteCategory(id: Long): Resource<Unit> {
        return handleResponse(categoryService.deleteCategory(id))
    }

    suspend fun checkCategoryName(name: String): Resource<Boolean> {
        return handleResponse(categoryService.checkCategoryName(name))
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
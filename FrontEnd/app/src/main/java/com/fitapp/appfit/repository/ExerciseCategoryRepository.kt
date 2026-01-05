package com.fitapp.appfit.repository

import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.response.category.response.ExerciseCategoryPageResponse
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.service.ExerciseCategoryService
import com.fitapp.appfit.utils.Resource

class ExerciseCategoryRepository {
    private val categoryService = ExerciseCategoryService.instance

    // Búsqueda general
    suspend fun searchCategories(filterRequest: ExerciseCategoryFilterRequest): Resource<ExerciseCategoryPageResponse> {
        return handleResponse(categoryService.searchCategories(filterRequest))
    }

    // Mis categorías
    suspend fun searchMyCategories(filterRequest: ExerciseCategoryFilterRequest): Resource<ExerciseCategoryPageResponse> {
        return handleResponse(categoryService.searchMyCategories(filterRequest))
    }

    // Categorías disponibles para un deporte
    suspend fun searchAvailableCategories(
        sportId: Long,
        filterRequest: ExerciseCategoryFilterRequest
    ): Resource<ExerciseCategoryPageResponse> {
        return handleResponse(categoryService.searchAvailableCategories(sportId, filterRequest))
    }

    // Obtener categoría por ID
    suspend fun getCategoryById(id: Long): Resource<ExerciseCategoryResponse> {
        return handleResponse(categoryService.getCategoryById(id))
    }

    // Crear categoría
    suspend fun createCategory(categoryRequest: ExerciseCategoryRequest): Resource<ExerciseCategoryResponse> {
        return handleResponse(categoryService.createCategory(categoryRequest))
    }

    // Actualizar categoría
    suspend fun updateCategory(id: Long, categoryRequest: ExerciseCategoryRequest): Resource<ExerciseCategoryResponse> {
        return handleResponse(categoryService.updateCategory(id, categoryRequest))
    }

    // Eliminar categoría
    suspend fun deleteCategory(id: Long): Resource<Unit> {
        return handleResponse(categoryService.deleteCategory(id))
    }

    // Verificar nombre
    suspend fun checkCategoryName(name: String): Resource<Boolean> {
        return handleResponse(categoryService.checkCategoryName(name))
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
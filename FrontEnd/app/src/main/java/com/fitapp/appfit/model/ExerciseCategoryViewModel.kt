package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.ExerciseCategoryRepository
import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.category.request.ExerciseCategoryRequest
import com.fitapp.appfit.response.category.response.ExerciseCategoryPageResponse
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class ExerciseCategoryViewModel : ViewModel() {
    private val repository = ExerciseCategoryRepository()

    // Estados para listas paginadas
    private val _allCategoriesState = MutableLiveData<Resource<ExerciseCategoryPageResponse>?>()
    val allCategoriesState: LiveData<Resource<ExerciseCategoryPageResponse>?> = _allCategoriesState

    private val _myCategoriesState = MutableLiveData<Resource<ExerciseCategoryPageResponse>?>()
    val myCategoriesState: LiveData<Resource<ExerciseCategoryPageResponse>?> = _myCategoriesState

    private val _availableCategoriesState = MutableLiveData<Resource<ExerciseCategoryPageResponse>?>()
    val availableCategoriesState: LiveData<Resource<ExerciseCategoryPageResponse>?> = _availableCategoriesState

    // Estados para operaciones CRUD
    private val _categoryDetailState = MutableLiveData<Resource<ExerciseCategoryResponse>?>()
    val categoryDetailState: LiveData<Resource<ExerciseCategoryResponse>?> = _categoryDetailState

    private val _createCategoryState = MutableLiveData<Resource<ExerciseCategoryResponse>?>()
    val createCategoryState: LiveData<Resource<ExerciseCategoryResponse>?> = _createCategoryState

    private val _updateCategoryState = MutableLiveData<Resource<ExerciseCategoryResponse>?>()
    val updateCategoryState: LiveData<Resource<ExerciseCategoryResponse>?> = _updateCategoryState

    private val _deleteCategoryState = MutableLiveData<Resource<Unit>?>()
    val deleteCategoryState: LiveData<Resource<Unit>?> = _deleteCategoryState

    private val _checkNameState = MutableLiveData<Resource<Boolean>?>()
    val checkNameState: LiveData<Resource<Boolean>?> = _checkNameState

    private val _categoriesForSpinnerState = MutableLiveData<Resource<List<ExerciseCategoryResponse>>>()
    val categoriesForSpinnerState: LiveData<Resource<List<ExerciseCategoryResponse>>> = _categoriesForSpinnerState

    // ==================== MÉTODOS PARA BÚSQUEDA ====================

    fun searchAllCategories(filterRequest: ExerciseCategoryFilterRequest = ExerciseCategoryFilterRequest()) {
        _allCategoriesState.value = Resource.Loading()
        viewModelScope.launch {
            _allCategoriesState.value = repository.searchCategories(filterRequest)
        }
    }

    fun searchMyCategories(filterRequest: ExerciseCategoryFilterRequest = ExerciseCategoryFilterRequest()) {
        _myCategoriesState.value = Resource.Loading()
        viewModelScope.launch {
            _myCategoriesState.value = repository.searchMyCategories(filterRequest)
        }
    }

    fun searchAvailableCategories(sportId: Long, filterRequest: ExerciseCategoryFilterRequest = ExerciseCategoryFilterRequest()) {
        _availableCategoriesState.value = Resource.Loading()
        viewModelScope.launch {
            _availableCategoriesState.value = repository.searchAvailableCategories(sportId, filterRequest)
        }
    }

    // ==================== MÉTODOS CRUD ====================

    fun getCategoryById(id: Long) {
        _categoryDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _categoryDetailState.value = repository.getCategoryById(id)
        }
    }

    fun createCategory(categoryRequest: ExerciseCategoryRequest) {
        _createCategoryState.value = Resource.Loading()
        viewModelScope.launch {
            _createCategoryState.value = repository.createCategory(categoryRequest)
        }
    }

    fun updateCategory(id: Long, categoryRequest: ExerciseCategoryRequest) {
        _updateCategoryState.value = Resource.Loading()
        viewModelScope.launch {
            _updateCategoryState.value = repository.updateCategory(id, categoryRequest)
        }
    }

    fun deleteCategory(id: Long) {
        _deleteCategoryState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteCategoryState.value = repository.deleteCategory(id)
        }
    }

    fun checkCategoryName(name: String) {
        _checkNameState.value = Resource.Loading()
        viewModelScope.launch {
            _checkNameState.value = repository.checkCategoryName(name)
        }
    }


    // En tu ExerciseCategoryViewModel
    // En tu ExerciseCategoryViewModel
    fun loadCategoriesForSpinner(filterRequest: ExerciseCategoryFilterRequest) {
        _categoriesForSpinnerState.value = Resource.Loading()
        Log.d("CATEGORY_DEBUG", "Loading categories for spinner with filter: $filterRequest")
        viewModelScope.launch {
            val result = repository.searchMyCategories(filterRequest)
            Log.d("CATEGORY_DEBUG", "Repository result: $result")
            if (result is Resource.Success) {
                val categories = result.data?.content ?: emptyList()
                Log.d("CATEGORY_DEBUG", "Categories loaded: ${categories.size}")
                _categoriesForSpinnerState.value = Resource.Success(categories)
            } else {
                Log.e("CATEGORY_DEBUG", "Error loading categories: ${result.message}")
                _categoriesForSpinnerState.value = Resource.Error(result.message ?: "Error")
            }
        }
    }

    fun clearSpinnerState() {
        _categoriesForSpinnerState.value = Resource.Success(emptyList())
    }

    // ==================== LIMPIAR ESTADOS ====================

    fun clearCreateState() {
        _createCategoryState.value = null
    }

    fun clearUpdateState() {
        _updateCategoryState.value = null
    }

    fun clearDeleteState() {
        _deleteCategoryState.value = null
    }

    fun clearDetailState() {
        _categoryDetailState.value = null
    }

    fun clearCheckNameState() {
        _checkNameState.value = null
    }

    fun clearAllStates() {
        _allCategoriesState.value = null
        _myCategoriesState.value = null
        _availableCategoriesState.value = null
        _categoryDetailState.value = null
        _createCategoryState.value = null
        _updateCategoryState.value = null
        _deleteCategoryState.value = null
        _checkNameState.value = null
    }
}
package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.ParameterRepository
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.request.CustomParameterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterPageResponse
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class ParameterViewModel : ViewModel() {
    private val repository = ParameterRepository()
    private val _allParametersState = MutableLiveData<Resource<CustomParameterPageResponse>?>()
    val allParametersState: LiveData<Resource<CustomParameterPageResponse>?> = _allParametersState
    private val _myParametersState = MutableLiveData<Resource<CustomParameterPageResponse>?>()
    val myParametersState: LiveData<Resource<CustomParameterPageResponse>?> = _myParametersState
    private val _availableParametersState = MutableLiveData<Resource<CustomParameterPageResponse>?>()
    val availableParametersState: LiveData<Resource<CustomParameterPageResponse>?> = _availableParametersState
    private val _parameterDetailState = MutableLiveData<Resource<CustomParameterResponse>?>()
    val parameterDetailState: LiveData<Resource<CustomParameterResponse>?> = _parameterDetailState
    private val _createParameterState = MutableLiveData<Resource<CustomParameterResponse>?>()
    val createParameterState: LiveData<Resource<CustomParameterResponse>?> = _createParameterState
    private val _updateParameterState = MutableLiveData<Resource<CustomParameterResponse>?>()
    val updateParameterState: LiveData<Resource<CustomParameterResponse>?> = _updateParameterState
    private val _deleteParameterState = MutableLiveData<Resource<Unit>?>()
    val deleteParameterState: LiveData<Resource<Unit>?> = _deleteParameterState
    private val _toggleParameterState = MutableLiveData<Resource<Unit>?>()
    val toggleParameterState: LiveData<Resource<Unit>?> = _toggleParameterState
    private val _categoriesState = MutableLiveData<Resource<List<String>>?>()
    val categoriesState: LiveData<Resource<List<String>>?> = _categoriesState
    private val _parameterTypesState = MutableLiveData<Resource<List<String>>?>()
    val parameterTypesState: LiveData<Resource<List<String>>?> = _parameterTypesState

    // ==================== MÉTODOS PARA BÚSQUEDA ====================

    fun searchAllParameters(filterRequest: CustomParameterFilterRequest = CustomParameterFilterRequest()) {
        _allParametersState.value = Resource.Loading()
        viewModelScope.launch {
            _allParametersState.value = repository.searchParameters(filterRequest)
        }
    }

    fun searchMyParameters(filterRequest: CustomParameterFilterRequest = CustomParameterFilterRequest()) {
        _myParametersState.value = Resource.Loading()
        viewModelScope.launch {
            _myParametersState.value = repository.searchMyParameters(filterRequest)
        }
    }

    fun searchAvailableParameters(sportId: Long, filterRequest: CustomParameterFilterRequest = CustomParameterFilterRequest()) {
        _availableParametersState.value = Resource.Loading()
        viewModelScope.launch {
            _availableParametersState.value = repository.searchAvailableParameters(sportId, filterRequest)
        }
    }

    // ==================== MÉTODOS CRUD ====================

    fun getParameterById(id: Long) {
        _parameterDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _parameterDetailState.value = repository.getParameterById(id)
        }
    }

    fun createParameter(parameterRequest: CustomParameterRequest) {
        _createParameterState.value = Resource.Loading()
        viewModelScope.launch {
            _createParameterState.value = repository.createParameter(parameterRequest)
        }
    }

    fun updateParameter(id: Long, parameterRequest: CustomParameterRequest) {
        _updateParameterState.value = Resource.Loading()
        viewModelScope.launch {
            _updateParameterState.value = repository.updateParameter(id, parameterRequest)
        }
    }

    fun deleteParameter(id: Long) {
        _deleteParameterState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteParameterState.value = repository.deleteParameter(id)
        }
    }

    fun toggleParameterStatus(id: Long) {
        _toggleParameterState.value = Resource.Loading()
        viewModelScope.launch {
            _toggleParameterState.value = repository.toggleParameterStatus(id)
        }
    }

    fun incrementParameterUsage(id: Long) {
        viewModelScope.launch {
            repository.incrementParameterUsage(id)
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    fun getCategories() {
        _categoriesState.value = Resource.Loading()
        viewModelScope.launch {
            _categoriesState.value = repository.getCategories()
        }
    }

    fun getParameterTypes() {
        _parameterTypesState.value = Resource.Loading()
        Log.d("ParamViewModel", "Solicitando tipos de parámetros...")
        viewModelScope.launch {
            try {
                val result = repository.getParameterTypes()
                Log.d("ParamViewModel", "Resultado tipos: $result")
                _parameterTypesState.value = result
            } catch (e: Exception) {
                Log.e("ParamViewModel", "Error obteniendo tipos: ${e.message}")
                _parameterTypesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ==================== LIMPIAR ESTADOS ====================

    fun clearCreateState() {
        _createParameterState.value = null
    }

    fun clearUpdateState() {
        _updateParameterState.value = null
    }

    fun clearDeleteState() {
        _deleteParameterState.value = null
    }

    fun clearDetailState() {
        _parameterDetailState.value = null
    }

    fun clearAllStates() {
        _allParametersState.value = null
        _myParametersState.value = null
        _availableParametersState.value = null
        _parameterDetailState.value = null
        _createParameterState.value = null
        _updateParameterState.value = null
        _deleteParameterState.value = null
        _toggleParameterState.value = null
    }
}
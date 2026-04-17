package com.fitapp.appfit.feature.parameter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.parameter.data.ParameterRepository
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterFilterRequest
import com.fitapp.appfit.feature.parameter.model.request.CustomParameterRequest
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterPageResponse
import com.fitapp.appfit.feature.parameter.model.response.CustomParameterResponse
import kotlinx.coroutines.launch

class ParameterViewModel : ViewModel() {
    private val repository = ParameterRepository()
    private val TAG = "ParameterViewModel"

    // Estados para búsquedas
    private val _allParametersState = MutableLiveData<Resource<CustomParameterPageResponse>?>()
    val allParametersState: LiveData<Resource<CustomParameterPageResponse>?> = _allParametersState

    private val _myParametersState = MutableLiveData<Resource<CustomParameterPageResponse>?>()
    val myParametersState: LiveData<Resource<CustomParameterPageResponse>?> = _myParametersState

    private val _availableParametersState = MutableLiveData<Resource<CustomParameterPageResponse>?>()
    val availableParametersState: LiveData<Resource<CustomParameterPageResponse>?> = _availableParametersState

    // Estados CRUD
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

    private val _toggleFavoriteState = MutableLiveData<Resource<Unit>?>()
    val toggleFavoriteState: LiveData<Resource<Unit>?> = _toggleFavoriteState

    // Estados auxiliares
    private val _parameterTypesState = MutableLiveData<Resource<List<String>>?>()
    val parameterTypesState: LiveData<Resource<List<String>>?> = _parameterTypesState

    // ==================== MÉTODOS PARA BÚSQUEDA ====================

    fun searchAllParameters(filterRequest: CustomParameterFilterRequest = CustomParameterFilterRequest()) {
        Log.d(TAG, "searchAllParameters called")
        _allParametersState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _allParametersState.value = repository.searchParameters(filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchAllParameters", e)
                _allParametersState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun searchMyParameters(filterRequest: CustomParameterFilterRequest = CustomParameterFilterRequest()) {
        Log.d(TAG, "searchMyParameters called")
        _myParametersState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _myParametersState.value = repository.searchMyParameters(filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchMyParameters", e)
                _myParametersState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun searchAvailableParameters(
        sportId: Long,
        filterRequest: CustomParameterFilterRequest = CustomParameterFilterRequest()
    ) {
        Log.d(TAG, "searchAvailableParameters called for sport $sportId")
        _availableParametersState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _availableParametersState.value =
                    repository.searchAvailableParameters(sportId, filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchAvailableParameters", e)
                _availableParametersState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ==================== MÉTODOS CRUD ====================

    fun getParameterById(id: Long) {
        Log.d(TAG, "getParameterById called: $id")
        _parameterDetailState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _parameterDetailState.value = repository.getParameterById(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in getParameterById", e)
                _parameterDetailState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun createParameter(parameterRequest: CustomParameterRequest) {
        Log.d(TAG, "createParameter called: ${parameterRequest.name}")
        _createParameterState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _createParameterState.value = repository.createParameter(parameterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in createParameter", e)
                _createParameterState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateParameter(id: Long, parameterRequest: CustomParameterRequest) {
        Log.d(TAG, "updateParameter called: $id")
        _updateParameterState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _updateParameterState.value = repository.updateParameter(id, parameterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateParameter", e)
                _updateParameterState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteParameter(id: Long) {
        Log.d(TAG, "deleteParameter called: $id")
        _deleteParameterState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _deleteParameterState.value = repository.deleteParameter(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in deleteParameter", e)
                _deleteParameterState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun toggleParameterStatus(id: Long) {
        Log.d(TAG, "toggleParameterStatus called: $id")
        _toggleParameterState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _toggleParameterState.value = repository.toggleParameterStatus(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in toggleParameterStatus", e)
                _toggleParameterState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun toggleFavorite(id: Long) {
        Log.d(TAG, "toggleFavorite called: $id")
        _toggleFavoriteState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _toggleFavoriteState.value = repository.toggleFavorite(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in toggleFavorite", e)
                _toggleFavoriteState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun incrementParameterUsage(id: Long) {
        Log.d(TAG, "incrementParameterUsage called: $id")
        viewModelScope.launch {
            try {
                repository.incrementParameterUsage(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing usage (non-critical)", e)
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    fun getParameterTypes() {
        Log.d(TAG, "getParameterTypes called")
        _parameterTypesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _parameterTypesState.value = repository.getParameterTypes()
            } catch (e: Exception) {
                Log.e(TAG, "Error in getParameterTypes", e)
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

    fun clearToggleFavoriteState() {
        _toggleFavoriteState.value = null
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
        _toggleFavoriteState.value = null
    }
}
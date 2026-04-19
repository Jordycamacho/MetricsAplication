package com.fitapp.appfit.feature.exercise

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.exercise.data.ExerciseRepository
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.feature.exercise.model.exercise.request.ExerciseRequest
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExercisePageResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val repository = ExerciseRepository()
    private val TAG = "ExerciseViewModel"

    // Estados para búsquedas
    private val _allExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val allExercisesState: LiveData<Resource<ExercisePageResponse>?> = _allExercisesState

    private val _myExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val myExercisesState: LiveData<Resource<ExercisePageResponse>?> = _myExercisesState

    private val _availableExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val availableExercisesState: LiveData<Resource<ExercisePageResponse>?> = _availableExercisesState

    private val _exercisesBySportState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val exercisesBySportState: LiveData<Resource<ExercisePageResponse>?> = _exercisesBySportState

    // Estados CRUD
    private val _exerciseDetailState = MutableLiveData<Resource<ExerciseResponse>?>()
    val exerciseDetailState: LiveData<Resource<ExerciseResponse>?> = _exerciseDetailState

    private val _createExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val createExerciseState: LiveData<Resource<ExerciseResponse>?> = _createExerciseState

    private val _updateExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val updateExerciseState: LiveData<Resource<ExerciseResponse>?> = _updateExerciseState

    private val _deleteExerciseState = MutableLiveData<Resource<Unit>?>()
    val deleteExerciseState: LiveData<Resource<Unit>?> = _deleteExerciseState

    private val _toggleStatusState = MutableLiveData<Resource<Unit>?>()
    val toggleStatusState: LiveData<Resource<Unit>?> = _toggleStatusState

    // Estados de acciones especiales
    private val _rateExerciseState = MutableLiveData<Resource<Unit>?>()
    val rateExerciseState: LiveData<Resource<Unit>?> = _rateExerciseState

    private val _duplicateExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val duplicateExerciseState: LiveData<Resource<ExerciseResponse>?> = _duplicateExerciseState

    // Listas especiales
    private val _recentlyUsedState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val recentlyUsedState: LiveData<Resource<ExercisePageResponse>?> = _recentlyUsedState

    private val _mostPopularState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val mostPopularState: LiveData<Resource<ExercisePageResponse>?> = _mostPopularState

    private val _topRatedState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val topRatedState: LiveData<Resource<ExercisePageResponse>?> = _topRatedState

    private val _exerciseCountState = MutableLiveData<Resource<Long>?>()
    val exerciseCountState: LiveData<Resource<Long>?> = _exerciseCountState

    // ==================== BÚSQUEDAS ====================

    fun searchExercises(filterRequest: ExerciseFilterRequest = ExerciseFilterRequest()) {
        Log.d(TAG, "searchExercises called")
        _allExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _allExercisesState.value = repository.searchExercises(filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchExercises", e)
                _allExercisesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun searchMyExercises(filterRequest: ExerciseFilterRequest = ExerciseFilterRequest()) {
        Log.d(TAG, "searchMyExercises called")
        _myExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _myExercisesState.value = repository.searchMyExercises(filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchMyExercises", e)
                _myExercisesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun searchAvailableExercises(filterRequest: ExerciseFilterRequest = ExerciseFilterRequest()) {
        Log.d(TAG, "searchAvailableExercises called")
        _availableExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _availableExercisesState.value = repository.searchAvailableExercises(filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchAvailableExercises", e)
                _availableExercisesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun searchExercisesBySport(
        sportId: Long,
        filterRequest: ExerciseFilterRequest = ExerciseFilterRequest()
    ) {
        Log.d(TAG, "searchExercisesBySport called for sport $sportId")
        _exercisesBySportState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _exercisesBySportState.value = repository.searchExercisesBySport(sportId, filterRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in searchExercisesBySport", e)
                _exercisesBySportState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ==================== CRUD ====================

    fun getExerciseById(id: Long) {
        Log.d(TAG, "getExerciseById called: $id")
        _exerciseDetailState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _exerciseDetailState.value = repository.getExerciseById(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in getExerciseById", e)
                _exerciseDetailState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun createExercise(exerciseRequest: ExerciseRequest) {
        Log.d(TAG, "createExercise called: ${exerciseRequest.name}")
        _createExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _createExerciseState.value = repository.createExercise(exerciseRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in createExercise", e)
                _createExerciseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateExercise(id: Long, exerciseRequest: ExerciseRequest) {
        Log.d(TAG, "updateExercise called: $id")
        _updateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _updateExerciseState.value = repository.updateExercise(id, exerciseRequest)
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateExercise", e)
                _updateExerciseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteExercise(id: Long) {
        Log.d(TAG, "deleteExercise called: $id")
        _deleteExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _deleteExerciseState.value = repository.deleteExercise(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in deleteExercise", e)
                _deleteExerciseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun toggleExerciseStatus(id: Long) {
        Log.d(TAG, "toggleExerciseStatus called: $id")
        _toggleStatusState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _toggleStatusState.value = repository.toggleExerciseStatus(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in toggleExerciseStatus", e)
                _toggleStatusState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ==================== ACCIONES ESPECIALES ====================

    fun rateExercise(id: Long, rating: Double) {
        Log.d(TAG, "rateExercise called: $id with rating $rating")
        _rateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _rateExerciseState.value = repository.rateExercise(id, rating)
            } catch (e: Exception) {
                Log.e(TAG, "Error in rateExercise", e)
                _rateExerciseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun duplicateExercise(id: Long) {
        Log.d(TAG, "duplicateExercise called: $id")
        _duplicateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _duplicateExerciseState.value = repository.duplicateExercise(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error in duplicateExercise", e)
                _duplicateExerciseState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ==================== LISTAS ESPECIALES ====================

    fun getRecentlyUsedExercises(page: Int = 0, size: Int = 10) {
        Log.d(TAG, "getRecentlyUsedExercises called")
        _recentlyUsedState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _recentlyUsedState.value = repository.getRecentlyUsedExercises(page, size)
            } catch (e: Exception) {
                Log.e(TAG, "Error in getRecentlyUsedExercises", e)
                _recentlyUsedState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getMostPopularExercises(page: Int = 0, size: Int = 10) {
        Log.d(TAG, "getMostPopularExercises called")
        _mostPopularState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _mostPopularState.value = repository.getMostPopularExercises(page, size)
            } catch (e: Exception) {
                Log.e(TAG, "Error in getMostPopularExercises", e)
                _mostPopularState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getTopRatedExercises(page: Int = 0, size: Int = 10) {
        Log.d(TAG, "getTopRatedExercises called")
        _topRatedState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _topRatedState.value = repository.getTopRatedExercises(page, size)
            } catch (e: Exception) {
                Log.e(TAG, "Error in getTopRatedExercises", e)
                _topRatedState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getMyExerciseCount() {
        Log.d(TAG, "getMyExerciseCount called")
        _exerciseCountState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _exerciseCountState.value = repository.getMyExerciseCount()
            } catch (e: Exception) {
                Log.e(TAG, "Error in getMyExerciseCount", e)
                _exerciseCountState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ==================== LIMPIAR ESTADOS ====================

    fun clearCreateState() {
        _createExerciseState.value = null
    }

    fun clearUpdateState() {
        _updateExerciseState.value = null
    }

    fun clearDeleteState() {
        _deleteExerciseState.value = null
    }

    fun clearToggleState() {
        _toggleStatusState.value = null
    }

    fun clearDetailState() {
        _exerciseDetailState.value = null
    }

    fun clearRateState() {
        _rateExerciseState.value = null
    }

    fun clearDuplicateState() {
        _duplicateExerciseState.value = null
    }

    fun clearAllStates() {
        _allExercisesState.value = null
        _myExercisesState.value = null
        _availableExercisesState.value = null
        _exercisesBySportState.value = null
        _exerciseDetailState.value = null
        _createExerciseState.value = null
        _updateExerciseState.value = null
        _deleteExerciseState.value = null
        _toggleStatusState.value = null
        _rateExerciseState.value = null
        _duplicateExerciseState.value = null
        _recentlyUsedState.value = null
        _mostPopularState.value = null
        _topRatedState.value = null
        _exerciseCountState.value = null
    }
}
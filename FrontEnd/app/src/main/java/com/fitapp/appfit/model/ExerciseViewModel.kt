package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.ExerciseRepository
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExercisePageResponse
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val repository = ExerciseRepository()

    companion object {
        private const val TAG = "ExerciseViewModel"
    }

    // --- Listas ---
    private val _allExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val allExercisesState: LiveData<Resource<ExercisePageResponse>?> = _allExercisesState

    private val _myExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val myExercisesState: LiveData<Resource<ExercisePageResponse>?> = _myExercisesState

    private val _availableExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val availableExercisesState: LiveData<Resource<ExercisePageResponse>?> = _availableExercisesState

    private val _exercisesBySportState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val exercisesBySportState: LiveData<Resource<ExercisePageResponse>?> = _exercisesBySportState

    private val _recentlyUsedState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val recentlyUsedState: LiveData<Resource<ExercisePageResponse>?> = _recentlyUsedState

    private val _mostPopularState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val mostPopularState: LiveData<Resource<ExercisePageResponse>?> = _mostPopularState

    private val _topRatedState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val topRatedState: LiveData<Resource<ExercisePageResponse>?> = _topRatedState

    // --- CRUD ---
    private val _exerciseDetailState = MutableLiveData<Resource<ExerciseResponse>?>()
    val exerciseDetailState: LiveData<Resource<ExerciseResponse>?> = _exerciseDetailState

    private val _createExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val createExerciseState: LiveData<Resource<ExerciseResponse>?> = _createExerciseState

    private val _updateExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val updateExerciseState: LiveData<Resource<ExerciseResponse>?> = _updateExerciseState

    private val _deleteExerciseState = MutableLiveData<Resource<Void>?>()
    val deleteExerciseState: LiveData<Resource<Void>?> = _deleteExerciseState

    private val _toggleExerciseState = MutableLiveData<Resource<Void>?>()
    val toggleExerciseState: LiveData<Resource<Void>?> = _toggleExerciseState

    private val _rateExerciseState = MutableLiveData<Resource<Void>?>()
    val rateExerciseState: LiveData<Resource<Void>?> = _rateExerciseState

    private val _duplicateExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val duplicateExerciseState: LiveData<Resource<ExerciseResponse>?> = _duplicateExerciseState

    private val _makePublicState = MutableLiveData<Resource<ExerciseResponse>?>()
    val makePublicState: LiveData<Resource<ExerciseResponse>?> = _makePublicState

    private val _myExerciseCountState = MutableLiveData<Resource<Long>?>()
    val myExerciseCountState: LiveData<Resource<Long>?> = _myExerciseCountState

    // =========================================================
    // Queries
    // =========================================================

    fun searchExercises(filterRequest: ExerciseFilterRequest) {
        _allExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _allExercisesState.value = repository.searchExercises(filterRequest)
        }
    }

    fun searchMyExercises(filterRequest: ExerciseFilterRequest) {
        _myExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _myExercisesState.value = repository.searchMyExercises(filterRequest)
        }
    }

    fun searchAvailableExercises(filterRequest: ExerciseFilterRequest) {
        _availableExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _availableExercisesState.value = repository.searchAvailableExercises(filterRequest)
        }
    }

    fun searchExercisesBySport(sportId: Long, filterRequest: ExerciseFilterRequest) {
        _exercisesBySportState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesBySportState.value = repository.searchExercisesBySport(sportId, filterRequest)
        }
    }

    fun getExerciseById(id: Long) {
        Log.i(TAG, "getExerciseById: $id")
        _exerciseDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _exerciseDetailState.value = repository.getExerciseById(id)
        }
    }

    fun getRecentlyUsedExercises(page: Int = 0, size: Int = 10) {
        _recentlyUsedState.value = Resource.Loading()
        viewModelScope.launch {
            _recentlyUsedState.value = repository.getRecentlyUsedExercises(page, size)
        }
    }

    fun getMostPopularExercises(page: Int = 0, size: Int = 10) {
        _mostPopularState.value = Resource.Loading()
        viewModelScope.launch {
            _mostPopularState.value = repository.getMostPopularExercises(page, size)
        }
    }

    fun getTopRatedExercises(page: Int = 0, size: Int = 10) {
        _topRatedState.value = Resource.Loading()
        viewModelScope.launch {
            _topRatedState.value = repository.getTopRatedExercises(page, size)
        }
    }

    fun getMyExerciseCount() {
        _myExerciseCountState.value = Resource.Loading()
        viewModelScope.launch {
            _myExerciseCountState.value = repository.getMyExerciseCount()
        }
    }

    // =========================================================
    // Commands
    // =========================================================

    fun createExercise(exerciseRequest: ExerciseRequest) {
        _createExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _createExerciseState.value = repository.createExercise(exerciseRequest)
        }
    }

    fun updateExercise(id: Long, exerciseRequest: ExerciseRequest) {
        _updateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _updateExerciseState.value = repository.updateExercise(id, exerciseRequest)
        }
    }

    fun deleteExercise(id: Long) {
        _deleteExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteExerciseState.value = repository.deleteExercise(id)
        }
    }

    fun toggleExerciseStatus(id: Long) {
        _toggleExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _toggleExerciseState.value = repository.toggleExerciseStatus(id)
        }
    }

    // ✅ incrementExerciseUsage ELIMINADO — el back lo gestiona internamente

    fun rateExercise(id: Long, rating: Double) {
        _rateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _rateExerciseState.value = repository.rateExercise(id, rating)
        }
    }

    fun duplicateExercise(id: Long) {
        _duplicateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _duplicateExerciseState.value = repository.duplicateExercise(id)
        }
    }

    fun makeExercisePublic(id: Long) {
        _makePublicState.value = Resource.Loading()
        viewModelScope.launch {
            _makePublicState.value = repository.makeExercisePublic(id)
        }
    }

    // =========================================================
    // Clear states
    // =========================================================

    fun clearCreateState()      { _createExerciseState.value = null }
    fun clearUpdateState()      { _updateExerciseState.value = null }
    fun clearDeleteState()      { _deleteExerciseState.value = null }
    fun clearToggleState()      { _toggleExerciseState.value = null }
    fun clearRateState()        { _rateExerciseState.value = null }
    fun clearDuplicateState()   { _duplicateExerciseState.value = null }
    fun clearMakePublicState()  { _makePublicState.value = null }
    fun clearDetailState()      { _exerciseDetailState.value = null }
}
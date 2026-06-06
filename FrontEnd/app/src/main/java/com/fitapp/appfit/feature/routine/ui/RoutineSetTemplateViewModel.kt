package com.fitapp.appfit.feature.routine.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.data.RoutineRepository
import com.fitapp.appfit.feature.routine.data.RoutineSetTemplateRepository
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setemplate.request.CreateSetTemplateRequest
import com.fitapp.appfit.feature.routine.model.setemplate.request.UpdateSetTemplateRequest
import kotlinx.coroutines.launch

class RoutineSetTemplateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineSetTemplateRepository()
    private val routineRepository = RoutineRepository(application)

    // ── Estados ──────────────────────────────────────────────────────────────

    private val _setCountState = MutableLiveData<Int?>()
    val setCountState: LiveData<Int?> = _setCountState

    private val _setsState = MutableLiveData<Resource<List<RoutineSetTemplateResponse>>>()
    val setsState: LiveData<Resource<List<RoutineSetTemplateResponse>>> = _setsState

    private val _setDetailState = MutableLiveData<Resource<RoutineSetTemplateResponse>?>()
    val setDetailState: LiveData<Resource<RoutineSetTemplateResponse>?> = _setDetailState

    private val _saveState = MutableLiveData<Resource<RoutineSetTemplateResponse>?>()
    val saveState: LiveData<Resource<RoutineSetTemplateResponse>?> = _saveState

    private val _deleteState = MutableLiveData<Resource<Unit>?>()
    val deleteState: LiveData<Resource<Unit>?> = _deleteState

    private val _deleteAllState = MutableLiveData<Resource<Unit>?>()
    val deleteAllState: LiveData<Resource<Unit>?> = _deleteAllState

    // ── Operaciones ───────────────────────────────────────────────────────────

    fun loadSets(routineExerciseId: Long) {
        _setsState.value = Resource.Loading()
        viewModelScope.launch {
            _setsState.value = repository.getSetTemplatesByRoutineExercise(routineExerciseId)
        }
    }

    fun loadSetCountForExercise(routineExerciseId: Long) {
        viewModelScope.launch {
            val result = repository.getSetTemplatesByRoutineExercise(routineExerciseId)
            _setCountState.value = when (result) {
                is Resource.Success -> result.data?.size ?: 0
                else -> 0
            }
        }
    }

    fun loadSetDetail(id: Long) {
        _setDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _setDetailState.value = repository.getSetTemplate(id)
        }
    }

    fun createSet(routineId: Long, request: CreateSetTemplateRequest) {
        _saveState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.createSetTemplate(request)
            if (result is Resource.Success) {
                invalidateTrainingCache(routineId)
            }
            _saveState.value = result
        }
    }

    fun updateSet(routineId: Long, id: Long, request: UpdateSetTemplateRequest) {
        _saveState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.updateSetTemplate(id, request)
            if (result is Resource.Success) {
                invalidateTrainingCache(routineId)
            }
            _saveState.value = result
        }
    }

    fun deleteSet(routineId: Long, id: Long) {
        _deleteState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.deleteSetTemplate(id)
            if (result is Resource.Success) {
                invalidateTrainingCache(routineId)
            }
            _deleteState.value = result
        }
    }

    fun deleteAllSets(routineId: Long, routineExerciseId: Long) {
        _deleteAllState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.deleteSetTemplatesByRoutineExercise(routineExerciseId)
            if (result is Resource.Success) {
                invalidateTrainingCache(routineId)
            }
            _deleteAllState.value = result
        }
    }

    private suspend fun invalidateTrainingCache(routineId: Long) {
        routineRepository.markTrainingCacheStale(routineId)
        routineRepository.refreshTrainingCache(routineId)
    }

    // ── Limpiar one-shots ─────────────────────────────────────────────────────

    fun clearSaveState()      { _saveState.value      = null }
    fun clearDeleteState()    { _deleteState.value    = null }
    fun clearDeleteAllState() { _deleteAllState.value = null }
    fun clearDetailState()    { _setDetailState.value = null }
}

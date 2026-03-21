package com.fitapp.appfit.feature.routine.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.data.RoutineSetTemplateRepository
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setemplate.request.CreateSetTemplateRequest
import com.fitapp.appfit.feature.routine.model.setemplate.request.UpdateSetTemplateRequest
import kotlinx.coroutines.launch

class RoutineSetTemplateViewModel : ViewModel() {

    private val repository = RoutineSetTemplateRepository()

    // ── Estados ──────────────────────────────────────────────────────────────

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

    fun loadSetDetail(id: Long) {
        _setDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _setDetailState.value = repository.getSetTemplate(id)
        }
    }

    fun createSet(request: CreateSetTemplateRequest) {
        _saveState.value = Resource.Loading()
        viewModelScope.launch {
            _saveState.value = repository.createSetTemplate(request)
        }
    }

    fun updateSet(id: Long, request: UpdateSetTemplateRequest) {
        _saveState.value = Resource.Loading()
        viewModelScope.launch {
            _saveState.value = repository.updateSetTemplate(id, request)
        }
    }

    fun deleteSet(id: Long) {
        _deleteState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteState.value = repository.deleteSetTemplate(id)
        }
    }

    fun deleteAllSets(routineExerciseId: Long) {
        _deleteAllState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteAllState.value = repository.deleteSetTemplatesByRoutineExercise(routineExerciseId)
        }
    }

    // ── Limpiar one-shots ─────────────────────────────────────────────────────

    fun clearSaveState() { _saveState.value = null }
    fun clearDeleteState() { _deleteState.value = null }
    fun clearDeleteAllState() { _deleteAllState.value = null }
    fun clearDetailState() { _setDetailState.value = null }
}
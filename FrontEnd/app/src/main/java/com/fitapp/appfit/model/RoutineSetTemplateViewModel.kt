package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.RoutineSetTemplateRepository
import com.fitapp.appfit.response.routine.response.RoutineSetTemplateResponse
import com.fitapp.appfit.response.sets.request.CreateSetTemplateRequest
import com.fitapp.appfit.response.sets.request.UpdateSetTemplateRequest
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class RoutineSetTemplateViewModel : ViewModel() {
    private val repository = RoutineSetTemplateRepository()

    companion object {
        private const val TAG = "SetTemplateViewModel"
    }

    // Estados
    private val _createSetTemplateState = MutableLiveData<Resource<RoutineSetTemplateResponse>?>()
    val createSetTemplateState: LiveData<Resource<RoutineSetTemplateResponse>?> = _createSetTemplateState

    private val _updateSetTemplateState = MutableLiveData<Resource<RoutineSetTemplateResponse>?>()
    val updateSetTemplateState: LiveData<Resource<RoutineSetTemplateResponse>?> = _updateSetTemplateState

    private val _getSetTemplateState = MutableLiveData<Resource<RoutineSetTemplateResponse>?>()
    val getSetTemplateState: LiveData<Resource<RoutineSetTemplateResponse>?> = _getSetTemplateState

    private val _getSetTemplatesByExerciseState = MutableLiveData<Resource<List<RoutineSetTemplateResponse>>?>()
    val getSetTemplatesByExerciseState: LiveData<Resource<List<RoutineSetTemplateResponse>>?> = _getSetTemplatesByExerciseState

    private val _deleteSetTemplateState = MutableLiveData<Resource<Unit>?>()
    val deleteSetTemplateState: LiveData<Resource<Unit>?> = _deleteSetTemplateState

    private val _deleteSetTemplatesByExerciseState = MutableLiveData<Resource<Unit>?>() // NUEVO: Estado para eliminación múltiple
    val deleteSetTemplatesByExerciseState: LiveData<Resource<Unit>?> = _deleteSetTemplatesByExerciseState

    private val _reorderSetTemplatesState = MutableLiveData<Resource<RoutineSetTemplateResponse>?>()
    val reorderSetTemplatesState: LiveData<Resource<RoutineSetTemplateResponse>?> = _reorderSetTemplatesState

    // Operaciones
    fun createSetTemplate(request: CreateSetTemplateRequest) {
        Log.i(TAG, "createSetTemplate: Creando set template")
        _createSetTemplateState.value = Resource.Loading()
        viewModelScope.launch {
            _createSetTemplateState.value = repository.createSetTemplate(request)
        }
    }

    fun updateSetTemplate(id: Long, request: UpdateSetTemplateRequest) {
        Log.i(TAG, "updateSetTemplate: Actualizando set template $id")
        _updateSetTemplateState.value = Resource.Loading()
        viewModelScope.launch {
            _updateSetTemplateState.value = repository.updateSetTemplate(id, request)
        }
    }

    fun getSetTemplate(id: Long) {
        Log.i(TAG, "getSetTemplate: Obteniendo set template $id")
        _getSetTemplateState.value = Resource.Loading()
        viewModelScope.launch {
            _getSetTemplateState.value = repository.getSetTemplate(id)
        }
    }

    fun getSetTemplatesByRoutineExercise(routineExerciseId: Long) {
        Log.i(TAG, "getSetTemplatesByRoutineExercise: Obteniendo sets para ejercicio $routineExerciseId")
        _getSetTemplatesByExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _getSetTemplatesByExerciseState.value = repository.getSetTemplatesByRoutineExercise(routineExerciseId)
        }
    }

    fun deleteSetTemplate(id: Long) {
        Log.i(TAG, "deleteSetTemplate: Eliminando set template $id")
        _deleteSetTemplateState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteSetTemplateState.value = repository.deleteSetTemplate(id)
        }
    }

    // NUEVO: Método para eliminar todos los sets de un ejercicio
    fun deleteSetTemplatesByRoutineExercise(routineExerciseId: Long) {
        Log.i(TAG, "deleteSetTemplatesByRoutineExercise: Eliminando sets del ejercicio $routineExerciseId")
        _deleteSetTemplatesByExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteSetTemplatesByExerciseState.value = repository.deleteSetTemplatesByRoutineExercise(routineExerciseId)
        }
    }

    fun reorderSetTemplates(routineExerciseId: Long, setTemplateIds: List<Long>) {
        Log.i(TAG, "reorderSetTemplates: Reordenando sets para ejercicio $routineExerciseId")
        _reorderSetTemplatesState.value = Resource.Loading()
        viewModelScope.launch {
            _reorderSetTemplatesState.value = repository.reorderSetTemplates(routineExerciseId, setTemplateIds)
        }
    }

    fun clearCreateState() {
        _createSetTemplateState.value = null
    }

    fun clearUpdateState() {
        _updateSetTemplateState.value = null
    }

    fun clearGetState() {
        _getSetTemplateState.value = null
    }

    fun clearDeleteState() {
        _deleteSetTemplateState.value = null
    }

    fun clearDeleteByExerciseState() {
        _deleteSetTemplatesByExerciseState.value = null
    }

    fun clearReorderState() {
        _reorderSetTemplatesState.value = null
    }
}
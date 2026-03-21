package com.fitapp.appfit.feature.routine.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.data.RoutineExerciseRepository
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseResponse
import kotlinx.coroutines.launch

class RoutineExerciseViewModel : ViewModel() {

    private val repository = RoutineExerciseRepository()

    // ── Estados ──────────────────────────────────────────────────────────────

    private val _exercisesState = MutableLiveData<Resource<List<RoutineExerciseResponse>>>()
    val exercisesState: LiveData<Resource<List<RoutineExerciseResponse>>> = _exercisesState

    private val _addExerciseState = MutableLiveData<Resource<RoutineExerciseResponse>?>()
    val addExerciseState: LiveData<Resource<RoutineExerciseResponse>?> = _addExerciseState

    private val _updateExerciseState = MutableLiveData<Resource<RoutineExerciseResponse>?>()
    val updateExerciseState: LiveData<Resource<RoutineExerciseResponse>?> = _updateExerciseState

    private val _deleteState = MutableLiveData<Resource<Unit>?>()
    val deleteState: LiveData<Resource<Unit>?> = _deleteState

    private val _reorderState = MutableLiveData<Resource<Unit>?>()
    val reorderState: LiveData<Resource<Unit>?> = _reorderState

    // ── Carga de ejercicios ───────────────────────────────────────────────────

    fun loadRoutineExercises(routineId: Long) {
        _exercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesState.value = repository.getRoutineExercises(routineId)
        }
    }

    // ── Añadir ejercicio(s) ───────────────────────────────────────────────────

    fun addExerciseToRoutine(routineId: Long, request: AddExerciseToRoutineRequest) {
        _addExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _addExerciseState.value = repository.addExerciseToRoutine(routineId, request)
        }
    }

    // ── Editar ────────────────────────────────────────────────────────────────

    fun updateExerciseInRoutine(
        routineId: Long,
        exerciseId: Long,
        request: AddExerciseToRoutineRequest
    ) {
        _updateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _updateExerciseState.value = repository.updateExerciseInRoutine(routineId, exerciseId, request)
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    fun deleteExercise(routineId: Long, exerciseId: Long) {
        _deleteState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.removeExerciseFromRoutine(routineId, exerciseId)
            _deleteState.value = result
            if (result is Resource.Success) {
                loadRoutineExercises(routineId)
            }
        }
    }

    // ── Reordenar ─────────────────────────────────────────────────────────────

    fun reorderExercises(routineId: Long, exerciseIds: List<Long>) {
        _reorderState.value = Resource.Loading()
        viewModelScope.launch {
            _reorderState.value = repository.reorderExercises(routineId, exerciseIds)
        }
    }

    // ── Limpiar estados one-shot ──────────────────────────────────────────────

    fun clearAddState() { _addExerciseState.value = null }
    fun clearUpdateState() { _updateExerciseState.value = null }
    fun clearDeleteState() { _deleteState.value = null }
    fun clearReorderState() { _reorderState.value = null }
}
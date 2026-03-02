package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.RoutineExerciseRepository
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.routine.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.utils.Resource
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

    /**
     * Agrega múltiples ejercicios secuencialmente.
     * En caso de fallo parcial notifica error pero continúa con el resto.
     * Al terminar recarga la lista de ejercicios de la rutina.
     */
    fun addMultipleExercisesToRoutine(
        routineId: Long,
        exercises: List<Pair<ExerciseResponse, AddExerciseToRoutineRequest>>
    ) {
        _addExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            var lastSuccess: RoutineExerciseResponse? = null
            var errorMessage: String? = null

            for ((exercise, request) in exercises) {
                when (val result = repository.addExerciseToRoutine(routineId, request)) {
                    is Resource.Success -> lastSuccess = result.data
                    is Resource.Error -> errorMessage = "Error en ${exercise.name}: ${result.message}"
                    else -> {}
                }
            }

            if (errorMessage != null) {
                _addExerciseState.value = Resource.Error(errorMessage!!)
            } else if (lastSuccess != null) {
                _addExerciseState.value = Resource.Success(lastSuccess!!)
            }

            // Siempre recargar la lista tras añadir múltiples
            loadRoutineExercises(routineId)
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
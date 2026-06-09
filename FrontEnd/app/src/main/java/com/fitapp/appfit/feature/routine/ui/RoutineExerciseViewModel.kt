package com.fitapp.appfit.feature.routine.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.data.RoutineExerciseRepository
import com.fitapp.appfit.feature.routine.data.RoutineRepository
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.ReorderSessionExercisesRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import kotlinx.coroutines.launch

class RoutineExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineExerciseRepository()
    private val routineRepository = RoutineRepository(application)

    private val _exercisesState = MutableLiveData<Resource<List<RoutineExerciseResponse>>>()
    val exercisesState: LiveData<Resource<List<RoutineExerciseResponse>>> = _exercisesState

    private val _orderBaselineState = MutableLiveData<Map<String, Int>>()
    val orderBaselineState: LiveData<Map<String, Int>> = _orderBaselineState

    private val _saveState = MutableLiveData<Resource<RoutineExerciseResponse>?>()
    val saveState: LiveData<Resource<RoutineExerciseResponse>?> = _saveState

    private val _deleteState = MutableLiveData<Resource<Unit>?>()
    val deleteState: LiveData<Resource<Unit>?> = _deleteState

    private val _reorderState = MutableLiveData<Resource<Unit>?>()
    val reorderState: LiveData<Resource<Unit>?> = _reorderState

    fun loadRoutineExercises(routineId: Long) {
        _exercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesState.value = repository.getRoutineExercises(routineId)
        }
    }

    fun loadOrderBaseline(routineId: Long) {
        viewModelScope.launch {
            when (val result = repository.getRoutineExercises(routineId)) {
                is Resource.Success -> {
                    val baseline = result.data.orEmpty()
                        .filter { !it.dayOfWeek.isNullOrBlank() }
                        .groupBy { it.dayOfWeek!! }
                        .mapValues { (_, exercises) ->
                            (exercises.maxOfOrNull { it.sessionOrder ?: 0 } ?: 0) + 1
                        }
                    _orderBaselineState.value = baseline
                }
                else -> _orderBaselineState.value = emptyMap()
            }
        }
    }

    fun saveExercise(
        routineId: Long,
        routineExerciseId: Long?,
        request: AddExerciseToRoutineRequest
    ) {
        _saveState.value = Resource.Loading()
        viewModelScope.launch {
            val result = if (routineExerciseId == null) {
                repository.addExerciseToRoutine(routineId, request)
            } else {
                repository.updateExerciseInRoutine(routineId, routineExerciseId, request)
            }
            if (result is Resource.Success) {
                invalidateTrainingCache(routineId)
            }
            _saveState.value = result
        }
    }

    fun deleteExercise(routineId: Long, routineExerciseId: Long) {
        _deleteState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.removeExerciseFromRoutine(routineId, routineExerciseId)
            _deleteState.value = result
            if (result is Resource.Success) {
                invalidateTrainingCache(routineId)
                loadRoutineExercises(routineId)
            }
        }
    }

    fun reorderSessionGroups(routineId: Long, groups: List<ReorderSessionExercisesRequest>) {
        if (groups.isEmpty()) return
        _reorderState.value = Resource.Loading()
        viewModelScope.launch {
            for (group in groups) {
                when (val result = repository.reorderSessionExercises(routineId, group)) {
                    is Resource.Error -> {
                        _reorderState.value = result
                        return@launch
                    }
                    else -> Unit
                }
            }
            routineRepository.markTrainingCacheStale(routineId)
            routineRepository.refreshTrainingCache(routineId)
            _reorderState.value = Resource.Success(Unit)
            loadRoutineExercises(routineId)
        }
    }

    private suspend fun invalidateTrainingCache(routineId: Long) {
        routineRepository.markTrainingCacheStale(routineId)
        routineRepository.refreshTrainingCache(routineId)
    }

    fun clearSaveState() { _saveState.value = null }
    fun clearDeleteState() { _deleteState.value = null }
    fun clearReorderState() { _reorderState.value = null }
}

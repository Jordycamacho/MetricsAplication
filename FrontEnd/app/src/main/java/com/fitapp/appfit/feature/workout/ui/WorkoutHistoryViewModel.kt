package com.fitapp.appfit.feature.workout.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.data.WorkoutRepository
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de historial de workouts.
 */
class WorkoutHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(application)

    private val _workoutHistoryState = MutableLiveData<Resource<PageResponse<WorkoutSessionSummaryResponse>>>()
    val workoutHistoryState: LiveData<Resource<PageResponse<WorkoutSessionSummaryResponse>>> = _workoutHistoryState

    private val _totalVolumeState = MutableLiveData<Resource<Double>>()
    val totalVolumeState: LiveData<Resource<Double>> = _totalVolumeState

    private val _deleteState = MutableLiveData<Resource<Unit>>()
    val deleteState: LiveData<Resource<Unit>> = _deleteState

    /**
     * Carga el historial de workouts.
     */
    fun loadWorkoutHistory(
        routineId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ) {
        _workoutHistoryState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getWorkoutHistory(
                routineId = routineId,
                page = page,
                size = size
            )
            _workoutHistoryState.postValue(result)
        }
    }

    /**
     * Carga las sesiones recientes.
     */
    fun loadRecentWorkouts(limit: Int = 10) {
        _workoutHistoryState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getRecentWorkouts(limit)
            _workoutHistoryState.postValue(result)
        }
    }

    /**
     * Carga el volumen total acumulado.
     */
    fun loadTotalVolume() {
        _totalVolumeState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getTotalVolume()
            _totalVolumeState.postValue(result)
        }
    }

    /**
     * Elimina una sesión de workout.
     */
    fun deleteWorkoutSession(sessionId: Long) {
        _deleteState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.deleteWorkoutSession(sessionId)
            _deleteState.postValue(result)
        }
    }
}
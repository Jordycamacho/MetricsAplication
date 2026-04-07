package com.fitapp.appfit.feature.workout.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.data.WorkoutRepository
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import kotlinx.coroutines.launch

class WorkoutSessionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(application)

    private val _sessionDetailState = MutableLiveData<Resource<WorkoutSessionResponse>>()
    val sessionDetailState: LiveData<Resource<WorkoutSessionResponse>> = _sessionDetailState

    private val _comparisonState = MutableLiveData<SessionComparison?>()
    val comparisonState: LiveData<SessionComparison?> = _comparisonState

    /**
     * Carga los detalles de una sesión y automáticamente busca la anterior para comparar.
     */
    fun loadSessionDetails(sessionId: Long) {
        _sessionDetailState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getWorkoutSessionDetails(sessionId)
            _sessionDetailState.postValue(result)

            // Si se cargó exitosamente, buscar sesión anterior para comparar
            if (result is Resource.Success) {
                result.data?.let { session ->
                    loadPreviousSessionForComparison(session.routineId, sessionId)
                }
            }
        }
    }

    /**
     * Busca la sesión anterior de la misma rutina para hacer comparación.
     */
    private suspend fun loadPreviousSessionForComparison(routineId: Long, currentSessionId: Long) {
        val historyResult = repository.getWorkoutHistory(
            routineId = routineId,
            page = 0,
            size = 10
        )

        if (historyResult is Resource.Success) {
            val sessions = historyResult.data?.content ?: emptyList()

            // Buscar la sesión inmediatamente anterior
            val previousSession = sessions
                .filter { it.id != currentSessionId }
                .maxByOrNull { it.id } // Asumiendo que IDs mayores = más recientes

            if (previousSession != null) {
                val current = _sessionDetailState.value?.data
                if (current != null) {
                    val comparison = SessionComparison(
                        previousSessionDate = previousSession.startTime,
                        volumeDifference = (current.totalVolume ?: 0.0) - (previousSession.totalVolume ?: 0.0),
                        durationDifference = (current.durationSeconds ?: 0L) - (previousSession.durationSeconds ?: 0L),
                        setsDifference = calculateSetsDifference(current, previousSession.setCount)
                    )
                    _comparisonState.postValue(comparison)
                }
            }
        }
    }

    private fun calculateSetsDifference(
        current: WorkoutSessionResponse,
        previousSetCount: Int
    ): Int {
        val currentSets = current.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0
        return currentSets - previousSetCount
    }

    /**
     * Elimina una sesión.
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutSession(sessionId)
        }
    }
}
package com.fitapp.appfit.feature.workout.ui

import android.app.Application
import android.util.Log
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

    companion object {
        private const val TAG = "WorkoutSessionDetailVM"
    }

    fun loadSessionDetails(sessionId: Long) {
        Log.i(TAG, "LOAD_SESSION_DETAILS | sessionId=$sessionId")
        _sessionDetailState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getWorkoutSessionDetails(sessionId)
            _sessionDetailState.postValue(result)

            if (result is Resource.Success) {
                result.data?.let { session ->
                    Log.i(TAG, "SESSION_LOADED | routineId=${session.routineId} | startTime=${session.startTime}")
                    loadPreviousSessionForComparison(session.routineId, session.startTime)
                }
            } else if (result is Resource.Error) {
                Log.e(TAG, "SESSION_LOAD_ERROR | error=${result.message}")
            }
        }
    }

    private suspend fun loadPreviousSessionForComparison(
        routineId: Long,
        currentStartTime: String
    ) {
        Log.i(TAG, "LOAD_COMPARISON | routineId=$routineId | currentStartTime=$currentStartTime")

        val historyResult = repository.getWorkoutHistory(
            routineId = routineId,
            page = 0,
            size = 20
        )

        if (historyResult is Resource.Success) {
            val sessions = historyResult.data?.content ?: emptyList()
            Log.i(TAG, "HISTORY_LOADED | totalSessions=${sessions.size}")

            val previousSession = sessions
                .filter { it.startTime < currentStartTime }
                .maxByOrNull { it.startTime }

            if (previousSession != null) {
                Log.i(TAG, "PREVIOUS_SESSION_FOUND | sessionId=${previousSession.id} | startTime=${previousSession.startTime}")

                val current = _sessionDetailState.value?.data
                if (current != null) {
                    val currentSets = current.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0
                    val previousSets = previousSession.setCount

                    val volumeDiff = (current.totalVolume ?: 0.0) - (previousSession.totalVolume ?: 0.0)
                    val durationDiff = (current.durationSeconds ?: 0L) - (previousSession.durationSeconds ?: 0L)
                    val setsDiff = currentSets - previousSets

                    Log.i(TAG, "COMPARISON_CALC | volumeDiff=$volumeDiff | durationDiff=$durationDiff | setsDiff=$setsDiff")

                    val comparison = SessionComparison(
                        previousSessionDate = previousSession.startTime,
                        volumeDifference = volumeDiff,
                        durationDifference = durationDiff,
                        setsDifference = setsDiff
                    )
                    _comparisonState.postValue(comparison)
                    Log.i(TAG, "COMPARISON_POSTED")
                }
            } else {
                Log.w(TAG, "NO_PREVIOUS_SESSION_FOUND")
                _comparisonState.postValue(null)
            }
        } else {
            Log.e(TAG, "HISTORY_LOAD_ERROR | error=${(historyResult as? Resource.Error)?.message}")
        }
    }

    fun deleteSession(sessionId: Long) {
        Log.i(TAG, "DELETE_SESSION | sessionId=$sessionId")
        viewModelScope.launch {
            repository.deleteWorkoutSession(sessionId)
        }
    }
}
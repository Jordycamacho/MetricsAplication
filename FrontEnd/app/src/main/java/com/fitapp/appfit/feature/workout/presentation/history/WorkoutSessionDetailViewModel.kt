package com.fitapp.appfit.feature.workout.presentation.history

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WorkoutSessionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepositoryImpl(application)

    private val _sessionDetailState = MutableLiveData<Resource<WorkoutSessionResponse>>()
    val sessionDetailState: LiveData<Resource<WorkoutSessionResponse>> = _sessionDetailState

    private val _comparisonState = MutableLiveData<SessionComparison?>()
    val comparisonState: LiveData<SessionComparison?> = _comparisonState

    // ✅ AÑADIDO: LiveData para los ejercicios de la sesión anterior (set-by-set)
    private val _previousExercisesState = MutableLiveData<List<SessionExerciseResponse>>()
    val previousExercisesState: LiveData<List<SessionExerciseResponse>> = _previousExercisesState

    companion object {
        private const val TAG = "WorkoutSessionDetailVM"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    fun loadSessionDetails(sessionId: Long) {
        Log.i(TAG, "LOAD_SESSION_DETAILS | sessionId=$sessionId")
        _sessionDetailState.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getWorkoutSessionDetails(sessionId)
            _sessionDetailState.postValue(result)

            if (result is Resource.Success) {
                result.data?.let { session ->
                    loadPreviousSessionForComparison(session.routineId, session.startTime)
                }
            }
        }
    }

    private suspend fun loadPreviousSessionForComparison(
        routineId: Long,
        currentStartTimeIso: String
    ) {
        Log.i(TAG, "LOAD_COMPARISON | routineId=$routineId | currentStart=$currentStartTimeIso")

        val currentStart = parseIso(currentStartTimeIso) ?: run {
            Log.w(TAG, "COULD_NOT_PARSE_CURRENT_START_TIME | value=$currentStartTimeIso")
            _comparisonState.postValue(null)
            _previousExercisesState.postValue(emptyList())
            return
        }

        val historyResult = repository.getWorkoutHistory(routineId = routineId, page = 0, size = 50)

        if (historyResult !is Resource.Success) {
            Log.e(TAG, "HISTORY_LOAD_ERROR | error=${(historyResult as? Resource.Error)?.message}")
            _comparisonState.postValue(null)
            _previousExercisesState.postValue(emptyList())
            return
        }

        val sessions = historyResult.data?.content ?: emptyList()
        Log.i(TAG, "HISTORY_LOADED | totalSessions=${sessions.size}")

        val previousSummary = sessions
            .mapNotNull { summary ->
                val parsedStart = parseIso(summary.startTime) ?: return@mapNotNull null
                summary to parsedStart
            }
            .filter { (_, startTime) -> startTime.isBefore(currentStart) }
            .maxByOrNull { (_, startTime) -> startTime }
            ?.first

        if (previousSummary == null) {
            Log.w(TAG, "NO_PREVIOUS_SESSION_FOUND")
            _comparisonState.postValue(null)
            _previousExercisesState.postValue(emptyList())
            return
        }

        Log.i(TAG, "PREVIOUS_SESSION_FOUND | sessionId=${previousSummary.id} | startTime=${previousSummary.startTime}")

        // Summary-level deltas
        val current = _sessionDetailState.value?.data ?: run {
            _comparisonState.postValue(null)
            _previousExercisesState.postValue(emptyList())
            return
        }

        val currentSetCount = current.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0
        val volumeDiff = (current.totalVolume ?: 0.0) - (previousSummary.totalVolume ?: 0.0)
        val durationDiff = (current.durationSeconds ?: 0L) - (previousSummary.durationSeconds ?: 0L)
        val setsDiff = currentSetCount - previousSummary.setCount

        Log.i(TAG, "COMPARISON | volumeDiff=$volumeDiff | durationDiff=$durationDiff | setsDiff=$setsDiff")

        _comparisonState.postValue(
            SessionComparison(
                previousSessionDate = previousSummary.startTime,
                volumeDifference = volumeDiff,
                durationDifference = durationDiff,
                setsDifference = setsDiff
            )
        )

        // ✅ Cargar detalles completos de la sesión anterior para comparativa set por set
        val prevDetailResult = repository.getWorkoutSessionDetails(previousSummary.id)
        if (prevDetailResult is Resource.Success) {
            val prevExercises = prevDetailResult.data?.exercises ?: emptyList()
            Log.i(TAG, "PREVIOUS_EXERCISES_LOADED | count=${prevExercises.size}")
            _previousExercisesState.postValue(prevExercises)
        } else {
            Log.w(TAG, "PREVIOUS_DETAIL_FAILED | no set-level comparison available")
            _previousExercisesState.postValue(emptyList())
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch { repository.deleteWorkoutSession(sessionId) }
    }

    private fun parseIso(isoDate: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(isoDate, ISO_FORMATTER)
        } catch (e: Exception) {
            Log.w(TAG, "PARSE_DATE_FAILED | value=$isoDate | error=${e.message}")
            null
        }
    }
}
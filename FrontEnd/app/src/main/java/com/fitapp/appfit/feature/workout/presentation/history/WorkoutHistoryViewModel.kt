package com.fitapp.appfit.feature.workout.presentation.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import kotlinx.coroutines.launch

class WorkoutHistoryViewModel(
    private val workoutRepositoryImpl: WorkoutRepositoryImpl
) : ViewModel() {

    companion object {
        private const val TAG = "WorkoutHistoryViewModel"
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private val _workoutHistoryState =
        MutableLiveData<Resource<PageResponse<WorkoutSessionSummaryResponse>>>()
    val workoutHistoryState: LiveData<Resource<PageResponse<WorkoutSessionSummaryResponse>>> = _workoutHistoryState

    private val _totalVolumeState = MutableLiveData<Resource<Double>>()
    val totalVolumeState: LiveData<Resource<Double>> = _totalVolumeState

    private val _deleteState = MutableLiveData<Resource<Unit>>()
    val deleteState: LiveData<Resource<Unit>> = _deleteState

    // ── Actions ───────────────────────────────────────────────────────────────

    fun loadWorkoutHistory(
        routineId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ) {
        Log.i(TAG, "LOAD_WORKOUT_HISTORY | routineId=$routineId | page=$page | size=$size")

        viewModelScope.launch {
            _workoutHistoryState.value = Resource.Loading()

            val result = workoutRepositoryImpl.getWorkoutHistory(
                routineId = routineId,
                page = page,
                size = size
            )

            when (result) {
                is Resource.Success -> {
                    Log.i(TAG, "✅ HISTORY_LOADED | count=${result.data?.content?.size}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ HISTORY_ERROR | error=${result.message}")
                }
                else -> {}
            }

            _workoutHistoryState.value = result
        }
    }

    fun loadTotalVolume() {
        Log.i(TAG, "LOAD_TOTAL_VOLUME")

        viewModelScope.launch {
            _totalVolumeState.value = Resource.Loading()

            val result = workoutRepositoryImpl.getTotalVolume()

            when (result) {
                is Resource.Success -> {
                    Log.i(TAG, "✅ TOTAL_VOLUME_LOADED | volume=${result.data}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ TOTAL_VOLUME_ERROR | error=${result.message}")
                }
                else -> {}
            }

            _totalVolumeState.value = result
        }
    }

    fun deleteWorkoutSession(sessionId: Long) {
        Log.i(TAG, "DELETE_WORKOUT_SESSION | sessionId=$sessionId")

        viewModelScope.launch {
            _deleteState.value = Resource.Loading()

            val result = workoutRepositoryImpl.deleteWorkoutSession(sessionId)

            when (result) {
                is Resource.Success -> {
                    Log.i(TAG, "✅ SESSION_DELETED | sessionId=$sessionId")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ DELETE_ERROR | sessionId=$sessionId | error=${result.message}")
                }
                else -> {}
            }

            _deleteState.value = result
        }
    }
}
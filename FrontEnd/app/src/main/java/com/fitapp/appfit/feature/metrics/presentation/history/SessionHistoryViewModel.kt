package com.fitapp.appfit.feature.metrics.presentation.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.metrics.data.MetricsReadRepositoryImpl
import com.fitapp.appfit.feature.metrics.domain.model.SessionHistoryFilter
import com.fitapp.appfit.feature.metrics.domain.usecase.DeleteSessionUseCase
import com.fitapp.appfit.feature.metrics.domain.usecase.GetSessionHistoryUseCase
import com.fitapp.appfit.feature.metrics.domain.usecase.GetTotalVolumeUseCase
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import kotlinx.coroutines.launch

class SessionHistoryViewModel(
    private val getHistory: GetSessionHistoryUseCase,
    private val getTotalVolume: GetTotalVolumeUseCase,
    private val deleteSession: DeleteSessionUseCase
) : ViewModel() {

    private var currentFilter = SessionHistoryFilter()

    private val _historyState = MutableLiveData<Resource<PageResponse<WorkoutSessionSummaryResponse>>>()
    val historyState: LiveData<Resource<PageResponse<WorkoutSessionSummaryResponse>>> = _historyState

    private val _totalVolumeState = MutableLiveData<Resource<Double>>()
    val totalVolumeState: LiveData<Resource<Double>> = _totalVolumeState

    private val _deleteState = MutableLiveData<Resource<Unit>>()
    val deleteState: LiveData<Resource<Unit>> = _deleteState

    fun loadHistory(filter: SessionHistoryFilter = currentFilter) {
        currentFilter = filter
        viewModelScope.launch {
            _historyState.value = Resource.Loading()
            _historyState.value = getHistory(filter)
        }
    }

    fun loadTotalVolume() {
        viewModelScope.launch {
            _totalVolumeState.value = Resource.Loading()
            _totalVolumeState.value = getTotalVolume()
        }
    }

    fun deleteWorkoutSession(sessionId: Long) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            val result = deleteSession(sessionId)
            _deleteState.value = result
            if (result is Resource.Success) loadHistory()
        }
    }

    companion object {
        fun create(context: android.content.Context): SessionHistoryViewModel {
            val repo = MetricsReadRepositoryImpl(context)
            return SessionHistoryViewModel(
                GetSessionHistoryUseCase(repo),
                GetTotalVolumeUseCase(repo),
                DeleteSessionUseCase(repo)
            )
        }
    }
}

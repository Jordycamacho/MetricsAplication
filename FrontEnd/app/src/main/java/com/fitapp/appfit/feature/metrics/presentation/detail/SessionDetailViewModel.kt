package com.fitapp.appfit.feature.metrics.presentation.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.metrics.data.MetricsReadRepositoryImpl
import com.fitapp.appfit.feature.metrics.domain.model.SessionComparison
import com.fitapp.appfit.feature.metrics.domain.usecase.CompareSessionsUseCase
import com.fitapp.appfit.feature.metrics.domain.usecase.GetSessionDetailUseCase
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import kotlinx.coroutines.launch

class SessionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = MetricsReadRepositoryImpl(application)
    private val getDetail = GetSessionDetailUseCase(repo)
    private val compareSessions = CompareSessionsUseCase(repo)

    private val _sessionState = MutableLiveData<Resource<WorkoutSessionResponse>>()
    val sessionState: LiveData<Resource<WorkoutSessionResponse>> = _sessionState

    private val _comparisonState = MutableLiveData<SessionComparison?>()
    val comparisonState: LiveData<SessionComparison?> = _comparisonState

    private val _previousExercisesState = MutableLiveData<List<SessionExerciseResponse>>()
    val previousExercisesState: LiveData<List<SessionExerciseResponse>> = _previousExercisesState

    fun loadSessionDetails(sessionId: Long) {
        _sessionState.value = Resource.Loading()
        viewModelScope.launch {
            val result = getDetail(sessionId)
            _sessionState.postValue(result)
            if (result is Resource.Success) {
                result.data?.let { session ->
                    val (comparison, prevExercises) = compareSessions(session)
                    _comparisonState.postValue(comparison)
                    _previousExercisesState.postValue(prevExercises)
                }
            }
        }
    }
}

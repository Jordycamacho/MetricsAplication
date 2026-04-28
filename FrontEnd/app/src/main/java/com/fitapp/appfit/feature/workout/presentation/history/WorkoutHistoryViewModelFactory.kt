package com.fitapp.appfit.feature.workout.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl

class WorkoutHistoryViewModelFactory(
    private val workoutRepositoryImpl: WorkoutRepositoryImpl
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutHistoryViewModel::class.java)) {
            return WorkoutHistoryViewModel(workoutRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
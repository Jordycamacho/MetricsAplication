package com.fitapp.appfit.feature.workout.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitapp.appfit.feature.workout.data.WorkoutRepository

class WorkoutHistoryViewModelFactory(
    private val workoutRepository: WorkoutRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutHistoryViewModel::class.java)) {
            return WorkoutHistoryViewModel(workoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
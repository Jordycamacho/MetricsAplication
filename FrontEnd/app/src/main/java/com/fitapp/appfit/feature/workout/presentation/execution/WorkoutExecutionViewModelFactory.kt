package com.fitapp.appfit.feature.workout.presentation.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitapp.appfit.feature.workout.data.repository.SaveLastExecutionValuesHelper
import com.fitapp.appfit.feature.workout.domain.usecase.LoadLocalLastExecutionValuesUseCase
import com.fitapp.appfit.feature.workout.domain.usecase.SaveWorkoutSessionUseCase
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache

class WorkoutExecutionViewModelFactory(
    private val saveWorkoutSessionUseCase: SaveWorkoutSessionUseCase,
    private val loadLocalLastExecutionValuesUseCase: LoadLocalLastExecutionValuesUseCase,
    private val saveLastExecutionValuesHelper: SaveLastExecutionValuesHelper,
    private val activeWorkoutCache: ActiveWorkoutCache
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutExecutionViewModel::class.java)) {
            return WorkoutExecutionViewModel(
                saveWorkoutSessionUseCase,
                loadLocalLastExecutionValuesUseCase,
                saveLastExecutionValuesHelper,
                activeWorkoutCache
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

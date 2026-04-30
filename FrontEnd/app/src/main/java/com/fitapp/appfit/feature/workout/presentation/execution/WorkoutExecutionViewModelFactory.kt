package com.fitapp.appfit.feature.workout.presentation.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fitapp.appfit.feature.workout.domain.manager.LastWorkoutValuesApplier
import com.fitapp.appfit.feature.workout.domain.usecase.LoadLastExerciseValuesUseCase
import com.fitapp.appfit.feature.workout.domain.usecase.SaveWorkoutSessionUseCase
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache

class WorkoutExecutionViewModelFactory(
    private val saveWorkoutSessionUseCase: SaveWorkoutSessionUseCase,
    private val loadLastExerciseValuesUseCase: LoadLastExerciseValuesUseCase,
    private val lastWorkoutValuesApplier: LastWorkoutValuesApplier,
    private val activeWorkoutCache: ActiveWorkoutCache
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutExecutionViewModel::class.java)) {
            return WorkoutExecutionViewModel(
                saveWorkoutSessionUseCase,
                loadLastExerciseValuesUseCase,
                lastWorkoutValuesApplier,
                activeWorkoutCache
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
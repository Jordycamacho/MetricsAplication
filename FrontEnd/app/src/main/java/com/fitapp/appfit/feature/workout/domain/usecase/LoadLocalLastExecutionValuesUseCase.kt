package com.fitapp.appfit.feature.workout.domain.usecase

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.workout.data.repository.LocalLastExecutionValuesHelper

/**
 * Carga los últimos valores de ejecución DESDE SQLITE LOCAL.
 * NO hace consultas al servidor.
 *
 * Reemplaza a LoadLastExerciseValuesUseCase que hacía consultas API.
 */
class LoadLocalLastExecutionValuesUseCase(
    private val localHelper: LocalLastExecutionValuesHelper
) {

    companion object {
        private const val TAG = "LoadLocalLastExecValuesUseCase"
    }

    /**
     * Carga la rutina y aplica los últimos valores históricos desde SQLite.
     */
    suspend operator fun invoke(
        routine: RoutineResponse
    ): RoutineWithLocalHistory {
        Log.i(TAG, "LOADING_LOCAL_VALUES | routineId=${routine.id}")

        return try {
            val hasHistory = localHelper.hasLocalHistory(routine.id)
            if (!hasHistory) {
                Log.d(TAG, "NO_LOCAL_HISTORY_FOUND | routineId=${routine.id}")
                return RoutineWithLocalHistory(routine, appliedLocalHistory = false)
            }

            val lastWorkoutTime = localHelper.getLastWorkoutTime(routine.id)
            Log.i(TAG, "LOCAL_HISTORY_FOUND | lastWorkout=${lastWorkoutTime ?: "unknown"}")

            val routineWithValues = localHelper.applyLastValuesToRoutine(routine)
            Log.i(TAG, "LOCAL_VALUES_APPLIED | routineId=${routine.id}")
            RoutineWithLocalHistory(routineWithValues, appliedLocalHistory = true)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_LOADING_LOCAL_VALUES | error=${e.message}", e)
            RoutineWithLocalHistory(routine, appliedLocalHistory = false)
        }
    }
}
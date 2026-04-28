package com.fitapp.appfit.feature.workout.domain.usecase

import android.util.Log
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.workout.domain.repository.IWorkoutRepository
import com.fitapp.appfit.feature.workout.model.response.LastExerciseValuesResponse

class LoadLastExerciseValuesUseCase(
    private val workoutRepository: IWorkoutRepository
) {

    companion object {
        private const val TAG = "LoadLastExerciseValuesUseCase"
    }

    suspend operator fun invoke(
        routineId: Long
    ): Resource<Map<Long, LastExerciseValuesResponse>> {
        Log.i(TAG, "LOAD_LAST_VALUES_INVOKED | routineId=$routineId")

        return try {
            val result = workoutRepository.getLastValuesForRoutine(routineId)

            when (result) {
                is Resource.Success -> {
                    val data = result.data ?: emptyMap()
                    Log.i(TAG, "VALUES_LOADED | exerciseCount=${data.size}")
                    Resource.Success(data)
                }
                is Resource.Error -> {
                    Log.w(TAG, "LOAD_ERROR | error=${result.message}")
                    result
                }
                else -> {
                    Log.d(TAG, "LOADING_STATE")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "EXCEPTION_DURING_LOAD | error=${e.message}", e)
            Resource.Error(e.message ?: "Error desconocido")
        }
    }
}
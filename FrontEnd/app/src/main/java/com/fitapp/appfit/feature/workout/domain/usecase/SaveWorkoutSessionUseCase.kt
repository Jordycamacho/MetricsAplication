package com.fitapp.appfit.feature.workout.domain.usecase

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.workout.data.repository.SaveLastExecutionValuesHelper
import com.fitapp.appfit.feature.workout.domain.repository.IWorkoutRepository

class SaveWorkoutSessionUseCase(
    private val workoutRepository: IWorkoutRepository,
    private val saveLastExecutionValuesHelper: SaveLastExecutionValuesHelper
) {

    companion object {
        private const val TAG = "SaveWorkoutSessionUseCase"
    }

    suspend operator fun invoke(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int? = null,
        setTemplateResponses: Map<Long, RoutineSetTemplateResponse> = emptyMap(),
        dayOfWeek: String? = null,
        sessionNumber: Int? = null
    ): Result<Long> {
        Log.i(TAG, "SAVE_WORKOUT_INVOKED | routineId=$routineId | userId=$userId")

        if (setCompletionState.isEmpty()) {
            Log.w(TAG, "NO_SETS_TO_SAVE")
            return Result.failure(Exception("No hay sets para guardar"))
        }

        if (setParamState.isEmpty()) {
            Log.w(TAG, "NO_PARAMETERS_PROVIDED")
            return Result.failure(Exception("Sin datos de parámetros"))
        }

        if (startedAt >= finishedAt) {
            Log.w(TAG, "INVALID_TIMESTAMPS")
            return Result.failure(Exception("Timestamps inválidos"))
        }

        return try {
            // Persist last execution values locally first — independent of server sync.
            try {
                saveLastExecutionValuesHelper.saveExecutedValues(
                    routineId,
                    setParamState,
                    setTemplateResponses
                )
                Log.i(
                    TAG,
                    "EXECUTION_VALUES_SAVED_TO_LOCAL_DB | routineId=$routineId | sets=${setParamState.size}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "ERROR_SAVING_TO_LOCAL_DB | ${e.message}", e)
            }

            val result = workoutRepository.saveWorkoutSession(
                routineId = routineId,
                userId = userId,
                setParamState = setParamState,
                setCompletionState = setCompletionState,
                startedAt = startedAt,
                finishedAt = finishedAt,
                performanceScore = performanceScore,
                dayOfWeek = dayOfWeek,
                sessionNumber = sessionNumber
            )

            when {
                result.isSuccess -> Log.i(TAG, "SAVE_SUCCESS | sessionId=${result.getOrNull()}")
                else -> Log.e(TAG, "SAVE_FAILED | error=${result.exceptionOrNull()?.message}")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "EXCEPTION_DURING_SAVE | error=${e.message}", e)
            Result.failure(e)
        }
    }
}

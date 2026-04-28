package com.fitapp.appfit.feature.workout.domain.usecase

import android.util.Log
import com.fitapp.appfit.feature.workout.domain.repository.IWorkoutRepository
import com.fitapp.appfit.feature.workout.model.request.SaveWorkoutSessionRequest


class SaveWorkoutSessionUseCase(
    private val workoutRepository: IWorkoutRepository
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
        performanceScore: Int? = null
    ): Result<Long> {
        Log.i(TAG, "SAVE_WORKOUT_INVOKED | routineId=$routineId | userId=$userId")

        // Validar entrada
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
            workoutRepository.saveWorkoutSession(
                routineId = routineId,
                userId = userId,
                setParamState = setParamState,
                setCompletionState = setCompletionState,
                startedAt = startedAt,
                finishedAt = finishedAt,
                performanceScore = performanceScore
            ).also {
                when {
                    it.isSuccess -> Log.i(TAG, "SAVE_SUCCESS | sessionId=${it.getOrNull()}")
                    else -> Log.e(TAG, "SAVE_FAILED | error=${it.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "EXCEPTION_DURING_SAVE | error=${e.message}", e)
            Result.failure(e)
        }
    }
}
package com.fitapp.appfit.feature.profile.util

import android.content.Context
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.feature.routine.util.TrainingCachePreferences
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalCacheCleaner {

    sealed class Result {
        data object Success : Result()
        data object ActiveWorkoutInProgress : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun clearRoutineCache(context: Context): Result = withContext(Dispatchers.IO) {
        if (ActiveWorkoutCache(context).getActiveRoutineIdOrNull() != null) {
            return@withContext Result.ActiveWorkoutInProgress
        }

        return@withContext try {
            val db = AppDatabase.getInstance(context)
            db.routineDao().deleteAllCachedRoutines()
            TrainingCachePreferences.clearAll(context)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al limpiar caché")
        }
    }
}

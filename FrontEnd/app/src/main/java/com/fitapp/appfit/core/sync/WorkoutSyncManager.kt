package com.fitapp.appfit.core.sync

import android.content.Context
import android.util.Log
import com.fitapp.appfit.feature.workout.data.WorkoutRepository

class WorkoutSyncManager(context: Context) {

    private val workoutRepository = WorkoutRepository(context)

    suspend fun syncPendingWorkouts(): Int {
        Log.i("WorkoutSyncManager", "Iniciando sync manual de workouts pendientes")
        return workoutRepository.syncAllPendingSessions()
    }
}
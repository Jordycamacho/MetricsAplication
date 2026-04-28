package com.fitapp.appfit.core.sync

import android.content.Context
import android.util.Log
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl

class WorkoutSyncManager(context: Context) {

    private val workoutRepositoryImpl = WorkoutRepositoryImpl(context)

    suspend fun syncPendingWorkouts(): Int {
        Log.i("WorkoutSyncManager", "Iniciando sync manual de workouts pendientes")
        return workoutRepositoryImpl.syncAllPendingSessions()
    }
}
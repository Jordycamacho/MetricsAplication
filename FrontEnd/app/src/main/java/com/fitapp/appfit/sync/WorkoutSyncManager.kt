package com.fitapp.appfit.sync

import android.content.Context
import android.util.Log
import com.fitapp.appfit.repository.WorkoutRepository

/**
 * Wrapper opcional sobre WorkoutRepository.syncAllPendingSessions().
 *
 * Si ya usas SyncWorker, este manager es redundante — el Worker llama
 * syncAllPendingSessions() directamente. Puedes borrar este archivo.
 *
 * Úsalo solo si necesitas disparar el sync manualmente desde un punto
 * concreto de la app sin pasar por WorkManager.
 *
 * Debe llamarse desde una coroutine:
 *
 *   lifecycleScope.launch {
 *       WorkoutSyncManager(applicationContext).syncPendingWorkouts()
 *   }
 */
class WorkoutSyncManager(context: Context) {

    private val workoutRepository = WorkoutRepository(context)

    suspend fun syncPendingWorkouts(): Int {
        Log.i("WorkoutSyncManager", "Iniciando sync manual de workouts pendientes")
        return workoutRepository.syncAllPendingSessions()
    }
}
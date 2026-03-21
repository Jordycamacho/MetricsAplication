package com.fitapp.appfit.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.database.dao.PendingSyncOperation
import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.workout.data.WorkoutRepository
import com.fitapp.appfit.feature.routine.model.rutine.request.CreateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.request.UpdateRoutineRequest
import com.google.gson.Gson
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = AppDatabase.Companion.getInstance(context)
    private val syncDao = db.pendingSyncDao()
    private val routineDao = db.routineDao()
    private val gson = Gson()
    private val networkChecker = NetworkChecker(context)
    private val workoutRepository = WorkoutRepository(context)

    override suspend fun doWork(): Result {
        if (!networkChecker.isOnline()) {
            Timber.Forest.d("SyncWorker: sin conexión, reintentando más tarde")
            return Result.retry()
        }

        Timber.Forest.i("SyncWorker: iniciando sincronización")

        val routineSuccess = processRoutineQueue()

        val workoutSynced = workoutRepository.syncAllPendingSessions()
        Timber.Forest.i("SyncWorker: $workoutSynced sesiones de workout sincronizadas")

        return if (routineSuccess) Result.success() else Result.retry()
    }

    // ── Cola de rutinas ───────────────────────────────────────────────────────

    private suspend fun processRoutineQueue(): Boolean {
        val pending = syncDao.getAllPending()

        if (pending.isEmpty()) {
            Timber.Forest.i("SyncWorker: nada en la cola de rutinas")
            return true
        }

        var allSuccess = true

        for (operation in pending) {
            if (operation.retryCount >= 3) {
                Timber.Forest.w("SyncWorker: operación ${operation.operationId} superó reintentos, saltando")
                continue
            }

            val success = processOperation(operation)
            if (!success) {
                allSuccess = false
                syncDao.incrementRetry(
                    id = operation.operationId,
                    error = "Falló en intento ${operation.retryCount + 1}"
                )
            }
        }

        return allSuccess
    }

    private suspend fun processOperation(op: PendingSyncOperation): Boolean {
        return try {
            when (op.entityType) {
                "ROUTINE"          -> processRoutineOperation(op)
                "ROUTINE_EXERCISE" -> processRoutineExerciseOperation(op)
                else -> {
                    Timber.Forest.w("Tipo de entidad desconocido: ${op.entityType}")
                    syncDao.delete(op)
                    true
                }
            }
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error procesando operación ${op.operationId}")
            false
        }
    }

    private suspend fun processRoutineOperation(op: PendingSyncOperation): Boolean {
        val routineService = ApiClient.routineService

        return when (op.operation) {
            "CREATE" -> {
                val request = gson.fromJson(op.payload, CreateRoutineRequest::class.java)
                val response = routineService.createRoutine(request)
                if (response.isSuccessful) {
                    val serverId = response.body()?.id ?: return false
                    routineDao.updateServerId(localId = op.entityId, serverId = serverId)
                    syncDao.delete(op)
                    Timber.Forest.i("Rutina creada en servidor: $serverId")
                    true
                } else {
                    Timber.Forest.w("Error creando rutina: ${response.code()}")
                    false
                }
            }

            "UPDATE" -> {
                val request = gson.fromJson(op.payload, UpdateRoutineRequest::class.java)
                val response = routineService.updateRoutine(op.entityId, request)
                if (response.isSuccessful) {
                    routineDao.markAsSynced(op.entityId)
                    syncDao.delete(op)
                    Timber.Forest.i("Rutina actualizada en servidor: ${op.entityId}")
                    true
                } else {
                    Timber.Forest.w("Error actualizando rutina: ${response.code()}")
                    false
                }
            }

            "DELETE" -> {
                val response = routineService.deleteRoutine(op.entityId)
                if (response.isSuccessful || response.code() == 404) {
                    routineDao.deleteRoutine(op.entityId)
                    syncDao.delete(op)
                    Timber.Forest.i("Rutina eliminada en servidor: ${op.entityId}")
                    true
                } else {
                    Timber.Forest.w("Error eliminando rutina: ${response.code()}")
                    false
                }
            }

            else -> {
                syncDao.delete(op)
                true
            }
        }
    }

    private suspend fun processRoutineExerciseOperation(op: PendingSyncOperation): Boolean {
        syncDao.delete(op)
        return true
    }

    // ── Scheduling ────────────────────────────────────────────────────────────

    companion object {
        private const val WORK_NAME = "fitapp_sync_work"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME}_immediate",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            Timber.Forest.i("Sincronización inmediata programada")
        }
    }
}
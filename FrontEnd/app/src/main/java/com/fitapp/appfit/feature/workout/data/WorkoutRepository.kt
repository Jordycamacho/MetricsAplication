package com.fitapp.appfit.feature.workout.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.sync.NetworkChecker
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.database.entity.WorkoutSetResultEntity
import com.fitapp.appfit.shared.enums.SyncStatus
import com.fitapp.appfit.feature.routine.model.setemplate.request.BulkUpdateSetParametersRequest
import com.fitapp.appfit.feature.routine.data.RoutineSetTemplateService

class WorkoutRepository(private val context: Context) {

    private val db = AppDatabase.Companion.getInstance(context)
    private val sessionDao = db.workoutSessionDao()
    private val resultDao = db.workoutSetResultDao()
    private val service = RoutineSetTemplateService.Companion.instance
    private val networkChecker = NetworkChecker(context)

    // ── API pública ───────────────────────────────────────────────────────────
    suspend fun saveWorkout(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<Long, Map<String, Any?>>>,
        startedAt: Long = System.currentTimeMillis()
    ): Result<Long> {

        val session = WorkoutSessionEntity(
            routineId = routineId,
            userId = userId,
            startedAt = startedAt,
            finishedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_CREATE
        )

        val sessionId = try {
            sessionDao.insertSession(session)
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error guardando sesión local: ${e.message}", e)
            return Result.failure(e)
        }

        val results = buildSetResults(sessionId, setParamState)
        try {
            resultDao.insertResults(results)
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error guardando resultados locales: ${e.message}", e)
            sessionDao.deleteSession(sessionId)
            return Result.failure(e)
        }

        if (networkChecker.isOnline()) {
            syncSession(sessionId)
        } else {
            Log.i("WorkoutRepository", "Sin conexión — sesión $sessionId queda pendiente")
        }

        return Result.success(sessionId)
    }

    suspend fun syncAllPendingSessions(): Int {
        val pending = sessionDao.getPendingSync()

        if (pending.isEmpty()) {
            Log.i("WorkoutRepository", "No hay sesiones pendientes de sync")
            return 0
        }

        Log.i("WorkoutRepository", "Sincronizando ${pending.size} sesiones pendientes")

        var synced = 0
        pending.forEach { session ->
            if (syncSession(session.id)) synced++
        }

        Log.i("WorkoutRepository", "Sync completado: $synced/${pending.size}")
        return synced
    }

    suspend fun syncSession(sessionId: Long): Boolean {
        val results = resultDao.getResultsBySession(sessionId)

        if (results.isEmpty()) {
            sessionDao.markAsSynced(sessionId)
            return true
        }

        val request = buildBulkRequest(results)

        return try {
            val response = service.bulkSaveSetParameters(request)
            if (response.isSuccessful) {
                sessionDao.markAsSynced(sessionId)
                resultDao.markSessionResultsAsSynced(sessionId)
                Log.i("WorkoutRepository", "Sesión $sessionId sincronizada con el back")
                true
            } else {
                Log.w("WorkoutRepository", "Back rechazó sesión $sessionId: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.w("WorkoutRepository", "Error de red sincronizando sesión $sessionId: ${e.message}")
            false
        }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private fun buildSetResults(
        sessionId: Long,
        setParamState: Map<Long, Map<Long, Map<String, Any?>>>
    ): List<WorkoutSetResultEntity> {
        val results = mutableListOf<WorkoutSetResultEntity>()
        setParamState.forEach { (setTemplateId, paramMap) ->
            paramMap.forEach { (parameterId, values) ->
                results.add(
                    WorkoutSetResultEntity(
                        workoutSessionId = sessionId,
                        setTemplateId = setTemplateId,
                        parameterId = parameterId,
                        repetitions = values["repetitions"] as? Int,
                        numericValue = values["numericValue"] as? Double,
                        durationValue = values["durationValue"] as? Long,
                        integerValue = values["integerValue"] as? Int,
                        syncStatus = SyncStatus.PENDING_CREATE
                    )
                )
            }
        }
        return results
    }

    private fun buildBulkRequest(
        results: List<WorkoutSetResultEntity>
    ): BulkUpdateSetParametersRequest {
        val grouped = results.groupBy { it.setTemplateId }
        val setResults = grouped.map { (setTemplateId, params) ->
            BulkUpdateSetParametersRequest.SetResultRequest(
                setTemplateId = setTemplateId,
                parameters = params.map { r ->
                    BulkUpdateSetParametersRequest.ParameterResultRequest(
                        parameterId  = r.parameterId,
                        repetitions  = r.repetitions,
                        numericValue = r.numericValue,
                        durationValue = r.durationValue,
                        integerValue = r.integerValue
                    )
                }
            )
        }
        return BulkUpdateSetParametersRequest(setResults)
    }
}
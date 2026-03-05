package com.fitapp.appfit.repository

import android.content.Context
import android.util.Log
import com.fitapp.appfit.database.AppDatabase
import com.fitapp.appfit.database.entities.WorkoutSessionEntity
import com.fitapp.appfit.database.entities.WorkoutSetResultEntity
import com.fitapp.appfit.database.entities.enums.SyncStatus
import com.fitapp.appfit.response.sets.request.BulkUpdateSetParametersRequest
import com.fitapp.appfit.service.RoutineSetTemplateService
import com.fitapp.appfit.sync.NetworkChecker

/**
 * Repositorio central para el workflow de workout.
 *
 * Flujo de guardado:
 *  1. Persiste la sesión y los resultados en Room (siempre — historial offline)
 *  2. Si hay conexión → llama directamente a bulkSaveSetParameters
 *     → éxito → marca sesión como SYNCED
 *     → fallo → sesión queda PENDING_CREATE
 *  3. Sin conexión → sesión queda PENDING_CREATE
 *     → SyncWorker la recogerá cuando vuelva la red (via NetworkMonitor)
 */
class WorkoutRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val sessionDao = db.workoutSessionDao()
    private val resultDao = db.workoutSetResultDao()
    private val service = RoutineSetTemplateService.instance

    // Reutiliza el NetworkChecker que ya existe en el proyecto
    private val networkChecker = NetworkChecker(context)

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Punto de entrada principal desde WorkoutFragment.
     *
     * @param routineId     ID de la rutina entrenada
     * @param userId        ID del usuario autenticado
     * @param setParamState Valores modificados por el usuario:
     *                      setTemplateId → (parameterId → { reps, numericValue, ... })
     * @param startedAt     Timestamp de inicio del entrenamiento (epoch ms)
     *
     * @return Result.success(sessionId) si el guardado local fue correcto.
     *         Result.failure si falló el guardado local (error crítico).
     *         La sincronización con el back es best-effort y no afecta al Result.
     */
    suspend fun saveWorkout(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<Long, Map<String, Any?>>>,
        startedAt: Long = System.currentTimeMillis()
    ): Result<Long> {

        // 1. Guardar sesión localmente
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

        // 2. Guardar resultados localmente
        val results = buildSetResults(sessionId, setParamState)
        try {
            resultDao.insertResults(results)
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Error guardando resultados locales: ${e.message}", e)
            sessionDao.deleteSession(sessionId)
            return Result.failure(e)
        }

        // 3. Sync inmediato si hay conexión
        if (networkChecker.isOnline()) {
            syncSession(sessionId)
        } else {
            Log.i("WorkoutRepository", "Sin conexión — sesión $sessionId queda pendiente")
        }

        return Result.success(sessionId)
    }

    /**
     * Intenta sincronizar todas las sesiones PENDING_CREATE con el back.
     * Llamado por SyncWorker cuando se recupera la conexión.
     *
     * @return Número de sesiones sincronizadas con éxito
     */
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

    /**
     * Intenta sincronizar una sesión concreta con el back.
     * Si tiene éxito, marca la sesión y sus resultados como SYNCED.
     * Si falla, permanece PENDING_CREATE para reintento posterior.
     *
     * Se llama desde:
     *  - saveWorkout() si hay conexión en el momento de guardar
     *  - SyncWorker cuando se recupera la conexión
     */
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
                        repetitions  = values["repetitions"] as? Int,
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

    /**
     * Construye el BulkUpdateSetParametersRequest agrupando por setTemplateId.
     * Encaja exactamente con la estructura que espera el endpoint del back.
     */
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
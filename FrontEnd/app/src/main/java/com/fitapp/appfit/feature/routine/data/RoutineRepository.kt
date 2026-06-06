package com.fitapp.appfit.feature.routine.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.database.entity.RoutineEntity
import com.fitapp.appfit.feature.routine.database.entity.RoutineExerciseEntity
import com.fitapp.appfit.feature.routine.database.entity.SetParameterEntity
import com.fitapp.appfit.feature.routine.database.entity.SetTemplateEntity
import com.fitapp.appfit.feature.routine.model.rutine.request.CreateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.request.UpdateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineStatisticsResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.routine.util.RoutineErrorHandler
import com.fitapp.appfit.feature.routine.util.TrainingCachePreferences
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache
import com.fitapp.appfit.shared.enums.SyncStatus
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class RoutineRepository(private val context: Context) {

    companion object {
        private const val TAG = "RoutineRepository"
        private val DEFAULT_GYM_ROUTINE_NAMES = listOf(
            "Push / Pull / Legs — Gym",
            "Fuerza + Prep Box — 5 días"
        )
    }

    private val service = RoutineService.instance
    private val exerciseService = RoutineExerciseService.instance
    private val db by lazy { AppDatabase.getInstance(context) }
    private val routineDao by lazy { db.routineDao() }
    private val exerciseDao by lazy { db.routineExerciseDao() }
    private val setTemplateDao by lazy { db.setTemplateDao() }
    private val setParameterDao by lazy { db.setParameterDao() }
    private val lastSetExecutionDao by lazy { db.lastSetExecutionDao() }

    // ── CRUD básico ───────────────────────────────────────────────────────────

    suspend fun createRoutine(request: CreateRoutineRequest) =
        call { service.createRoutine(request) }

    suspend fun getRoutine(id: Long) =
        call { service.getRoutine(id) }

    suspend fun updateRoutine(id: Long, request: UpdateRoutineRequest) =
        call { service.updateRoutine(id, request) }

    suspend fun deleteRoutine(id: Long): Resource<Unit> {
        val result = callUnit { service.deleteRoutine(id) }
        if (result is Resource.Success) {
            deleteRoutineLocally(id)
        }
        return result
    }

    suspend fun toggleRoutineActiveStatus(id: Long, active: Boolean) =
        callUnit { service.toggleRoutineActiveStatus(id, active) }

    suspend fun generateDefaultRoutine(type: String): Resource<Map<String, Long>> {
        if (type.equals("GYM", ignoreCase = true)) {
            clearLocalDefaultGymRoutines()
        }

        val result = call { service.generateDefaultRoutine(type) }
        if (result is Resource.Success) {
            val routineId = result.data?.get("routineId")
            if (routineId != null) {
                Log.i(TAG, "GENERATE_DEFAULT_OK | type=$type | routineId=$routineId — prefetch for-training")
                when (val training = getRoutineForTraining(routineId)) {
                    is Resource.Success -> Log.i(
                        TAG,
                        "GENERATE_DEFAULT_CACHED | routineId=$routineId | name=${training.data?.name} | exercises=${training.data?.exercises?.size ?: 0}"
                    )
                    is Resource.Error -> Log.w(TAG, "GENERATE_DEFAULT_CACHE_FAILED | routineId=$routineId | ${training.message}")
                    else -> Unit
                }
            }
        }
        return result
    }

    suspend fun markRoutineAsUsed(id: Long) =
        callUnit { service.markRoutineAsUsed(id) }

    suspend fun getRoutineByExportKey(exportKey: String) =
        call { service.getRoutineByExportKey(exportKey) }

    suspend fun importRoutineFromExportKey(exportKey: String) =
        call { service.importRoutineFromExportKey(exportKey) }

    suspend fun registerPurchase(id: Long) =
        callUnit { service.registerPurchase(id) }

    // ── Listados con fallback a Room ──────────────────────────────────────────

    suspend fun getRoutines(page: Int = 0, size: Int = 20): Resource<PageResponse<RoutineSummaryResponse>> {
        val networkResult = call { service.getRoutines(page, size) }
        if (networkResult is Resource.Success) {
            networkResult.data?.content?.let { summaries ->
                purgeStaleLocalRoutines(summaries.map { it.id }.toSet())
                val entities = summaries.map { it.toEntity() }
                routineDao.insertRoutines(entities)
            }
            return networkResult
        }

        return try {
            val local = routineDao.getRoutines("")
            if (local.isEmpty()) return networkResult

            val summaries = local.map { it.toSummaryResponse() }
            Resource.Success(
                PageResponse(
                    content = summaries,
                    pageNumber = 0,
                    pageSize = summaries.size,
                    totalElements = summaries.size.toLong(),
                    totalPages = 1,
                    first = true,
                    last = true,
                    numberOfElements = summaries.size,
                    sort = null
                )
            )
        } catch (e: Exception) {
            networkResult
        }
    }

    suspend fun getRoutinesWithFilters(
        sportId: Long? = null,
        name: String? = null,
        isActive: Boolean? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        page: Int = 0,
        size: Int = 20
    ): Resource<PageResponse<RoutineSummaryResponse>> {
        val networkResult = call {
            service.getRoutinesWithFilters(sportId, name, isActive, sortBy, sortDirection, page, size)
        }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val local = routineDao.getRoutines("")
            val filtered = local
                .filter { isActive == null || it.isActive == isActive }
                .filter { name == null || it.name.contains(name, ignoreCase = true) }
                .map { it.toSummaryResponse() }

            if (filtered.isEmpty()) return networkResult

            Resource.Success(
                PageResponse(
                    content = filtered,
                    pageNumber = 0,
                    pageSize = filtered.size,
                    totalElements = filtered.size.toLong(),
                    totalPages = 1,
                    first = true,
                    last = true,
                    numberOfElements = filtered.size,
                    sort = null
                )
            )
        } catch (e: Exception) {
            networkResult
        }
    }

    suspend fun getLastUsedRoutines(limit: Int = 3): Resource<List<RoutineSummaryResponse>> {
        val networkResult = call { service.getLastUsedRoutines(limit) }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val local = routineDao.getRoutines("")
                .sortedByDescending { it.lastUsedAt }
                .take(limit)
                .map { it.toSummaryResponse() }

            if (local.isEmpty()) return networkResult
            Resource.Success(local)
        } catch (e: Exception) {
            networkResult
        }
    }

    suspend fun getRecentRoutines(limit: Int = 5): Resource<List<RoutineSummaryResponse>> {
        val networkResult = call { service.getRecentRoutines(limit) }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val local = routineDao.getRoutines("")
                .sortedByDescending { it.lastModifiedLocally }
                .take(limit)
                .map { it.toSummaryResponse() }

            if (local.isEmpty()) return networkResult
            Resource.Success(local)
        } catch (e: Exception) {
            networkResult
        }
    }

    suspend fun getActiveRoutines(): Resource<List<RoutineSummaryResponse>> {
        val networkResult = call { service.getActiveRoutines() }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val local = routineDao.getRoutines("")
                .filter { it.isActive }
                .map { it.toSummaryResponse() }

            if (local.isEmpty()) return networkResult
            Resource.Success(local)
        } catch (e: Exception) {
            networkResult
        }
    }

    // ── getRoutineForTraining con fallback completo ───────────────────────────

    /**
     * Loads training routine from Room when a valid local tree exists and the cache
     * is not marked stale. Falls back to [getRoutineForTraining] (network + cache).
     */
    suspend fun getRoutineForTrainingLocalFirst(id: Long): Resource<RoutineResponse> {
        if (!TrainingCachePreferences.needsRefresh(context, id)) {
            loadTrainingRoutineFromRoom(id)?.let { local ->
                if (!local.exercises.isNullOrEmpty()) {
                    Log.i(
                        TAG,
                        "FOR_TRAINING_LOCAL_FIRST | routineId=$id | exercises=${local.exercises?.size}"
                    )
                    return Resource.Success(local)
                }
            }
        } else {
            Log.i(TAG, "FOR_TRAINING_STALE_CACHE | routineId=$id — fetching from server")
        }
        return getRoutineForTraining(id)
    }

    suspend fun getRoutineForTraining(id: Long): Resource<RoutineResponse> {
        val networkResult = call { service.getRoutineForTraining(id) }
        if (networkResult is Resource.Success) {
            val training = networkResult.data
            if (!training?.exercises.isNullOrEmpty()) {
                cacheRoutineForTraining(training)
                return networkResult
            }
            Log.w(
                TAG,
                "FOR_TRAINING_EMPTY_EXERCISES | routineId=$id — trying exercises endpoint"
            )
        }

        tryFetchTrainingViaExercisesEndpoint(id)?.let { alternate ->
            if (alternate is Resource.Success && !alternate.data?.exercises.isNullOrEmpty()) {
                Log.i(
                    TAG,
                    "FOR_TRAINING_FALLBACK_EXERCISES_OK | routineId=$id | exercises=${alternate.data?.exercises?.size}"
                )
                return alternate
            }
        }

        if (networkResult is Resource.Success) {
            TrainingCachePreferences.markNeedsRefresh(context, id)
            return networkResult
        }

        return loadTrainingRoutineFromRoom(id)?.let { local ->
            Log.d(TAG, "FOR_TRAINING_OFFLINE_FALLBACK | routineId=$id")
            Resource.Success(local)
        } ?: networkResult
    }

    private suspend fun loadTrainingRoutineFromRoom(id: Long): RoutineResponse? {
        return try {
            val routine = routineDao.getRoutineById(id) ?: return null
            val exercises = exerciseDao.getExercises(id)
            if (exercises.isEmpty()) return null

            val exerciseResponses = exercises.map { exercise ->
                val sets = setTemplateDao.getSets(exercise.id)
                val setResponses = sets.map { set ->
                    val params = setParameterDao.getParameters(set.id)
                    set.toResponse(params)
                }
                exercise.toResponse(routineId = id, setsTemplate = setResponses)
            }

            routine.toFullResponse(exerciseResponses)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading training routine from Room | routineId=$id | ${e.message}", e)
            null
        }
    }

    // ── Estadísticas con fallback ─────────────────────────────────────────────

    suspend fun getRoutineStatistics(): Resource<RoutineStatisticsResponse> {
        val networkResult = call { service.getRoutineStatistics() }
        if (networkResult is Resource.Success) return networkResult

        return try {
            val all = routineDao.getRoutines("")
            if (all.isEmpty()) return networkResult

            val active = all.count { it.isActive }.toLong()
            Resource.Success(
                RoutineStatisticsResponse(
                    totalRoutines = all.size.toLong(),
                    activeRoutines = active,
                    inactiveRoutines = all.size.toLong() - active
                )
            )
        } catch (e: Exception) {
            networkResult
        }
    }

    // ── Mappers Room → Response ───────────────────────────────────────────────

    private fun RoutineEntity.toSummaryResponse() = RoutineSummaryResponse(
        id = id,
        name = name,
        description = description,
        sportId = sportId,
        sportName = sportName,
        isActive = isActive,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: "",
        lastUsedAt = lastUsedAt,
        trainingDays = emptySet(),
        goal = goal ?: "",
        sessionsPerWeek = sessionsPerWeek ?: 0,
        exerciseCount = 0
    )

    private fun RoutineEntity.toFullResponse(exercises: List<RoutineExerciseResponse>?) =
        RoutineResponse(
            id = id,
            name = name,
            description = description,
            sportId = sportId,
            sportName = sportName,
            isActive = isActive,
            createdAt = null,
            updatedAt = null,
            lastUsedAt = null,
            exercises = exercises,
            trainingDays = emptySet(),
            goal = goal,
            sessionsPerWeek = sessionsPerWeek
        )

    private fun RoutineExerciseEntity.toResponse(
        routineId: Long,
        setsTemplate: List<RoutineSetTemplateResponse>
    ) = RoutineExerciseResponse(
        id = id,
        routineId = routineId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        position = position,
        sessionNumber = sessionNumber,
        dayOfWeek = dayOfWeek,
        sessionOrder = sessionOrder,
        restAfterExercise = restAfterExercise,
        sets = sets,
        targetParameters = emptyList(),
        setsTemplate = setsTemplate,
        circuitGroupId = circuitGroupId,
        circuitRoundCount = circuitRoundCount,
        superSetGroupId = superSetGroupId,
        amrapDurationSeconds = amrapDurationSeconds,
        emomIntervalSeconds = emomIntervalSeconds,
        emomTotalRounds = emomTotalRounds,
        tabataWorkSeconds = tabataWorkSeconds,
        tabataRestSeconds = tabataRestSeconds,
        tabataRounds = tabataRounds,
        notes = notes
    )

    private fun SetTemplateEntity.toResponse(
        params: List<SetParameterEntity>
    ) = RoutineSetTemplateResponse(
        id = id,
        position = position,
        subSetNumber = subSetNumber,
        groupId = groupId,
        setType = setType,
        restAfterSet = restAfterSet,
        parameters = params.map { it.toResponse() }
    )

    private fun SetParameterEntity.toResponse() = RoutineSetParameterResponse(
        id = id,
        setTemplateId = setTemplateId,
        parameterId = parameterId ?: 0L,
        parameterName = parameterName,
        parameterType = parameterType,
        unit = unit,
        numericValue = numericValue,
        durationValue = durationValue,
        integerValue = integerValue,
        repetitions = repetitions
    )

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("El servidor respondió sin datos")
            } else {
                Resource.Error(RoutineErrorHandler.getErrorMessage(response))
            }
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<Unit>): Resource<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(RoutineErrorHandler.getErrorMessage(response))
            }
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private fun exceptionMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> "⏱️ Tiempo de espera agotado. Verifica tu conexión."
        is ConnectException -> "📡 Sin conexión. Verifica tu internet."
        is HttpException -> RoutineErrorHandler.getErrorMessage(e.response()!!)
        else -> "Error: ${e.message ?: "Error desconocido"}"
    }

    // ── Cache helpers ─────────────────────────────────────────────────────────

    suspend fun invalidateTrainingCache(routineId: Long) {
        TrainingCachePreferences.markNeedsRefresh(context, routineId)
        deleteRoutineLocally(routineId)
        Log.i(TAG, "INVALIDATED_TRAINING_CACHE | routineId=$routineId")
    }

    fun markTrainingCacheStale(routineId: Long) {
        TrainingCachePreferences.markNeedsRefresh(context, routineId)
        ActiveWorkoutCache(context).clearIfRoutine(routineId)
        Log.i(TAG, "MARKED_TRAINING_CACHE_STALE | routineId=$routineId")
    }

    /** Repopulates local training cache from API (for-training or exercises fallback). */
    suspend fun refreshTrainingCache(routineId: Long) {
        when (val result = getRoutineForTraining(routineId)) {
            is Resource.Success -> Log.i(
                TAG,
                "REFRESH_TRAINING_CACHE_OK | routineId=$routineId | exercises=${result.data?.exercises?.size ?: 0}"
            )
            is Resource.Error -> Log.w(TAG, "REFRESH_TRAINING_CACHE_FAILED | routineId=$routineId | ${result.message}")
            else -> Unit
        }
    }

    private suspend fun tryFetchTrainingViaExercisesEndpoint(id: Long): Resource<RoutineResponse>? {
        val exercisesResult = call { exerciseService.getRoutineExercises(id) }
        if (exercisesResult !is Resource.Success) {
            Log.w(TAG, "FOR_TRAINING_FALLBACK_EXERCISES_FAILED | routineId=$id")
            return null
        }

        val exercises = exercisesResult.data.orEmpty()
        if (exercises.isEmpty()) return null

        val header = when (val routineResult = call { service.getRoutine(id) }) {
            is Resource.Success -> routineResult.data
            else -> null
        }

        val response = header?.copy(exercises = exercises)
            ?: routineDao.getRoutineById(id)?.toFullResponse(exercises)
            ?: return null

        cacheRoutineForTraining(response)
        return Resource.Success(response)
    }

    private suspend fun clearLocalDefaultGymRoutines() {
        val ids = routineDao.getIdsByNames(DEFAULT_GYM_ROUTINE_NAMES)
        ids.forEach { deleteRoutineLocally(it) }
        Log.i(TAG, "CLEARED_LOCAL_GYM_ROUTINES | count=${ids.size}")
    }

    private suspend fun purgeStaleLocalRoutines(serverIds: Set<Long>) {
        val localIds = routineDao.getRoutines("").map { it.id }
        localIds.filter { it !in serverIds }.forEach { deleteRoutineLocally(it) }
    }

    /** Removes exercises/sets/params for cache refresh; keeps routine row and last execution history. */
    private suspend fun clearRoutineTrainingChildren(routineId: Long) {
        setParameterDao.deleteByRoutineId(routineId)
        setTemplateDao.deleteByRoutineId(routineId)
        exerciseDao.deleteByRoutineId(routineId)
        Log.d(TAG, "CLEARED_TRAINING_CHILDREN | routineId=$routineId")
    }

    /** Full local removal: training cache, last execution values, and routine header. */
    private suspend fun deleteRoutineLocally(routineId: Long) {
        lastSetExecutionDao.deleteByRoutine(routineId)
        clearRoutineTrainingChildren(routineId)
        routineDao.deleteRoutine(routineId)
        Log.d(TAG, "DELETED_ROUTINE_LOCALLY | routineId=$routineId")
    }

    private suspend fun pruneStaleLastExecutions(routine: RoutineResponse) {
        val paramsBySetId = routine.exercises.orEmpty()
            .flatMap { ex -> ex.setsTemplate.orEmpty() }
            .associate { set ->
                set.id to set.parameters.orEmpty().map { it.parameterId }.toSet()
            }
        val currentSetIds = paramsBySetId.keys

        val executions = lastSetExecutionDao.getLastExecutionsByRoutine(routine.id)
        if (executions.isEmpty()) return

        if (currentSetIds.isEmpty()) {
            lastSetExecutionDao.deleteByRoutine(routine.id)
            Log.i(TAG, "PRUNED_ALL_LAST_EXECUTIONS | routineId=${routine.id} | reason=no_sets")
            return
        }

        var prunedSets = 0
        var prunedParams = 0
        executions.groupBy { it.setTemplateId }.forEach { (setId, rows) ->
            if (setId !in currentSetIds) {
                lastSetExecutionDao.deleteBySet(routine.id, setId)
                prunedSets++
            } else {
                val validParamIds = paramsBySetId[setId].orEmpty()
                rows.filter { it.parameterId !in validParamIds }.forEach { row ->
                    lastSetExecutionDao.deleteBySetAndParameter(routine.id, setId, row.parameterId)
                    prunedParams++
                }
            }
        }
        if (prunedSets > 0 || prunedParams > 0) {
            Log.i(
                TAG,
                "PRUNED_STALE_LAST_EXECUTIONS | routineId=${routine.id} | sets=$prunedSets | params=$prunedParams"
            )
        }
    }

    private suspend fun cacheRoutineForTraining(routine: RoutineResponse) {
        if (routine.exercises.isNullOrEmpty()) {
            Log.w(TAG, "SKIP_CACHE_EMPTY_TRAINING_TREE | routineId=${routine.id}")
            TrainingCachePreferences.markNeedsRefresh(context, routine.id)
            return
        }
        try {
            clearRoutineTrainingChildren(routine.id)
            routineDao.insertRoutine(
                RoutineEntity(
                    id = routine.id,
                    userId = "",
                    name = routine.name,
                    description = routine.description,
                    sportId = routine.sportId,
                    sportName = routine.sportName,
                    isActive = routine.isActive,
                    goal = routine.goal,
                    sessionsPerWeek = routine.sessionsPerWeek,
                    trainingDays = routine.trainingDays?.joinToString(","),
                    createdAt = routine.createdAt,
                    updatedAt = routine.updatedAt,
                    lastUsedAt = routine.lastUsedAt,
                    syncStatus = SyncStatus.SYNCED
                )
            )
            Log.d(TAG, "Cacheando rutina ${routine.id}, ejercicios: ${routine.exercises.orEmpty().size}")

            val exerciseEntities = routine.exercises.orEmpty().map { ex ->
            Log.d(TAG, "  Cacheando ejercicio ${ex.id} (${ex.exerciseName}), routineId=${routine.id}")
                RoutineExerciseEntity(
                    id = ex.id,
                    routineId = routine.id,
                    exerciseId = ex.exerciseId,
                    exerciseName = ex.exerciseName ?: "",
                    position = ex.position,
                    sessionNumber = ex.sessionNumber,
                    dayOfWeek = ex.dayOfWeek,
                    sessionOrder = ex.sessionOrder,
                    restAfterExercise = ex.restAfterExercise,
                    sets = ex.sets,
                    circuitGroupId = ex.circuitGroupId,
                    circuitRoundCount = ex.circuitRoundCount,
                    superSetGroupId = ex.superSetGroupId,
                    amrapDurationSeconds = ex.amrapDurationSeconds,
                    emomIntervalSeconds = ex.emomIntervalSeconds,
                    emomTotalRounds = ex.emomTotalRounds,
                    tabataWorkSeconds = ex.tabataWorkSeconds,
                    tabataRestSeconds = ex.tabataRestSeconds,
                    tabataRounds = ex.tabataRounds,
                    notes = ex.notes
                )
            }
            exerciseDao.insertExercises(exerciseEntities)
            Log.d("RoutineRepository", "  Ejercicios insertados: ${exerciseEntities.size}")

            routine.exercises.orEmpty().forEach { ex ->
                val setEntities = ex.setsTemplate?.map { set ->
                    SetTemplateEntity(
                        id = set.id,
                        routineExerciseId = ex.id,
                        position = set.position,
                        subSetNumber = set.subSetNumber,
                        groupId = set.groupId,
                        setType = set.setType,
                        restAfterSet = set.restAfterSet
                    )
                } ?: emptyList()
                setTemplateDao.insertSets(setEntities)

                ex.setsTemplate?.forEach { set ->
                    set.parameters?.let { params ->
                        setParameterDao.deleteBySetTemplate(set.id)
                        val paramEntities = params.map { p ->
                            SetParameterEntity(
                                id = p.id,
                                setTemplateId = set.id,
                                parameterId = p.parameterId,
                                parameterName = p.parameterName,
                                parameterType = p.parameterType,
                                unit = p.unit,
                                numericValue = p.numericValue,
                                durationValue = p.durationValue,
                                integerValue = p.integerValue,
                                repetitions = p.repetitions
                            )
                        }
                        setParameterDao.insertParameters(paramEntities)
                    }
                }
            }
            pruneStaleLastExecutions(routine)
            TrainingCachePreferences.clearNeedsRefresh(context, routine.id)
        } catch (e: Exception) {
            Log.e("RoutineRepository", "Error cacheando rutina ${routine.id}: ${e.message}", e)
        }
    }

    private fun RoutineSummaryResponse.toEntity() = RoutineEntity(
        id = id,
        userId = "",
        name = name,
        description = description,
        sportId = sportId,
        sportName = sportName,
        isActive = isActive,
        goal = goal,
        sessionsPerWeek = sessionsPerWeek,
        trainingDays = trainingDays.joinToString(","),
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastUsedAt = lastUsedAt,
        syncStatus = SyncStatus.SYNCED
    )
}
package com.fitapp.appfit.feature.routine.data

import android.content.Context
import android.util.Log
import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.database.entity.RoutineEntity
import com.fitapp.appfit.feature.routine.database.entity.RoutineExerciseEntity
import com.fitapp.appfit.feature.routine.database.entity.SetParameterEntity
import com.fitapp.appfit.feature.routine.database.entity.SetTemplateEntity
import com.fitapp.appfit.shared.enums.SyncStatus
import com.fitapp.appfit.feature.routine.model.rutine.request.CreateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.request.UpdateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineStatisticsResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class RoutineRepository(private val context: Context) {

    private val service = RoutineService.Companion.instance
    private val db by lazy { AppDatabase.Companion.getInstance(context) }
    private val routineDao by lazy { db.routineDao() }
    private val exerciseDao by lazy { db.routineExerciseDao() }
    private val setTemplateDao by lazy { db.setTemplateDao() }
    private val setParameterDao by lazy { db.setParameterDao() }

    // ── CRUD básico (solo back) ───────────────────────────────────────────────

    suspend fun createRoutine(request: CreateRoutineRequest) =
        call { service.createRoutine(request) }

    suspend fun getRoutine(id: Long) =
        call { service.getRoutine(id) }

    suspend fun updateRoutine(id: Long, request: UpdateRoutineRequest) =
        call { service.updateRoutine(id, request) }

    suspend fun deleteRoutine(id: Long) =
        callUnit { service.deleteRoutine(id) }

    suspend fun toggleRoutineActiveStatus(id: Long, active: Boolean) =
        callUnit { service.toggleRoutineActiveStatus(id, active) }

    suspend fun generateDefaultRoutine(type: String) =
        call { service.generateDefaultRoutine(type) }

    suspend fun markRoutineAsUsed(id: Long) =
        callUnit { service.markRoutineAsUsed(id) }

    // ── Listados con fallback a Room ──────────────────────────────────────────

    suspend fun getRoutines(page: Int = 0, size: Int = 20): Resource<PageResponse<RoutineSummaryResponse>> {
        val networkResult = call { service.getRoutines(page, size) }
        if (networkResult is Resource.Success) {
            networkResult.data?.content?.let { summaries ->
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

    suspend fun getRoutineForTraining(id: Long): Resource<RoutineResponse> {
        val networkResult = call { service.getRoutineForTraining(id) }
        if (networkResult is Resource.Success) {
            networkResult.data?.let { cacheRoutineForTraining(it) }
            return networkResult
        }

        return try {
            Log.d("RoutineRepository", "Fallback offline: buscando rutina $id en Room")

            val routine = routineDao.getRoutineById(id)
            if (routine == null) {
                Log.w("RoutineRepository", "Rutina $id NO encontrada en Room — sin cache previo")
                return networkResult
            }
            Log.d("RoutineRepository", "Rutina $id encontrada en Room: ${routine.name}")

            val exercises = exerciseDao.getExercises(id)
            Log.d("RoutineRepository", "Ejercicios en Room para rutina $id: ${exercises.size}")

            val exerciseResponses = exercises.map { exercise ->
                val sets = setTemplateDao.getSets(exercise.id)
                Log.d("RoutineRepository", "  Ejercicio ${exercise.id} (${exercise.exerciseName}): ${sets.size} sets")
                val setResponses = sets.map { set ->
                    val params = setParameterDao.getParameters(set.id)
                    Log.d("RoutineRepository", "    Set ${set.id}: ${params.size} params")
                    set.toResponse(params)
                }
                exercise.toResponse(routineId = id, setsTemplate = setResponses)
            }

            Log.d("RoutineRepository", "Fallback completado OK para rutina $id")
            Resource.Success(routine.toFullResponse(exerciseResponses))
        } catch (e: Exception) {
            Log.e("RoutineRepository", "Error en fallback Room para rutina $id: ${e.message}", e)
            networkResult
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
        setsTemplate = setsTemplate
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

    // ── Funciones genéricas de red ────────────────────────────────────────────

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("El servidor respondió sin datos")
            } else {
                Resource.Error(httpErrorMessage(response.code(), response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<Unit>): Resource<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(httpErrorMessage(response.code(), response.errorBody()?.string()))
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int, body: String?): String = when (code) {
        401 -> "Sesión expirada. Vuelve a iniciar sesión."
        403 -> "No tienes permisos para realizar esta acción."
        404 -> "Recurso no encontrado."
        500 -> "Error del servidor. Intenta nuevamente."
        else -> "Error $code: ${body ?: "Error desconocido"}"
    }

    private fun exceptionMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado. Verifica tu conexión."
        is ConnectException -> "Sin conexión. Verifica tu internet."
        is HttpException -> httpErrorMessage(e.code(), e.message())
        else -> "Error: ${e.message ?: "Error desconocido"}"
    }

    // ── Cache helpers ─────────────────────────────────────────────────────────

    /**
     * Guarda la rutina completa (ejercicios + sets + parámetros) en Room.
     * Se llama cuando el back responde con éxito en getRoutineForTraining,
     * asegurando que el workout funcione offline en la próxima sesión.
     */
    private suspend fun cacheRoutineForTraining(routine: RoutineResponse) {
        try {
            Log.d("RoutineRepository", "Cacheando rutina ${routine.id}, ejercicios: ${routine.exercises.orEmpty().size}")

            val exerciseEntities = routine.exercises.orEmpty().map { ex ->
                Log.d("RoutineRepository", "  Cacheando ejercicio ${ex.id} (${ex.exerciseName}), routineId=${routine.id}")
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
                    sets = ex.sets
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
        } catch (e: Exception) {
            Log.e("RoutineRepository", "Error cacheando rutina ${routine.id}: ${e.message}", e)
        }
    }

    /**
     * Convierte RoutineSummaryResponse a RoutineEntity para cachear la lista.
     * userId se deja vacío porque el summary no lo incluye — se actualiza
     * cuando el usuario abre la rutina completa.
     */
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
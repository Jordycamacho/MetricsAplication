package com.fitapp.appfit.repository

import android.content.Context
import com.fitapp.appfit.database.AppDatabase
import com.fitapp.appfit.database.entities.RoutineEntity
import com.fitapp.appfit.database.entities.RoutineExerciseEntity
import com.fitapp.appfit.database.entities.SetParameterEntity
import com.fitapp.appfit.database.entities.SetTemplateEntity
import com.fitapp.appfit.response.routine.request.*
import com.fitapp.appfit.response.routine.response.*
import com.fitapp.appfit.response.sets.response.RoutineSetParameterResponse
import com.fitapp.appfit.service.RoutineService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * RoutineRepository con fallback offline.
 *
 * Estrategia por método:
 *  - getRoutines / getLastUsedRoutines / getActiveRoutines → back primero, Room si falla
 *  - getRoutineForTraining → back primero, Room si falla (reconstruye RoutineResponse completa)
 *  - getRoutineStatistics → back primero, Room si falla (calcula local)
 *  - create / update / delete / toggleActive → solo back (gestionado por SyncWorker offline)
 *
 * IMPORTANTE: el constructor requiere Context para acceder a Room.
 * Actualiza RoutineViewModel para pasarlo: RoutineRepository(application)
 */
class RoutineRepository(private val context: Context) {

    private val service = RoutineService.instance
    private val db by lazy { AppDatabase.getInstance(context) }
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

    suspend fun markRoutineAsUsed(id: Long) =
        callUnit { service.markRoutineAsUsed(id) }

    // ── Listados con fallback a Room ──────────────────────────────────────────

    suspend fun getRoutines(page: Int = 0, size: Int = 20): Resource<com.fitapp.appfit.response.page.PageResponse<RoutineSummaryResponse>> {
        val networkResult = call { service.getRoutines(page, size) }
        if (networkResult is Resource.Success) {
            // Cachear en Room para uso offline
            networkResult.data?.content?.let { summaries ->
                val entities = summaries.map { it.toEntity() }
                routineDao.insertRoutines(entities)
            }
            return networkResult
        }

        // Fallback: leer de Room y cachear para próxima vez
        return try {
            // userId vacío porque en Room se guarda como String — ajusta si tienes sesión disponible aquí
            val local = routineDao.getRoutines("")
            if (local.isEmpty()) return networkResult // devuelve el error original si Room también está vacío

            val summaries = local.map { it.toSummaryResponse() }
            Resource.Success(
                com.fitapp.appfit.response.page.PageResponse(
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
            networkResult // devuelve el error original de red
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
    ): Resource<com.fitapp.appfit.response.page.PageResponse<RoutineSummaryResponse>> {
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
                com.fitapp.appfit.response.page.PageResponse(
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
     * Intenta obtener la rutina completa del back.
     * Si falla, reconstruye desde Room: rutina + ejercicios + sets + parámetros.
     */
    suspend fun getRoutineForTraining(id: Long): Resource<RoutineResponse> {
        val networkResult = call { service.getRoutineForTraining(id) }
        if (networkResult is Resource.Success) {
            // Cachear rutina completa en Room para uso offline en workout
            networkResult.data?.let { cacheRoutineForTraining(it) }
            return networkResult
        }

        return try {
            android.util.Log.d("RoutineRepository", "Fallback offline: buscando rutina $id en Room")

            val routine = routineDao.getRoutineById(id)
            if (routine == null) {
                android.util.Log.w("RoutineRepository", "Rutina $id NO encontrada en Room — sin cache previo")
                return networkResult
            }
            android.util.Log.d("RoutineRepository", "Rutina $id encontrada en Room: ${routine.name}")

            val exercises = exerciseDao.getExercises(id)
            android.util.Log.d("RoutineRepository", "Ejercicios en Room para rutina $id: ${exercises.size}")

            val exerciseResponses = exercises.map { exercise ->
                val sets = setTemplateDao.getSets(exercise.id)
                android.util.Log.d("RoutineRepository", "  Ejercicio ${exercise.id} (${exercise.exerciseName}): ${sets.size} sets")
                val setResponses = sets.map { set ->
                    val params = setParameterDao.getParameters(set.id)
                    android.util.Log.d("RoutineRepository", "    Set ${set.id}: ${params.size} params")
                    set.toResponse(params)
                }
                exercise.toResponse(routineId = id, setsTemplate = setResponses)
            }

            android.util.Log.d("RoutineRepository", "Fallback completado OK para rutina $id")
            Resource.Success(routine.toFullResponse(exerciseResponses))
        } catch (e: Exception) {
            android.util.Log.e("RoutineRepository", "Error en fallback Room para rutina $id: ${e.message}", e)
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

    private fun RoutineEntity.toFullResponse(exercises: List<RoutineExerciseResponse>?) = RoutineResponse(
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
        is retrofit2.HttpException -> httpErrorMessage(e.code(), e.message())
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
            android.util.Log.d("RoutineRepository", "Cacheando rutina ${routine.id}, ejercicios: ${routine.exercises.orEmpty().size}")

            // Guardar exercises — usamos routine.id como routineId para garantizar
            // que coincide con la FK en Room (el campo routineId del response puede diferir)
            val exerciseEntities = routine.exercises.orEmpty().map { ex ->
                android.util.Log.d("RoutineRepository", "  Cacheando ejercicio ${ex.id} (${ex.exerciseName}), routineId=${routine.id}")
                RoutineExerciseEntity(
                    id = ex.id,
                    routineId = routine.id,  // usar routine.id garantiza que la FK existe
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
            android.util.Log.d("RoutineRepository", "  Ejercicios insertados: ${exerciseEntities.size}")

            // Guardar sets y parámetros de cada ejercicio
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
                        // Borrar parámetros viejos y reinsertar — evita duplicados
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
            // El cacheo nunca debe romper el flujo normal
            android.util.Log.e("RoutineRepository", "Error cacheando rutina ${routine.id}: ${e.message}", e)
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
        syncStatus = com.fitapp.appfit.database.entities.enums.SyncStatus.SYNCED
    )
}
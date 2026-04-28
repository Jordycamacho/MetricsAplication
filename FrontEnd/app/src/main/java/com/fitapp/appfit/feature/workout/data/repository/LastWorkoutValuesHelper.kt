package com.fitapp.appfit.feature.workout.data.repository

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.data.database.dao.WorkoutSetResultDao
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSetResultEntity

/**
 * Helper para pre-cargar valores de la última sesión de entrenamiento
 * en las plantillas de sets de una rutina.
 *
 * Esto permite que el usuario vea automáticamente los valores que usó
 * la última vez que entrenó, sin modificar las plantillas originales.
 *
 * IMPORTANTE: Todas las modificaciones son EN MEMORIA, no se tocan
 * las plantillas originales en la base de datos.
 */
class LastWorkoutValuesHelper(
    private val workoutSetResultDao: WorkoutSetResultDao
) {

    companion object {
        private const val TAG = "LastWorkoutValues"
    }

    /**
     * Carga los últimos valores usados y los aplica a la rutina.
     * Devuelve una COPIA de la rutina con los valores de la última sesión.
     */
    suspend fun applyLastValuesToRoutine(routine: RoutineResponse): RoutineResponse {
        val lastResults = workoutSetResultDao.getLastWorkoutResults(routine.id)

        if (lastResults.isEmpty()) {
            Log.d(TAG, "No hay sesiones previas para rutina ${routine.id}")
            return routine
        }

        Log.i(TAG, "APLICANDO_ULTIMOS_VALORES | routineId=${routine.id} | resultados=${lastResults.size}")

        // Agrupar resultados por setTemplateId y parameterId
        val lastValuesBySetAndParam = lastResults
            .groupBy { it.setTemplateId }
            .mapValues { (_, results) ->
                results.associateBy { it.parameterId }
            }

        // Clonar rutina y aplicar valores
        val modifiedExercises = routine.exercises?.map { exercise ->
            applyLastValuesToExercise(exercise, lastValuesBySetAndParam)
        }

        return routine.copy(exercises = modifiedExercises)
    }

    /**
     * Aplica últimos valores a un ejercicio.
     */
    private fun applyLastValuesToExercise(
        exercise: RoutineExerciseResponse,
        lastValuesBySetAndParam: Map<Long, Map<Long, WorkoutSetResultEntity>>
    ): RoutineExerciseResponse {
        val modifiedSets = exercise.setsTemplate?.map { set ->
            applyLastValuesToSet(set, lastValuesBySetAndParam)
        }

        return exercise.copy(setsTemplate = modifiedSets)
    }

    /**
     * Aplica últimos valores a un set.
     */
    private fun applyLastValuesToSet(
        set: RoutineSetTemplateResponse,
        lastValuesBySetAndParam: Map<Long, Map<Long, WorkoutSetResultEntity>>
    ): RoutineSetTemplateResponse {
        val lastValuesForSet = lastValuesBySetAndParam[set.id]

        if (lastValuesForSet == null) {
            // No hay valores previos para este set
            return set
        }

        // Clonar parámetros y aplicar últimos valores
        val modifiedParams = set.parameters?.map { param ->
            applyLastValuesToParameter(param, lastValuesForSet)
        }

        return set.copy(parameters = modifiedParams)
    }

    /**
     * Aplica últimos valores a un parámetro individual.
     */
    private fun applyLastValuesToParameter(
        param: RoutineSetParameterResponse,
        lastValuesForSet: Map<Long, WorkoutSetResultEntity>
    ): RoutineSetParameterResponse {
        val lastResult = lastValuesForSet[param.parameterId]

        if (lastResult == null) {
            // No hay valor previo para este parámetro
            return param
        }

        Log.d(TAG, "PARAM_${param.parameterId} | ÚLTIMO: reps=${lastResult.repetitions} num=${lastResult.numericValue} dur=${lastResult.durationValue} int=${lastResult.integerValue}")

        // Crear copia con últimos valores usados
        return RoutineSetParameterResponse(
            id = param.id,
            setTemplateId = param.setTemplateId,
            parameterId = param.parameterId,
            parameterName = param.parameterName,
            parameterType = param.parameterType,
            unit = param.unit,
            repetitions = lastResult.repetitions ?: param.repetitions,
            numericValue = lastResult.numericValue ?: param.numericValue,
            durationValue = lastResult.durationValue ?: param.durationValue,
            integerValue = lastResult.integerValue ?: param.integerValue
        )
    }

    /**
     * Obtiene los últimos valores usados para un set específico.
     * Útil para cargar valores bajo demanda.
     */
    suspend fun getLastValuesForSet(
        setTemplateId: Long,
        routineId: Long
    ): Map<Long, WorkoutSetResultValues> {
        val results = workoutSetResultDao.getLastResultsForSet(setTemplateId, routineId)

        return results.associate { result ->
            result.parameterId to WorkoutSetResultValues(
                repetitions = result.repetitions,
                numericValue = result.numericValue,
                durationValue = result.durationValue,
                integerValue = result.integerValue
            )
        }
    }

    /**
     * Verifica si hay sesiones previas para esta rutina.
     */
    suspend fun hasLastWorkout(routineId: Long): Boolean {
        return workoutSetResultDao.hasLastWorkout(routineId)
    }

    /**
     * Modelo simple para encapsular valores de un resultado.
     */
    data class WorkoutSetResultValues(
        val repetitions: Int?,
        val numericValue: Double?,
        val durationValue: Long?,
        val integerValue: Int?
    )
}

/**
 * Extension functions para copiar responses de forma inmutable.
 */
private fun RoutineResponse.copy(
    exercises: List<RoutineExerciseResponse>?
) = RoutineResponse(
    id = this.id,
    name = this.name,
    description = this.description,
    sportId = this.sportId,
    sportName = this.sportName,
    trainingDays = this.trainingDays,
    goal = this.goal,
    sessionsPerWeek = this.sessionsPerWeek,
    isActive = this.isActive,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    lastUsedAt = this.lastUsedAt,
    exercises = exercises
)

private fun RoutineExerciseResponse.copy(
    setsTemplate: List<RoutineSetTemplateResponse>?
) = RoutineExerciseResponse(
    id = this.id,
    exerciseId = this.exerciseId,
    routineId = this.routineId,
    exerciseName = this.exerciseName,
    position = this.position,
    sessionNumber = this.sessionNumber,
    dayOfWeek = this.dayOfWeek,
    sessionOrder = this.sessionOrder,
    restAfterExercise = this.restAfterExercise,
    sets = this.sets,
    targetParameters = this.targetParameters,
    setsTemplate = setsTemplate,
    // v2: Agrupación
    circuitGroupId = this.circuitGroupId,
    circuitRoundCount = this.circuitRoundCount,
    superSetGroupId = this.superSetGroupId,
    // v2: Modos especiales
    amrapDurationSeconds = this.amrapDurationSeconds,
    emomIntervalSeconds = this.emomIntervalSeconds,
    emomTotalRounds = this.emomTotalRounds,
    tabataWorkSeconds = this.tabataWorkSeconds,
    tabataRestSeconds = this.tabataRestSeconds,
    tabataRounds = this.tabataRounds,
    // v2: Notas
    notes = this.notes
)

private fun RoutineSetTemplateResponse.copy(
    parameters: List<RoutineSetParameterResponse>?
) = RoutineSetTemplateResponse(
    id = this.id,
    position = this.position,
    subSetNumber = this.subSetNumber,
    groupId = this.groupId,
    setType = this.setType,
    restAfterSet = this.restAfterSet,
    parameters = parameters
)
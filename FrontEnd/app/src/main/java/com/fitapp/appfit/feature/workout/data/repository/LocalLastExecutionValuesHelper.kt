package com.fitapp.appfit.feature.workout.data.repository

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.data.database.dao.LastSetExecutionDao
import com.fitapp.appfit.feature.workout.data.database.entity.LastSetExecutionEntity
import com.fitapp.appfit.feature.workout.util.WorkoutParameterHelper

/**
 * Helper que carga los últimos valores de ejecución DESDE SQLITE LOCAL
 * sin hacer ninguna consulta al servidor.
 *
 * Reemplaza la lógica anterior que hacía consultas API.
 * Los valores se guardaron en SQLite la última vez que se completó un entrenamiento.
 *
 * IMPORTANTE: Todas las modificaciones son EN MEMORIA, no se tocan
 * las plantillas originales en la base de datos.
 */
class LocalLastExecutionValuesHelper(
    private val lastSetExecutionDao: LastSetExecutionDao
) {

    companion object {
        private const val TAG = "LocalLastExecValues"
    }

    /**
     * Carga los últimos valores desde SQLite local y los aplica a la rutina.
     * Devuelve una COPIA de la rutina con los valores de la última sesión.
     *
     * NO HACE CONSULTAS AL SERVIDOR.
     */
    suspend fun applyLastValuesToRoutine(routine: RoutineResponse): RoutineResponse {
        val lastExecutions = lastSetExecutionDao.getLastExecutionsByRoutine(routine.id)

        if (lastExecutions.isEmpty()) {
            Log.d(TAG, "NO_LOCAL_HISTORY | routineId=${routine.id}")
            return routine
        }

        Log.i(
            TAG,
            "APPLYING_LOCAL_HISTORY | routineId=${routine.id} | " +
                    "records=${lastExecutions.size}"
        )

        // Agrupar por setTemplateId y parameterId
        val lastValuesBySetAndParam = lastExecutions
            .groupBy { it.setTemplateId }
            .mapValues { (_, executions) ->
                executions.associateBy { it.parameterId }
            }

        // Clonar rutina y aplicar valores
        val modifiedExercises = routine.exercises?.map { exercise ->
            applyLastValuesToExercise(exercise, lastValuesBySetAndParam)
        }

        return routine.copy(exercises = modifiedExercises)
    }

    private fun applyLastValuesToExercise(
        exercise: RoutineExerciseResponse,
        lastValuesBySetAndParam: Map<Long, Map<Long, LastSetExecutionEntity>>
    ): RoutineExerciseResponse {
        val modifiedSets = exercise.setsTemplate?.map { set ->
            applyLastValuesToSet(set, lastValuesBySetAndParam)
        }

        return exercise.copy(setsTemplate = modifiedSets)
    }

    private fun applyLastValuesToSet(
        set: RoutineSetTemplateResponse,
        lastValuesBySetAndParam: Map<Long, Map<Long, LastSetExecutionEntity>>
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

    private fun applyLastValuesToParameter(
        param: RoutineSetParameterResponse,
        lastValuesForSet: Map<Long, LastSetExecutionEntity>
    ): RoutineSetParameterResponse {
        val lastExecution = lastValuesForSet[param.parameterId]

        if (lastExecution == null) {
            // No hay valor previo para este parámetro
            return param
        }

        Log.d(
            TAG,
            "APPLYING_PARAM | parameterId=${param.parameterId} | " +
                    "reps=${lastExecution.lastRepetitions} | " +
                    "num=${lastExecution.lastNumericValue} | " +
                    "dur=${lastExecution.lastDurationValue} | " +
                    "int=${lastExecution.lastIntegerValue}"
        )

        val isReps = WorkoutParameterHelper.isRepsParameter(param)
        val repsFromHistory = lastExecution.lastRepetitions ?: lastExecution.lastIntegerValue

        return RoutineSetParameterResponse(
            id = param.id,
            setTemplateId = param.setTemplateId,
            parameterId = param.parameterId,
            parameterName = param.parameterName,
            parameterType = param.parameterType,
            unit = param.unit,
            repetitions = if (isReps) repsFromHistory ?: param.repetitions else lastExecution.lastRepetitions ?: param.repetitions,
            numericValue = lastExecution.lastNumericValue ?: param.numericValue,
            durationValue = lastExecution.lastDurationValue ?: param.durationValue,
            integerValue = if (isReps) repsFromHistory ?: param.integerValue else lastExecution.lastIntegerValue ?: param.integerValue
        )
    }

    /**
     * Obtiene los últimos valores para un set específico bajo demanda.
     */
    suspend fun getLastValuesForSet(
        routineId: Long,
        setTemplateId: Long
    ): Map<Long, LocalLastExecutionValues> {
        val executions = lastSetExecutionDao.getLastExecutionsForSet(routineId, setTemplateId)

        return executions.associate { execution ->
            execution.parameterId to LocalLastExecutionValues(
                repetitions = execution.lastRepetitions,
                numericValue = execution.lastNumericValue,
                durationValue = execution.lastDurationValue,
                integerValue = execution.lastIntegerValue
            )
        }
    }

    /**
     * Comprueba si hay historial local para esta rutina.
     */
    suspend fun hasLocalHistory(routineId: Long): Boolean {
        return lastSetExecutionDao.countByRoutine(routineId) > 0
    }

    /**
     * Obtiene el timestamp del último entrenamiento de esta rutina.
     */
    suspend fun getLastWorkoutTime(routineId: Long): Long? {
        return lastSetExecutionDao.getLastRecordedTime(routineId)
    }

    /**
     * Modelo simple para encapsular valores de una ejecución.
     */
    data class LocalLastExecutionValues(
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
    circuitGroupId = this.circuitGroupId,
    circuitRoundCount = this.circuitRoundCount,
    superSetGroupId = this.superSetGroupId,
    amrapDurationSeconds = this.amrapDurationSeconds,
    emomIntervalSeconds = this.emomIntervalSeconds,
    emomTotalRounds = this.emomTotalRounds,
    tabataWorkSeconds = this.tabataWorkSeconds,
    tabataRestSeconds = this.tabataRestSeconds,
    tabataRounds = this.tabataRounds,
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
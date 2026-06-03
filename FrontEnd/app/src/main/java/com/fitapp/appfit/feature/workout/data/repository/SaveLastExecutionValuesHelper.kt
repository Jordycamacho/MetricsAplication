package com.fitapp.appfit.feature.workout.data.repository

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.data.database.dao.LastSetExecutionDao
import com.fitapp.appfit.feature.workout.data.database.entity.LastSetExecutionEntity
import com.fitapp.appfit.feature.workout.util.WorkoutParameterHelper

/**
 * Helper para guardar los valores de una sesión de entrenamiento completada
 * en SQLite para su reutilización posterior.
 *
 * Se llama cuando el usuario guarda un entrenamiento con sets completados.
 */
class SaveLastExecutionValuesHelper(
    private val lastSetExecutionDao: LastSetExecutionDao
) {

    companion object {
        private const val TAG = "SaveLastExecValuesHelper"
    }

    /**
     * Guarda los valores de ejecución de todos los sets completados en una sesión.
     *
     * @param routineId ID de la rutina
     * @param sets Mapa de setId -> parámetros ejecutados
     *             Formato: { setId: { "exerciseId": Long, "parameters": { paramId: { reps, num, dur, int } } } }
     * @param setTemplateResponses Información del template de cada set (para obtener metadata)
     */
    suspend fun saveExecutedValues(
        routineId: Long,
        sets: Map<Long, Map<String, Any?>>,
        setTemplateResponses: Map<Long, RoutineSetTemplateResponse>
    ) {
        Log.i(TAG, "SAVING_EXECUTION_VALUES | routineId=$routineId | setsCount=${sets.size}")

        val executionEntities = mutableListOf<LastSetExecutionEntity>()

        sets.forEach { (setId, setData) ->
            @Suppress("UNCHECKED_CAST")
            val parameters = setData["parameters"] as? Map<Long, Map<String, Any?>> ?: return@forEach

            val setTemplate = setTemplateResponses[setId]

            parameters.forEach { (paramId, paramValues) ->
                val paramTemplate = setTemplate?.parameters?.find { it.parameterId == paramId }
                val reps = paramValues["repetitions"] as? Int
                val integer = paramValues["integerValue"] as? Int
                val isReps = paramTemplate?.let { WorkoutParameterHelper.isRepsParameter(it) } == true
                val repsToStore = if (isReps) reps ?: integer else reps

                val entity = LastSetExecutionEntity(
                    routineId = routineId,
                    setTemplateId = setId,
                    parameterId = paramId,
                    parameterName = paramTemplate?.parameterName,
                    parameterType = paramTemplate?.parameterType,
                    unit = paramTemplate?.unit,
                    lastRepetitions = repsToStore,
                    lastNumericValue = (paramValues["numericValue"] as? Double),
                    lastDurationValue = (paramValues["durationValue"] as? Long),
                    lastIntegerValue = if (isReps) repsToStore else integer,
                    recordedAt = System.currentTimeMillis()
                )

                executionEntities.add(entity)

                Log.d(
                    TAG,
                    "EXECUTION_CREATED | setId=$setId | paramId=$paramId | " +
                            "reps=${entity.lastRepetitions} | num=${entity.lastNumericValue} | " +
                            "dur=${entity.lastDurationValue} | int=${entity.lastIntegerValue}"
                )
            }
        }

        if (executionEntities.isNotEmpty()) {
            try {
                lastSetExecutionDao.insertOrUpdateBatch(executionEntities)
                Log.i(TAG, "EXECUTION_VALUES_SAVED | count=${executionEntities.size}")
            } catch (e: Exception) {
                Log.e(TAG, "ERROR_SAVING_EXECUTION_VALUES | error=${e.message}", e)
            }
        }
    }

    /**
     * Guarda el valor de un parámetro específico (útil si se actualiza un valor individual).
     */
    suspend fun saveSingleValue(
        routineId: Long,
        setTemplateId: Long,
        parameterId: Long,
        parameterName: String?,
        parameterType: String?,
        unit: String?,
        repetitions: Int? = null,
        numericValue: Double? = null,
        durationValue: Long? = null,
        integerValue: Int? = null
    ) {
        val entity = LastSetExecutionEntity(
            routineId = routineId,
            setTemplateId = setTemplateId,
            parameterId = parameterId,
            parameterName = parameterName,
            parameterType = parameterType,
            unit = unit,
            lastRepetitions = repetitions,
            lastNumericValue = numericValue,
            lastDurationValue = durationValue,
            lastIntegerValue = integerValue,
            recordedAt = System.currentTimeMillis()
        )

        try {
            lastSetExecutionDao.insertOrUpdate(entity)
            Log.d(
                TAG,
                "SINGLE_VALUE_SAVED | setId=$setTemplateId | paramId=$parameterId | " +
                        "reps=$repetitions | num=$numericValue | dur=$durationValue | int=$integerValue"
            )
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_SAVING_SINGLE_VALUE | error=${e.message}", e)
        }
    }

    /**
     * Limpia los registros de una rutina (si el usuario la elimina, por ejemplo).
     */
    suspend fun deleteValuesForRoutine(routineId: Long) {
        try {
            lastSetExecutionDao.deleteByRoutine(routineId)
            Log.i(TAG, "EXECUTION_VALUES_DELETED | routineId=$routineId")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR_DELETING_VALUES | error=${e.message}", e)
        }
    }
}
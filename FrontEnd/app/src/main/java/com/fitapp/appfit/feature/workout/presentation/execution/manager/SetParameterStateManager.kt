package com.fitapp.appfit.feature.workout.presentation.execution.manager

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse

/**
 * Manager: Estado de parámetros durante la ejecución
 *
 * Ahora almacena también el exerciseId para poder exportar la información completa
 * que necesita el repositorio.
 */
class SetParameterStateManager {

    companion object {
        private const val TAG = "SetParameterStateManager"
    }

    private data class SetState(
        val exerciseId: Long,
        val parameters: MutableMap<Long, ParameterValues>
    )

    private val state = mutableMapOf<Long, SetState>()

    data class ParameterValues(
        var repetitions: Int? = null,
        var numericValue: Double? = null,
        var durationValue: Long? = null,
        var integerValue: Int? = null
    )

    /**
     * Inicializa el estado para un set con su exerciseId y parámetros.
     */
    fun initializeSet(
        setId: Long,
        exerciseId: Long,
        setTemplate: RoutineSetTemplateResponse
    ) {
        if (state.containsKey(setId)) {
            Log.d(TAG, "SET_ALREADY_INITIALIZED | setId=$setId")
            return
        }

        val paramMap = mutableMapOf<Long, ParameterValues>()
        setTemplate.parameters?.forEach { param ->
            paramMap[param.parameterId] = ParameterValues(
                repetitions = param.repetitions,
                numericValue = param.numericValue,
                durationValue = param.durationValue,
                integerValue = param.integerValue
            )
        }

        state[setId] = SetState(exerciseId, paramMap)
        Log.d(TAG, "SET_INITIALIZED | setId=$setId | exerciseId=$exerciseId | params=${paramMap.size}")
    }

    fun getParameterValues(setId: Long, parameterId: Long): ParameterValues? {
        return state[setId]?.parameters?.get(parameterId)
    }

    fun updateReps(setId: Long, reps: Int) {
        Log.d(TAG, "UPDATE_REPS | setId=$setId | reps=$reps")
        state[setId]?.parameters?.values?.forEach { it.repetitions = reps }
    }

    fun updateNumericValue(setId: Long, parameterId: Long, value: Double) {
        Log.d(TAG, "UPDATE_NUMERIC | setId=$setId | parameterId=$parameterId | value=$value")
        state[setId]?.parameters?.get(parameterId)?.numericValue = value
    }

    fun updateIntegerValue(setId: Long, parameterId: Long, value: Int) {
        Log.d(TAG, "UPDATE_INTEGER | setId=$setId | parameterId=$parameterId | value=$value")
        state[setId]?.parameters?.get(parameterId)?.integerValue = value
    }

    fun updateDurationValue(setId: Long, parameterId: Long, value: Long) {
        Log.d(TAG, "UPDATE_DURATION | setId=$setId | parameterId=$parameterId | value=$value")
        state[setId]?.parameters?.get(parameterId)?.durationValue = value
    }

    /**
     * Exporta el estado con la estructura que espera el repositorio:
     *   setTemplateId -> { "exerciseId": Long, "parameters": { paramId -> { ... } } }
     */
    fun exportState(): Map<Long, Map<String, Any?>> {
        Log.i(TAG, "EXPORTING_STATE | setsCount=${state.size}")
        return state.mapValues { (_, setState) ->
            mapOf(
                "exerciseId" to setState.exerciseId,
                "parameters" to setState.parameters.mapValues { (_, values) ->
                    mapOf<String, Any?>(
                        "repetitions" to values.repetitions,
                        "numericValue" to values.numericValue,
                        "durationValue" to values.durationValue,
                        "integerValue" to values.integerValue
                    )
                }
            )
        }
    }

    fun clear() {
        Log.i(TAG, "CLEARING_STATE")
        state.clear()
    }

    fun getRegisteredSets(): Set<Long> = state.keys
}
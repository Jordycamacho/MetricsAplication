package com.fitapp.appfit.feature.workout.presentation.execution.manager

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse

class SetParameterStateManager {

    companion object {
        private const val TAG = "SetParameterStateManager"
    }

    private data class SetState(
        val routineExerciseId: Long,
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

    fun initializeSet(
        setId: Long,
        routineExerciseId: Long,
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

        state[setId] = SetState(routineExerciseId, exerciseId, paramMap)
        Log.d(TAG, "SET_INITIALIZED | setId=$setId | exerciseId=$exerciseId | params=${paramMap.size}")
    }

    fun restoreFromExport(
        setId: Long,
        exerciseId: Long,
        params: Map<Long, Map<String, Any?>>
    ) {
        val paramMap = mutableMapOf<Long, ParameterValues>()
        params.forEach { (paramId, values) ->
            paramMap[paramId] = ParameterValues(
                repetitions = values["repetitions"] as? Int,
                numericValue = values["numericValue"] as? Double,
                durationValue = values["durationValue"] as? Long,
                integerValue = values["integerValue"] as? Int
            )
        }
        state[setId] = SetState(0L, exerciseId, paramMap) // routineExerciseId no se usa en restauración
        Log.d(TAG, "SET_RESTORED | setId=$setId | exerciseId=$exerciseId | params=${paramMap.size}")
    }

    fun getParameterValues(setId: Long, parameterId: Long): ParameterValues? =
        state[setId]?.parameters?.get(parameterId)

    fun getRegisteredSets(): Set<Long> = state.keys

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

    fun getSetParameters(
        setId: Long,
        defaultExerciseId: Long,
        defaultParameters: List<RoutineSetParameterResponse>
    ): Map<String, Any?>? {
        state[setId]?.let { setState ->
            val paramsMap = mutableMapOf<Long, Map<String, Any?>>()
            setState.parameters.forEach { (paramId, values) ->
                val paramValues = buildNonNullParamMap(
                    repetitions = values.repetitions,
                    integerValue = values.integerValue,
                    numericValue = values.numericValue,
                    durationValue = values.durationValue
                )
                if (paramValues.isNotEmpty()) paramsMap[paramId] = paramValues
            }
            if (paramsMap.isEmpty()) return null
            return mapOf("exerciseId" to setState.exerciseId, "parameters" to paramsMap)
        }

        // Fallback
        val paramsMap = mutableMapOf<Long, Map<String, Any?>>()
        defaultParameters.forEach { param ->
            val paramValues = buildNonNullParamMap(
                repetitions = param.repetitions,
                integerValue = param.integerValue,
                numericValue = param.numericValue,
                durationValue = param.durationValue
            )
            if (paramValues.isNotEmpty()) paramsMap[param.parameterId] = paramValues
        }
        if (paramsMap.isEmpty()) return null
        return mapOf("exerciseId" to defaultExerciseId, "parameters" to paramsMap)
    }

    fun clear() {
        Log.i(TAG, "CLEARING_STATE")
        state.clear()
    }

    private fun buildNonNullParamMap(
        repetitions: Int?,
        integerValue: Int?,
        numericValue: Double?,
        durationValue: Long?
    ): Map<String, Any?> = buildMap {
        if (repetitions != null) put("repetitions", repetitions)
        if (integerValue != null) put("integerValue", integerValue)
        if (numericValue != null) put("numericValue", numericValue)
        if (durationValue != null) put("durationValue", durationValue)
    }
}
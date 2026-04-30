package com.fitapp.appfit.feature.workout.presentation.execution.manager

import android.util.Log
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse

/**
 * Mantiene el estado mutable de parámetros durante la ejecución del entrenamiento.
 *
 * Ciclo de vida:
 *  - initializeSet    → crea el estado desde la plantilla (primera vez que se toca un set)
 *  - restoreFromExport → reconstruye el estado desde el cache persistido (restauración)
 *  - update*          → modifica un valor concreto
 *  - exportState      → serializa para guardar/persistir
 *  - getSetParameters → lee el estado actual (o defaults del template si no fue editado)
 *  - clear            → limpia todo al terminar el entrenamiento
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

    // ── Init ──────────────────────────────────────────────────────────────────

    /**
     * Inicializa el estado para un set desde su plantilla.
     * Si el set ya estaba inicializado, no lo sobreescribe.
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

    /**
     * Restaura el estado de un set directamente desde el mapa exportado por el cache.
     * Usar solo al recuperar una sesión activa; no reemplaza initializeSet en el flujo normal.
     */
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
        state[setId] = SetState(exerciseId, paramMap)
        Log.d(TAG, "SET_RESTORED | setId=$setId | exerciseId=$exerciseId | params=${paramMap.size}")
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    fun getParameterValues(setId: Long, parameterId: Long): ParameterValues? =
        state[setId]?.parameters?.get(parameterId)

    fun getRegisteredSets(): Set<Long> = state.keys

    // ── Update ────────────────────────────────────────────────────────────────

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

    // ── Export ────────────────────────────────────────────────────────────────

    /**
     * Exporta el estado completo. Formato esperado por el repositorio:
     *   setTemplateId → { "exerciseId": Long, "parameters": { paramId → { ... } } }
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

    /**
     * Obtiene los parámetros de un set para guardado.
     * Si el set fue editado, devuelve sus valores actuales.
     * Si no, construye los defaults desde el template.
     */
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

        // Fallback: sin edición previa, usamos los valores del template
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

    // ── Clear ─────────────────────────────────────────────────────────────────

    fun clear() {
        Log.i(TAG, "CLEARING_STATE")
        state.clear()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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
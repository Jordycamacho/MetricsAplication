package com.fitapp.appfit.feature.workout.util

import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.presentation.execution.manager.SetParameterStateManager
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object WorkoutParameterHelper {

    private const val DECIMAL_EPSILON = 0.0001

    private val NUMERIC_TYPES = setOf("NUMBER", "INTEGER", "DISTANCE", "PERCENTAGE")
    private val REPS_NAMES = setOf("repeticiones", "reps")

    fun isRepsParameter(param: RoutineSetParameterResponse): Boolean {
        val name = param.parameterName?.lowercase()?.trim().orEmpty()
        return name in REPS_NAMES
    }

    fun findRepsParameter(params: List<RoutineSetParameterResponse>?): RoutineSetParameterResponse? =
        params?.firstOrNull { isRepsParameter(it) }

    fun findNumericParameter(params: List<RoutineSetParameterResponse>?): RoutineSetParameterResponse? =
        params?.firstOrNull { param ->
            !isRepsParameter(param) && (
                param.parameterType?.uppercase() in NUMERIC_TYPES
                    || param.numericValue != null
                    || param.integerValue != null
                )
        }

    /** Parámetros editables con sheet manual (todos excepto tiempo/texto/booleano). */
    fun supportsManualInput(param: RoutineSetParameterResponse): Boolean {
        val type = param.parameterType?.uppercase()?.trim().orEmpty()
        if (type == "DURATION" || type == "TEXT" || type == "BOOLEAN") return false
        return isRepsParameter(param)
            || type in NUMERIC_TYPES
            || param.numericValue != null
            || param.integerValue != null
    }

    fun isIntegerInput(param: RoutineSetParameterResponse): Boolean =
        isRepsParameter(param) || param.parameterType?.uppercase() == "INTEGER"

    fun isWeightParameter(param: RoutineSetParameterResponse): Boolean {
        val name = param.parameterName?.lowercase()?.trim().orEmpty()
        return name.contains("peso")
            || name.contains("weight")
            || name == "carga"
            || name.contains("carga")
    }

    /** Unidad del parámetro (nunca el nombre). */
    fun resolveUnit(param: RoutineSetParameterResponse): String {
        param.unit?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return when {
            isWeightParameter(param) -> "kg"
            param.parameterType?.uppercase() == "DISTANCE" -> "m"
            param.parameterType?.uppercase() == "PERCENTAGE" -> "%"
            param.parameterType?.uppercase() == "INTEGER" -> "rep"
            else -> ""
        }
    }

    /** Etiqueta superior en ejecución: solo unidad (kg, m, %…). */
    fun displayUnit(param: RoutineSetParameterResponse): String {
        return resolveUnit(param).ifEmpty { "—" }
    }

    fun formatNumericValue(value: Double, param: RoutineSetParameterResponse): String {
        return when (param.parameterType?.uppercase()) {
            "PERCENTAGE" -> "%.0f".format(value)
            "INTEGER" -> value.toInt().toString()
            else -> if (hasFraction(value)) "%.1f".format(value) else value.toInt().toString()
        }
    }

    /**
     * Botones +/- : siempre +1 o -1 por pulsación.
     * Si el valor tiene decimales, ajusta solo lo necesario para llegar al entero más cercano.
     * Ej: 12.5 + → 13 | 13 + → 14 | 12.5 - → 12 | 13 - → 12
     */
    fun adjustNumericByButton(current: Double, direction: Int, min: Double = 0.0): Double {
        require(direction == 1 || direction == -1)
        val result = if (direction > 0) {
            if (hasFraction(current)) ceil(current - DECIMAL_EPSILON) else current + 1.0
        } else {
            if (hasFraction(current)) floor(current + DECIMAL_EPSILON) else current - 1.0
        }
        return max(min, result)
    }

    fun parseNumericInput(input: String, param: RoutineSetParameterResponse): Double? {
        val normalized = input.trim().replace(',', '.')
        if (normalized.isEmpty()) return null
        return if (isIntegerInput(param)) {
            normalized.toIntOrNull()?.toDouble()
        } else {
            normalized.toDoubleOrNull()
        }
    }

    fun readNumericValue(
        setId: Long,
        param: RoutineSetParameterResponse,
        stateManager: SetParameterStateManager
    ): Double? {
        val values = stateManager.getParameterValues(setId, param.parameterId)
        return values?.numericValue
            ?: values?.integerValue?.toDouble()
            ?: param.numericValue
            ?: param.integerValue?.toDouble()
    }

    private fun hasFraction(value: Double): Boolean {
        val fraction = value - floor(value)
        return fraction > DECIMAL_EPSILON && fraction < 1.0 - DECIMAL_EPSILON
    }
}

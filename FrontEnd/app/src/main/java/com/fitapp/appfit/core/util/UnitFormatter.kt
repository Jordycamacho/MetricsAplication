package com.fitapp.appfit.core.util

import android.content.Context
import com.fitapp.appfit.core.preferences.AppPreferences
import com.fitapp.appfit.feature.routine.model.setparameter.response.RoutineSetParameterResponse
import com.fitapp.appfit.feature.workout.util.WorkoutParameterHelper

/**
 * Display-only unit conversion. Stored/server values remain in kg and meters.
 */
object UnitFormatter {

    const val KG_TO_LBS = 2.20462
    const val M_TO_FT = 3.28084

    fun displayUnit(context: Context, param: RoutineSetParameterResponse): String {
        if (WorkoutParameterHelper.isWeightParameter(param)) {
            return AppPreferences.getWeightUnitLabel(context)
        }
        if (param.parameterType?.uppercase() == "DISTANCE") {
            return AppPreferences.getDistanceUnitLabel(context)
        }
        return WorkoutParameterHelper.resolveUnit(param).ifEmpty { "—" }
    }

    fun displayNumericValue(
        context: Context,
        storedValue: Double,
        param: RoutineSetParameterResponse
    ): Double {
        return when {
            WorkoutParameterHelper.isWeightParameter(param) -> {
                if (AppPreferences.getWeightUnit(context) == AppPreferences.WeightUnit.LBS) {
                    storedValue * KG_TO_LBS
                } else {
                    storedValue
                }
            }
            param.parameterType?.uppercase() == "DISTANCE" -> {
                if (AppPreferences.getDistanceUnit(context) == AppPreferences.DistanceUnit.FT) {
                    storedValue * M_TO_FT
                } else {
                    storedValue
                }
            }
            else -> storedValue
        }
    }

    fun formatNumericForDisplay(
        context: Context,
        storedValue: Double,
        param: RoutineSetParameterResponse
    ): String {
        val displayValue = displayNumericValue(context, storedValue, param)
        return WorkoutParameterHelper.formatNumericValue(displayValue, param)
    }

    fun toStoredWeightValue(context: Context, displayValue: Double): Double {
        return if (AppPreferences.getWeightUnit(context) == AppPreferences.WeightUnit.LBS) {
            displayValue / KG_TO_LBS
        } else {
            displayValue
        }
    }

    fun toStoredDistanceValue(context: Context, displayValue: Double): Double {
        return if (AppPreferences.getDistanceUnit(context) == AppPreferences.DistanceUnit.FT) {
            displayValue / M_TO_FT
        } else {
            displayValue
        }
    }

    fun toStoredNumericValue(
        context: Context,
        displayValue: Double,
        param: RoutineSetParameterResponse
    ): Double {
        return when {
            WorkoutParameterHelper.isWeightParameter(param) ->
                toStoredWeightValue(context, displayValue)
            param.parameterType?.uppercase() == "DISTANCE" ->
                toStoredDistanceValue(context, displayValue)
            else -> displayValue
        }
    }
}

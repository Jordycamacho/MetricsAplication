package com.fitapp.appfit.core.preferences

import android.content.Context

object AppPreferences {

    private const val PREFS_NAME = "app_preferences"
    private const val KEY_WEIGHT_UNIT = "weight_unit"
    private const val KEY_DISTANCE_UNIT = "distance_unit"
    private const val KEY_PREFILL_STRATEGY = "prefill_strategy"

    enum class WeightUnit { KG, LBS }
    enum class DistanceUnit { M, FT }
    enum class PrefillStrategy { LAST_EXERCISE, LAST_SAME_ROUTINE }

    fun getWeightUnit(context: Context): WeightUnit =
        WeightUnit.valueOf(
            prefs(context).getString(KEY_WEIGHT_UNIT, WeightUnit.KG.name) ?: WeightUnit.KG.name
        )

    fun setWeightUnit(context: Context, unit: WeightUnit) {
        prefs(context).edit().putString(KEY_WEIGHT_UNIT, unit.name).apply()
    }

    fun getDistanceUnit(context: Context): DistanceUnit =
        DistanceUnit.valueOf(
            prefs(context).getString(KEY_DISTANCE_UNIT, DistanceUnit.M.name) ?: DistanceUnit.M.name
        )

    fun setDistanceUnit(context: Context, unit: DistanceUnit) {
        prefs(context).edit().putString(KEY_DISTANCE_UNIT, unit.name).apply()
    }

    fun getPrefillStrategy(context: Context): PrefillStrategy =
        PrefillStrategy.valueOf(
            prefs(context).getString(KEY_PREFILL_STRATEGY, PrefillStrategy.LAST_SAME_ROUTINE.name)
                ?: PrefillStrategy.LAST_SAME_ROUTINE.name
        )

    fun setPrefillStrategy(context: Context, strategy: PrefillStrategy) {
        prefs(context).edit().putString(KEY_PREFILL_STRATEGY, strategy.name).apply()
    }

    fun getWeightUnitLabel(context: Context): String =
        when (getWeightUnit(context)) {
            WeightUnit.KG -> "kg"
            WeightUnit.LBS -> "lbs"
        }

    fun getDistanceUnitLabel(context: Context): String =
        when (getDistanceUnit(context)) {
            DistanceUnit.M -> "m"
            DistanceUnit.FT -> "ft"
        }

    fun getPrefillStrategyLabel(context: Context): String =
        when (getPrefillStrategy(context)) {
            PrefillStrategy.LAST_EXERCISE -> "Último ejercicio"
            PrefillStrategy.LAST_SAME_ROUTINE -> "Misma rutina"
        }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

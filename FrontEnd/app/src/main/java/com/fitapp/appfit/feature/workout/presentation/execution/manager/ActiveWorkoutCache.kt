package com.fitapp.appfit.feature.workout.presentation.execution.manager

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persiste el estado activo de un entrenamiento en SharedPreferences.
 *
 * Escribe en cada cambio relevante (valor modificado, set completado).
 * Se lee al recrear el ViewModel para restaurar la sesión si el proceso murió.
 *
 * Clave de diseño: formato JSON propio en lugar de Gson para no añadir
 * dependencias. Los datos que persiste son exactamente los que ya maneja
 * SetParameterStateManager + WorkoutCompletionState.
 */
class ActiveWorkoutCache(context: Context) {

    companion object {
        private const val TAG = "ActiveWorkoutCache"
        private const val PREFS_NAME = "active_workout_cache"
        private const val KEY_ROUTINE_ID = "routine_id"
        private const val KEY_STARTED_AT = "started_at"
        private const val KEY_PARAM_STATE = "param_state"
        private const val KEY_COMPLETED_SETS = "completed_sets"
        private const val NO_ACTIVE_WORKOUT = -1L
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Write ─────────────────────────────────────────────────────────────────

    fun saveRoutineId(routineId: Long) {
        prefs.edit().putLong(KEY_ROUTINE_ID, routineId).apply()
    }

    fun saveStartedAt(startedAt: Long) {
        prefs.edit().putLong(KEY_STARTED_AT, startedAt).apply()
    }

    /**
     * Serializa el estado de parámetros del SetParameterStateManager.
     * Formato: { "setId": { "exerciseId": Long, "parameters": { "paramId": { reps, num, dur, int } } } }
     */
    fun saveParamState(state: Map<Long, Map<String, Any?>>) {
        try {
            val root = JSONObject()
            state.forEach { (setId, setData) ->
                val setObj = JSONObject()
                setObj.put("exerciseId", setData["exerciseId"] as? Long ?: 0L)

                val paramsObj = JSONObject()
                @Suppress("UNCHECKED_CAST")
                (setData["parameters"] as? Map<Long, Map<String, Any?>>)?.forEach { (paramId, values) ->
                    val paramObj = JSONObject()
                    values["repetitions"]?.let { paramObj.put("repetitions", it) }
                    values["numericValue"]?.let { paramObj.put("numericValue", it) }
                    values["durationValue"]?.let { paramObj.put("durationValue", it) }
                    values["integerValue"]?.let { paramObj.put("integerValue", it) }
                    paramsObj.put(paramId.toString(), paramObj)
                }
                setObj.put("parameters", paramsObj)
                root.put(setId.toString(), setObj)
            }
            prefs.edit().putString(KEY_PARAM_STATE, root.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "SAVE_PARAM_STATE_FAILED | error=${e.message}", e)
        }
    }

    /**
     * Persiste el conjunto de setIds que el usuario marcó como completados.
     */
    fun saveCompletedSets(completedSetIds: Set<Long>) {
        try {
            val arr = JSONArray()
            completedSetIds.forEach { arr.put(it) }
            prefs.edit().putString(KEY_COMPLETED_SETS, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "SAVE_COMPLETED_SETS_FAILED | error=${e.message}", e)
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    fun getRoutineId(): Long = prefs.getLong(KEY_ROUTINE_ID, NO_ACTIVE_WORKOUT)

    fun getStartedAt(): Long = prefs.getLong(KEY_STARTED_AT, System.currentTimeMillis())

    fun hasActiveWorkout(routineId: Long): Boolean =
        prefs.getLong(KEY_ROUTINE_ID, NO_ACTIVE_WORKOUT) == routineId

    /**
     * Devuelve el estado de parámetros en el mismo formato que exportState()
     * del SetParameterStateManager, o mapa vacío si no hay nada guardado.
     */
    fun loadParamState(): Map<Long, Map<String, Any?>> {
        val json = prefs.getString(KEY_PARAM_STATE, null) ?: return emptyMap()
        return try {
            val root = JSONObject(json)
            val result = mutableMapOf<Long, Map<String, Any?>>()
            root.keys().forEach { setIdStr ->
                val setId = setIdStr.toLongOrNull() ?: return@forEach
                val setObj = root.getJSONObject(setIdStr)
                val exerciseId = setObj.getLong("exerciseId")
                val paramsObj = setObj.getJSONObject("parameters")
                val params = mutableMapOf<Long, Map<String, Any?>>()
                paramsObj.keys().forEach { paramIdStr ->
                    val paramId = paramIdStr.toLongOrNull() ?: return@forEach
                    val paramObj = paramsObj.getJSONObject(paramIdStr)
                    val values = mutableMapOf<String, Any?>()
                    if (paramObj.has("repetitions")) values["repetitions"] = paramObj.getInt("repetitions")
                    if (paramObj.has("numericValue")) values["numericValue"] = paramObj.getDouble("numericValue")
                    if (paramObj.has("durationValue")) values["durationValue"] = paramObj.getLong("durationValue")
                    if (paramObj.has("integerValue")) values["integerValue"] = paramObj.getInt("integerValue")
                    params[paramId] = values
                }
                result[setId] = mapOf("exerciseId" to exerciseId, "parameters" to params)
            }
            Log.i(TAG, "PARAM_STATE_LOADED | sets=${result.size}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "LOAD_PARAM_STATE_FAILED | error=${e.message}", e)
            emptyMap()
        }
    }

    fun loadCompletedSets(): Set<Long> {
        val json = prefs.getString(KEY_COMPLETED_SETS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getLong(it) }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "LOAD_COMPLETED_SETS_FAILED | error=${e.message}", e)
            emptySet()
        }
    }

    // ── Clear ─────────────────────────────────────────────────────────────────

    /**
     * Llamar al guardar correctamente o al abandonar el entrenamiento.
     */
    fun clear() {
        Log.i(TAG, "CACHE_CLEARED")
        prefs.edit().clear().apply()
    }
}
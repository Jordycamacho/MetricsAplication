package com.fitapp.appfit.feature.routine.util

import android.content.Context

/**
 * Tracks when the local training tree for a routine must be refreshed from the server
 * (e.g. after exercise CRUD in the edit view).
 */
object TrainingCachePreferences {

    private const val PREFS_NAME = "training_cache_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun markNeedsRefresh(context: Context, routineId: Long) {
        prefs(context).edit().putBoolean(key(routineId), true).apply()
    }

    fun clearNeedsRefresh(context: Context, routineId: Long) {
        prefs(context).edit().remove(key(routineId)).apply()
    }

    fun needsRefresh(context: Context, routineId: Long): Boolean =
        prefs(context).getBoolean(key(routineId), false)

    private fun key(routineId: Long) = "needs_refresh_$routineId"
}

package com.fitapp.appfit.utils

import android.content.Context

object WorkoutPreferences {

    private const val PREFS_NAME = "workout_preferences"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_SOUND_TYPE = "sound_type"

    enum class SoundType { BEEP, BELL, CHIME }

    fun isVibrationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VIBRATION_ENABLED, true)

    fun isSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_ENABLED, false)

    fun getSoundType(context: Context): SoundType =
        SoundType.valueOf(
            prefs(context).getString(KEY_SOUND_TYPE, SoundType.BEEP.name) ?: SoundType.BEEP.name
        )

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).commit()
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).commit()
    }

    fun setSoundType(context: Context, type: SoundType) {
        prefs(context).edit().putString(KEY_SOUND_TYPE, type.name).commit()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
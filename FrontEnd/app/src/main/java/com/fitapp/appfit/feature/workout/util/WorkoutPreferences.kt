package com.fitapp.appfit.feature.workout.util

import android.content.Context

object WorkoutPreferences {

    private const val PREFS_NAME = "workout_preferences"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_SOUND_TYPE = "sound_type"
    private const val KEY_SET_VIEW_TYPE = "set_view_type"
    private const val KEY_TIMER_VOLUME = "timer_volume"

    private const val KEY_SET_REST_SOUND = "set_rest_sound"
    private const val KEY_EXERCISE_REST_SOUND = "exercise_rest_sound"
    private const val KEY_DURATION_COMPLETE_SOUND = "duration_complete_sound"

    // Durante el entrenamiento
    private const val KEY_FILTER_TODAY_ON_START = "filter_today_on_start"
    private const val KEY_AUTO_REST_ENABLED = "auto_rest_enabled"
    private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    private const val KEY_DEFAULT_REST_SECONDS = "default_rest_seconds"
    private const val KEY_EXPAND_ACTIVE_ONLY = "expand_active_only"
    private const val KEY_LAST_FILTER_MODE = "last_filter_mode"
    private const val KEY_LAST_FILTER_SESSION = "last_filter_session"
    private const val KEY_LAST_FILTER_DAY = "last_filter_day"

    enum class SoundType { BEEP, BELL, CHIME }
    enum class SetViewType { CLASSIC, MODERN }

    enum class TimerSoundType {
        SET_REST,
        EXERCISE_REST,
        DURATION_COMPLETE
    }

    enum class SoundVariant {
        BEEP, BELL, CHIME, BUZZ, PING
    }

    enum class WorkoutFilterMode {
        ALL, TODAY, SESSION, DAY
    }

    // ── Vibración ──
    fun isVibrationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VIBRATION_ENABLED, true)

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    // ── Sonido (global) ──
    fun isSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_ENABLED, false)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    // ── Volumen (global, 0-100) ──
    fun getTimerVolume(context: Context): Int =
        prefs(context).getInt(KEY_TIMER_VOLUME, 70)

    fun setTimerVolume(context: Context, volume: Int) {
        val clamped = volume.coerceIn(0, 100)
        prefs(context).edit().putInt(KEY_TIMER_VOLUME, clamped).apply()
    }

    // ── Sonidos por tipo de contador ──
    fun getTimerSound(context: Context, timerType: TimerSoundType): SoundVariant {
        val key = when (timerType) {
            TimerSoundType.SET_REST -> KEY_SET_REST_SOUND
            TimerSoundType.EXERCISE_REST -> KEY_EXERCISE_REST_SOUND
            TimerSoundType.DURATION_COMPLETE -> KEY_DURATION_COMPLETE_SOUND
        }
        return SoundVariant.valueOf(
            prefs(context).getString(key, SoundVariant.BEEP.name) ?: SoundVariant.BEEP.name
        )
    }

    fun setTimerSound(context: Context, timerType: TimerSoundType, variant: SoundVariant) {
        val key = when (timerType) {
            TimerSoundType.SET_REST -> KEY_SET_REST_SOUND
            TimerSoundType.EXERCISE_REST -> KEY_EXERCISE_REST_SOUND
            TimerSoundType.DURATION_COMPLETE -> KEY_DURATION_COMPLETE_SOUND
        }
        prefs(context).edit().putString(key, variant.name).apply()
    }

    // ── Vista de sets ──
    fun getSetViewType(context: Context): SetViewType =
        SetViewType.valueOf(
            prefs(context).getString(KEY_SET_VIEW_TYPE, SetViewType.MODERN.name) ?: SetViewType.MODERN.name
        )

    fun setSetViewType(context: Context, type: SetViewType) {
        prefs(context).edit().putString(KEY_SET_VIEW_TYPE, type.name).apply()
    }

    // ── Durante el entrenamiento ──
    fun isFilterTodayOnStart(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FILTER_TODAY_ON_START, true)

    fun setFilterTodayOnStart(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FILTER_TODAY_ON_START, enabled).apply()
    }

    fun isAutoRestEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_REST_ENABLED, false)

    fun setAutoRestEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_REST_ENABLED, enabled).apply()
    }

    fun isKeepScreenOn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_KEEP_SCREEN_ON, true)

    fun setKeepScreenOn(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }

    fun getDefaultRestSeconds(context: Context): Int =
        prefs(context).getInt(KEY_DEFAULT_REST_SECONDS, 60).coerceIn(0, 180)

    fun setDefaultRestSeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_DEFAULT_REST_SECONDS, seconds.coerceIn(0, 180)).apply()
    }

    fun isExpandActiveOnly(context: Context): Boolean =
        prefs(context).getBoolean(KEY_EXPAND_ACTIVE_ONLY, false)

    fun setExpandActiveOnly(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_EXPAND_ACTIVE_ONLY, enabled).apply()
    }

    fun getLastFilterMode(context: Context): WorkoutFilterMode =
        WorkoutFilterMode.valueOf(
            prefs(context).getString(KEY_LAST_FILTER_MODE, WorkoutFilterMode.ALL.name)
                ?: WorkoutFilterMode.ALL.name
        )

    fun setLastFilterMode(context: Context, mode: WorkoutFilterMode) {
        prefs(context).edit().putString(KEY_LAST_FILTER_MODE, mode.name).apply()
    }

    fun getLastFilterSession(context: Context): Int =
        prefs(context).getInt(KEY_LAST_FILTER_SESSION, 1)

    fun setLastFilterSession(context: Context, session: Int) {
        prefs(context).edit().putInt(KEY_LAST_FILTER_SESSION, session.coerceAtLeast(1)).apply()
    }

    fun getLastFilterDay(context: Context): String? =
        prefs(context).getString(KEY_LAST_FILTER_DAY, null)

    fun setLastFilterDay(context: Context, dayOfWeek: String?) {
        prefs(context).edit().apply {
            if (dayOfWeek.isNullOrBlank()) remove(KEY_LAST_FILTER_DAY)
            else putString(KEY_LAST_FILTER_DAY, dayOfWeek)
        }.apply()
    }

    // ── Legacy (deprecated) ──
    @Deprecated("Use getTimerSound instead")
    fun getSoundType(context: Context): SoundType =
        SoundType.valueOf(
            prefs(context).getString(KEY_SOUND_TYPE, SoundType.BEEP.name) ?: SoundType.BEEP.name
        )

    @Deprecated("Use setTimerSound instead")
    fun setSoundType(context: Context, type: SoundType) {
        prefs(context).edit().putString(KEY_SOUND_TYPE, type.name).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

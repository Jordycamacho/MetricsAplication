package com.fitapp.appfit.feature.workout.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences

object WorkoutHaptics {

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun exerciseStart(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 80, 60, 80))
    }

    fun setComplete(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 150))
    }

    fun restFinished(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 100, 80, 100, 80, 100))
    }

    fun exerciseComplete(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 400))
    }

    private fun vibrate(context: Context, pattern: LongArray) {
        val v = vibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(pattern, -1)
        }
    }
}
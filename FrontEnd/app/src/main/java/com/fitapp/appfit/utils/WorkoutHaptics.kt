package com.fitapp.appfit.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

object WorkoutHaptics {

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    /** Ejercicio iniciado — pulso corto doble */
    fun exerciseStart(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 80, 60, 80))
    }

    /** Serie/duración completada — pulso medio */
    fun setComplete(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 150))
    }

    /** Descanso terminado — pulso triple */
    fun restFinished(context: Context) {
        if (!WorkoutPreferences.isVibrationEnabled(context)) return
        vibrate(context, longArrayOf(0, 100, 80, 100, 80, 100))
    }

    /** Ejercicio completo — pulso largo */
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
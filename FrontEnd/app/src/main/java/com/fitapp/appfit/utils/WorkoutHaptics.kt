package com.fitapp.appfit.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

/**
 * Vibraciones y sonidos para el flujo de entrenamiento.
 * Usa VibrationEffect para API 26+, fallback para anteriores.
 */
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
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun exerciseStart(context: Context) =  vibrate(context, longArrayOf(0, 80, 60, 80))

    /** Serie/duración completada — pulso medio */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun setComplete(context: Context) = vibrate(context, longArrayOf(0, 150))

    /** Descanso terminado, siguiente serie — pulso triple */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun restFinished(context: Context) = vibrate(context, longArrayOf(0, 100, 80, 100, 80, 100))

    /** Ejercicio completo — pulso largo */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun exerciseComplete(context: Context) = vibrate(context, longArrayOf(0, 400))

    @RequiresPermission(Manifest.permission.VIBRATE)
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
package com.fitapp.appfit.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fitapp.appfit.MainActivity
import com.fitapp.appfit.R

object WorkoutNotificationManager {

    const val CHANNEL_ID = "workout_timer_channel"
    const val NOTIFICATION_ID_TIMER = 1001
    const val NOTIFICATION_ID_REST_DONE = 1002

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Temporizador de entrenamiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos del temporizador de descanso"
                enableVibration(true)
                setSound(null, null) // El sonido lo gestiona WorkoutSoundManager
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Notificación persistente del ForegroundService —
     * muestra el tiempo restante de descanso mientras la app está en segundo plano.
     */
    fun buildTimerNotification(
        context: Context,
        exerciseName: String,
        secondsLeft: Int
    ) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_check) // reemplaza con tu icono de fitness
        .setContentTitle("Descanso — $exerciseName")
        .setContentText(formatSeconds(secondsLeft))
        .setOngoing(true)
        .setSilent(true)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(openAppIntent(context))
        .build()

    /**
     * Notificación de fin de descanso — aparece aunque la app esté en segundo plano.
     */
    fun notifyRestFinished(context: Context, exerciseName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle("¡Descanso terminado!")
            .setContentText("Continúa con $exerciseName")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent(context))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REST_DONE, notification)
        } catch (e: SecurityException) {
        }
    }

    fun cancelTimerNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_TIMER)
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun formatSeconds(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return if (m > 0) "${m}m ${s.toString().padStart(2, '0')}s" else "${s}s"
    }
}
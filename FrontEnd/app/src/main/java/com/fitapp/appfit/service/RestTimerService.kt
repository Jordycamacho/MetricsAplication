package com.fitapp.appfit.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import com.fitapp.appfit.utils.WorkoutHaptics
import com.fitapp.appfit.utils.WorkoutNotificationManager
import com.fitapp.appfit.utils.WorkoutPreferences
import com.fitapp.appfit.utils.WorkoutSoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class RestTimerService : Service() {

    companion object {
        const val ACTION_START = "com.fitapp.appfit.REST_TIMER_START"
        const val ACTION_STOP  = "com.fitapp.appfit.REST_TIMER_STOP"
        const val EXTRA_SECONDS       = "extra_seconds"
        const val EXTRA_EXERCISE_NAME = "extra_exercise_name"

        fun startTimer(context: Context, seconds: Int, exerciseName: String) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
                putExtra(EXTRA_EXERCISE_NAME, exerciseName)
            }
            context.startForegroundService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    // Binder para comunicación con el fragment cuando la app está en primer plano
    inner class LocalBinder : Binder() {
        fun getService() = this@RestTimerService
    }

    private val binder = LocalBinder()
    private var timer: CountDownTimer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Callback que el fragment puede registrar para recibir ticks en primer plano
    var onTick: ((Int) -> Unit)? = null
    var onFinish: (() -> Unit)? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        WorkoutNotificationManager.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 0)
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Ejercicio"
                startCountdown(seconds, exerciseName)
            }
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startCountdown(totalSeconds: Int, exerciseName: String) {
        timer?.cancel()

        // Iniciar como foreground con notificación persistente
        val notification = WorkoutNotificationManager
            .buildTimerNotification(this, exerciseName, totalSeconds)
        startForeground(WorkoutNotificationManager.NOTIFICATION_ID_TIMER, notification)

        timer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1

                // Actualizar notificación
                val updatedNotification = WorkoutNotificationManager
                    .buildTimerNotification(this@RestTimerService, exerciseName, secondsLeft)
                startForeground(WorkoutNotificationManager.NOTIFICATION_ID_TIMER, updatedNotification)

                // Notificar al fragment si está en primer plano
                scope.launch { onTick?.invoke(secondsLeft) }
            }

            override fun onFinish() {
                Timber.i("RestTimerService: countdown terminado para $exerciseName")

                // Vibración (respeta preferencias)
                if (WorkoutPreferences.isVibrationEnabled(this@RestTimerService)) {
                    WorkoutHaptics.restFinished(this@RestTimerService)
                }

                // Sonido en hilo de fondo (ToneGenerator bloquea)
                scope.launch(Dispatchers.IO) {
                    WorkoutSoundManager.playRestFinished(this@RestTimerService)
                }

                // Notificación de fin
                WorkoutNotificationManager.notifyRestFinished(this@RestTimerService, exerciseName)

                // Callback al fragment
                scope.launch { onFinish?.invoke() }

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null
        super.onDestroy()
    }
}
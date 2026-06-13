package com.fitapp.appfit.feature.workout.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import com.fitapp.appfit.core.notification.WorkoutNotificationManager
import com.fitapp.appfit.feature.workout.util.TimerSoundPlayer
import com.fitapp.appfit.feature.workout.util.WorkoutHaptics
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class RestTimerService : Service() {

    companion object {
        const val ACTION_START = "com.fitapp.appfit.REST_TIMER_START"
        const val ACTION_STOP = "com.fitapp.appfit.REST_TIMER_STOP"
        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_EXERCISE_NAME = "extra_exercise_name"
        const val EXTRA_SOUND_TYPE = "extra_sound_type"

        @Volatile
        private var isRunning = false

        fun startTimer(
            context: Context,
            seconds: Int,
            exerciseName: String,
            soundType: WorkoutPreferences.TimerSoundType = WorkoutPreferences.TimerSoundType.SET_REST
        ) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
                putExtra(EXTRA_EXERCISE_NAME, exerciseName)
                putExtra(EXTRA_SOUND_TYPE, soundType.name)
            }
            context.startForegroundService(intent)
        }

        fun stopTimer(context: Context) {
            if (!isRunning) return
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@RestTimerService
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var handlerThread: HandlerThread? = null
    private var timerHandler: Handler? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var timerToken = 0
    private var endAtRealtime = 0L
    private var exerciseName = "Ejercicio"
    private var soundType = WorkoutPreferences.TimerSoundType.SET_REST
    private var inForeground = false

    var onTick: ((Int) -> Unit)? = null
    var onFinish: (() -> Unit)? = null

    private val tickRunnable = object : Runnable {
        override fun run() {
            val token = timerToken
            val remainingMs = endAtRealtime - SystemClock.elapsedRealtime()
            if (remainingMs <= 0L) {
                finishCountdown(token)
                return
            }

            val secondsLeft = ((remainingMs + 999) / 1000).toInt()
            updateForegroundNotification(secondsLeft)
            scope.launch { onTick?.invoke(secondsLeft) }

            val delay = (remainingMs % 1000).coerceIn(200L, 1000L)
            timerHandler?.postDelayed(this, delay)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        WorkoutNotificationManager.createChannel(this)
        TimerSoundPlayer.init(this)
        ensureWorkerThread()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 0)
                exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Ejercicio"
                val soundTypeName = intent.getStringExtra(EXTRA_SOUND_TYPE)
                soundType = soundTypeName?.let {
                    runCatching { WorkoutPreferences.TimerSoundType.valueOf(it) }.getOrNull()
                } ?: WorkoutPreferences.TimerSoundType.SET_REST

                // Obligatorio: startForeground antes de cualquier otro trabajo
                enterForeground(seconds.coerceAtLeast(1))
                startCountdown(seconds)
            }
            ACTION_STOP -> {
                cancelCountdown()
                leaveForeground()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun ensureWorkerThread() {
        if (handlerThread == null) {
            handlerThread = HandlerThread("RestTimerWorker").apply { start() }
            timerHandler = Handler(handlerThread!!.looper)
        }
    }

    private fun enterForeground(secondsLeft: Int) {
        if (!inForeground) {
            val notification = WorkoutNotificationManager
                .buildTimerNotification(this, exerciseName, secondsLeft)
            startForeground(WorkoutNotificationManager.NOTIFICATION_ID_TIMER, notification)
            inForeground = true
            isRunning = true
        }
    }

    private fun updateForegroundNotification(secondsLeft: Int) {
        if (!inForeground) return
        val notification = WorkoutNotificationManager
            .buildTimerNotification(this, exerciseName, secondsLeft)
        startForeground(WorkoutNotificationManager.NOTIFICATION_ID_TIMER, notification)
    }

    private fun leaveForeground() {
        if (inForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            inForeground = false
        }
        isRunning = false
        WorkoutNotificationManager.cancelTimerNotification(this)
    }

    private fun acquireWakeLock(maxSeconds: Int) {
        releaseWakeLock()
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jnobfit:RestTimer").apply {
            acquire((maxSeconds + 30).coerceAtMost(600) * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun startCountdown(totalSeconds: Int) {
        timerHandler?.removeCallbacks(tickRunnable)
        releaseWakeLock()

        if (totalSeconds <= 0) {
            finishCountdown(++timerToken)
            return
        }

        val token = ++timerToken
        endAtRealtime = SystemClock.elapsedRealtime() + totalSeconds * 1000L
        acquireWakeLock(totalSeconds)
        updateForegroundNotification(totalSeconds)
        timerHandler?.post(tickRunnable)
    }

    private fun finishCountdown(token: Int) {
        if (token != timerToken) return

        timerHandler?.removeCallbacks(tickRunnable)
        releaseWakeLock()

        Timber.i("RestTimerService: countdown terminado para $exerciseName")

        if (WorkoutPreferences.isVibrationEnabled(this)) {
            WorkoutHaptics.restFinished(this)
        }

        TimerSoundPlayer.playTimerSound(this, soundType)
        WorkoutNotificationManager.notifyRestFinished(this, exerciseName)

        scope.launch { onFinish?.invoke() }

        leaveForeground()
        stopSelf()
    }

    private fun cancelCountdown() {
        timerHandler?.removeCallbacks(tickRunnable)
        releaseWakeLock()
        timerToken++
    }

    override fun onDestroy() {
        cancelCountdown()
        leaveForeground()
        handlerThread?.quitSafely()
        handlerThread = null
        timerHandler = null
        TimerSoundPlayer.release()
        super.onDestroy()
    }
}

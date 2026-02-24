package com.fitapp.appfit.timer

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper

class RestTimer(
    private val onTick: (Int) -> Unit,
    private val onFinish: () -> Unit
) {
    private var timer: CountDownTimer? = null
    private var currentToken = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    fun start(seconds: Int) {
        stop()
        val token = ++currentToken

        // Ejecutar en background thread para no bloquear UI
        timer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (token != currentToken) return
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                mainHandler.post {
                    if (token == currentToken) {
                        onTick(secondsLeft)
                    }
                }
            }

            override fun onFinish() {
                if (token != currentToken) return
                mainHandler.post {
                    if (token == currentToken) {
                        onFinish()
                    }
                }
            }
        }.start()
    }

    fun stop() {
        currentToken++
        timer?.cancel()
        timer = null
    }
}
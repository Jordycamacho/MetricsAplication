package com.fitapp.appfit.core.util

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
        if (seconds <= 0) {
            mainHandler.post { onFinish() }
            return
        }
        val token = ++currentToken

        timer = object : CountDownTimer(seconds * 1000L, 100L) {
            private var lastSecondEmitted = seconds + 1

            override fun onTick(millisUntilFinished: Long) {
                if (token != currentToken) return
                val secondsLeft = ((millisUntilFinished + 50) / 1000).toInt()
                    .coerceAtLeast(1)
                if (secondsLeft != lastSecondEmitted) {
                    lastSecondEmitted = secondsLeft
                    mainHandler.post {
                        if (token == currentToken) onTick(secondsLeft)
                    }
                }
            }

            override fun onFinish() {
                if (token != currentToken) return
                mainHandler.post {
                    if (token == currentToken) this@RestTimer.onFinish()
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
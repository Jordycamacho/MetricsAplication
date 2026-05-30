package com.fitapp.appfit.feature.workout.util

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

/**
 * Toque en botón +/- : un paso por tap; mantener pulsado repite (+1/-1) cada vez más rápido.
 */
class WorkoutRepeatButtonHelper {

    private val handler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null
    private var holding = false
    private var steppedWhileHeld = false

    fun clear() {
        repeatRunnable?.let { handler.removeCallbacks(it) }
        repeatRunnable = null
        holding = false
        steppedWhileHeld = false
    }

    fun attach(button: View, onStep: () -> Unit) {
        button.setOnClickListener(null)
        button.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    clear()
                    holding = true
                    onStep()
                    steppedWhileHeld = true
                    startRepeat(onStep)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    repeatRunnable?.let { handler.removeCallbacks(it) }
                    holding = false

                    if (!steppedWhileHeld) {
                        onStep()
                    }

                    steppedWhileHeld = false
                    false
                }

                else -> false
            }
        }
    }

    private fun startRepeat(onStep: () -> Unit) {
        var delay = INITIAL_DELAY_MS
        repeatRunnable = object : Runnable {
            override fun run() {
                if (!holding) return
                onStep()
                steppedWhileHeld = true
                delay = (delay * ACCELERATION).toLong().coerceAtLeast(MIN_DELAY_MS)
                handler.postDelayed(this, delay)
            }
        }
        handler.postDelayed(repeatRunnable!!, delay)
    }

    companion object {
        private const val INITIAL_DELAY_MS = 350L
        private const val MIN_DELAY_MS = 60L
        private const val ACCELERATION = 0.82
    }
}

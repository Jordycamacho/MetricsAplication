package com.fitapp.appfit.feature.workout.presentation.execution

import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.workout.util.WorkoutPreferences

/**
 * Configuración en tiempo de ejecución compartida entre adapters.
 */
data class WorkoutExecutionConfig(
    var autoRestEnabled: Boolean = false,
    var defaultRestSeconds: Int = 60,
    var expandActiveOnly: Boolean = false,
    var onAutoRestRequested: ((seconds: Int, label: String, set: RoutineSetTemplateResponse) -> Unit)? = null,
    var onSetCompleted: (() -> Unit)? = null,
    var onWorkoutTimerStart: ((
        seconds: Int,
        label: String,
        soundType: WorkoutPreferences.TimerSoundType,
        onTick: (Int) -> Unit,
        onFinish: () -> Unit
    ) -> Unit)? = null,
    var onWorkoutTimerStop: (() -> Unit)? = null
) {
    private var timerOwner: Any? = null

    fun resolveRestSeconds(set: RoutineSetTemplateResponse): Int {
        val configured = set.restAfterSet ?: 0
        return if (configured > 0) configured else defaultRestSeconds
    }

    fun triggerAutoRestIfNeeded(set: RoutineSetTemplateResponse, exerciseName: String, completed: Boolean) {
        if (!completed || !autoRestEnabled) return
        val rest = resolveRestSeconds(set)
        if (rest <= 0) return
        onAutoRestRequested?.invoke(rest, exerciseName, set)
    }

    fun startWorkoutTimer(
        owner: Any,
        seconds: Int,
        label: String,
        soundType: WorkoutPreferences.TimerSoundType,
        onTick: (Int) -> Unit,
        onFinish: () -> Unit
    ) {
        timerOwner = owner
        onWorkoutTimerStart?.invoke(seconds, label, soundType, onTick, onFinish)
    }

    fun stopWorkoutTimer(owner: Any) {
        if (timerOwner != owner) return
        timerOwner = null
        onWorkoutTimerStop?.invoke()
    }

    fun stopAnyWorkoutTimer() {
        if (timerOwner == null) return
        timerOwner = null
        onWorkoutTimerStop?.invoke()
    }
}

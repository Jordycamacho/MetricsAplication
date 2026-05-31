package com.fitapp.appfit.feature.workout.presentation.execution

import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse

/**
 * Configuración en tiempo de ejecución compartida entre adapters.
 */
data class WorkoutExecutionConfig(
    var autoRestEnabled: Boolean = false,
    var defaultRestSeconds: Int = 60,
    var expandActiveOnly: Boolean = false,
    var onAutoRestRequested: ((seconds: Int, label: String, set: RoutineSetTemplateResponse) -> Unit)? = null,
    var onSetCompleted: (() -> Unit)? = null
) {
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
}

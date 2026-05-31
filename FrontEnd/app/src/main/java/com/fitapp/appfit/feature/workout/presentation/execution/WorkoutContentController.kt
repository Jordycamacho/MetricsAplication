package com.fitapp.appfit.feature.workout.presentation.execution

import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.workout.domain.model.WorkoutCompletionState

/**
 * Contrato para controllers de contenido intercambiables (post-v1).
 * v1: la implementación efectiva es [WorkoutDayAdapter] vía [SetsWorkoutContentController].
 */
interface WorkoutContentController {
    fun bindRoutine(routine: RoutineResponse)
    fun getProgress(completionState: WorkoutCompletionState): Pair<Int, Int>
    fun focusNextIncomplete(completionState: WorkoutCompletionState): Boolean
    fun expandAll()
    fun collapseAll()
}

/**
 * Wrapper sobre WorkoutDayAdapter — punto de extensión para Intervalos/Segmentos/Circuito.
 */
class SetsWorkoutContentController(
    private val dayAdapter: WorkoutDayAdapter
) : WorkoutContentController {

    override fun bindRoutine(routine: RoutineResponse) {
        dayAdapter.submitRoutine(routine)
    }

    override fun getProgress(completionState: WorkoutCompletionState): Pair<Int, Int> {
        return dayAdapter.getProgress(completionState)
    }

    override fun focusNextIncomplete(completionState: WorkoutCompletionState): Boolean {
        return dayAdapter.focusNextIncomplete(completionState)
    }

    override fun expandAll() = dayAdapter.expandAll()
    override fun collapseAll() = dayAdapter.collapseAll()
}

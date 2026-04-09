package com.fitapp.appfit.feature.workout.model

/**
 * Modelo para trackear el estado de completado durante una sesión de workout.
 *
 * Estructura jerárquica:
 * - Día (dayOfWeek) puede marcar todos sus ejercicios
 * - Ejercicio (routineExerciseId) puede marcar todos sus sets
 * - Set individual (setTemplateId) marcado/desmarcado
 */
data class WorkoutCompletionState(
    private val completedSets: MutableMap<Long, Boolean> = mutableMapOf(),

    private val completedExercises: MutableMap<Long, Boolean> = mutableMapOf(),

    private val completedDays: MutableMap<String, Boolean> = mutableMapOf(),

    private val exercisesByDay: MutableMap<String, List<Long>> = mutableMapOf(),

    private val setsByExercise: MutableMap<Long, List<Long>> = mutableMapOf()
) {

    // ── SET LEVEL ────────────────────────────────────────────────────────────

    fun isSetCompleted(setId: Long): Boolean = completedSets[setId] ?: false

    fun toggleSet(setId: Long, exerciseId: Long): Boolean {
        val newState = !isSetCompleted(setId)
        completedSets[setId] = newState

        recalculateExerciseCompletion(exerciseId)

        return newState
    }

    fun markSetCompleted(setId: Long, exerciseId: Long, completed: Boolean) {
        completedSets[setId] = completed
        recalculateExerciseCompletion(exerciseId)
    }

    // ── EXERCISE LEVEL ───────────────────────────────────────────────────────

    fun isExerciseCompleted(exerciseId: Long): Boolean {
        val sets = setsByExercise[exerciseId] ?: return false
        if (sets.isEmpty()) return false

        return sets.any { completedSets[it] == true }
    }

    fun toggleExercise(exerciseId: Long, dayOfWeek: String): Boolean {
        val sets = setsByExercise[exerciseId] ?: emptyList()
        if (sets.isEmpty()) return false

        val shouldComplete = sets.none { completedSets[it] == true }

        sets.forEach { setId ->
            completedSets[setId] = shouldComplete
        }

        completedExercises[exerciseId] = shouldComplete
        recalculateDayCompletion(dayOfWeek)

        return shouldComplete
    }

    private fun recalculateExerciseCompletion(exerciseId: Long) {
        val sets = setsByExercise[exerciseId] ?: emptyList()
        completedExercises[exerciseId] = sets.any { completedSets[it] == true }
    }

    // ── DAY LEVEL ────────────────────────────────────────────────────────────

    fun isDayCompleted(dayOfWeek: String): Boolean {
        val exercises = exercisesByDay[dayOfWeek] ?: return false
        if (exercises.isEmpty()) return false

        return exercises.any { isExerciseCompleted(it) }
    }

    fun toggleDay(dayOfWeek: String): Boolean {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList()
        if (exercises.isEmpty()) return false

        val shouldComplete = exercises.none { isExerciseCompleted(it) }

        exercises.forEach { exerciseId ->
            val sets = setsByExercise[exerciseId] ?: emptyList()
            sets.forEach { setId ->
                completedSets[setId] = shouldComplete
            }
            completedExercises[exerciseId] = shouldComplete
        }

        completedDays[dayOfWeek] = shouldComplete
        return shouldComplete
    }

    private fun recalculateDayCompletion(dayOfWeek: String) {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList()
        completedDays[dayOfWeek] = exercises.any { isExerciseCompleted(it) }
    }

    // ── INITIALIZATION ───────────────────────────────────────────────────────

    fun registerSet(setId: Long, exerciseId: Long) {
        if (!setsByExercise.containsKey(exerciseId)) {
            setsByExercise[exerciseId] = mutableListOf()
        }
        (setsByExercise[exerciseId] as MutableList).add(setId)

        if (!completedSets.containsKey(setId)) {
            completedSets[setId] = false
        }
    }

    fun registerExercise(exerciseId: Long, dayOfWeek: String) {
        if (!exercisesByDay.containsKey(dayOfWeek)) {
            exercisesByDay[dayOfWeek] = mutableListOf()
        }
        if (!exercisesByDay[dayOfWeek]!!.contains(exerciseId)) {
            (exercisesByDay[dayOfWeek] as MutableList).add(exerciseId)
        }
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    fun getCompletedSetsCount(exerciseId: Long): Int {
        val sets = setsByExercise[exerciseId] ?: emptyList()
        return sets.count { completedSets[it] == true }
    }

    fun getTotalSetsCount(exerciseId: Long): Int {
        return setsByExercise[exerciseId]?.size ?: 0
    }

    fun getCompletedExercisesCount(dayOfWeek: String): Int {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList()
        return exercises.count { isExerciseCompleted(it) }
    }

    fun getTotalExercisesCount(dayOfWeek: String): Int {
        return exercisesByDay[dayOfWeek]?.size ?: 0
    }

    fun getAllCompletedSets(): Set<Long> {
        return completedSets.filterValues { it }.keys
    }

    fun getSkippedSets(): Set<Long> {
        return completedSets.filterValues { !it }.keys
    }

    fun hasAnyCompletedSets(): Boolean {
        return completedSets.values.any { it }
    }

    // ── DEBUG ────────────────────────────────────────────────────────────────

    fun debugPrint() {
        println("=== WORKOUT COMPLETION STATE ===")
        println("Completed sets: ${completedSets.filterValues { it }.size}/${completedSets.size}")
        println("Completed exercises: ${completedExercises.filterValues { it }.size}/${completedExercises.size}")
        println("Completed days: ${completedDays.filterValues { it }.size}/${completedDays.size}")
    }
}
package com.fitapp.appfit.feature.workout.domain.model

/**
 * Tracks completion state for sets, exercises, and days during a workout session.
 *
 */
data class WorkoutCompletionState(
    private val completedSets: MutableMap<Long, Boolean> = mutableMapOf(),
    private val completedExercises: MutableMap<Long, Boolean> = mutableMapOf(), // clave = routineExerciseId
    private val completedDays: MutableMap<String, Boolean> = mutableMapOf(),
    private val exercisesByDay: MutableMap<String, MutableList<Long>> = mutableMapOf(), // valor = list of routineExerciseId
    private val setsByExercise: MutableMap<Long, MutableList<Long>> = mutableMapOf(), // clave = routineExerciseId
    private val exerciseDayMap: MutableMap<Long, MutableSet<String>> = mutableMapOf() // clave = routineExerciseId
) {

    // ── Set level ─────────────────────────────────────────────────────────────

    fun isSetCompleted(setId: Long): Boolean = completedSets[setId] ?: false

    fun toggleSet(setId: Long, routineExerciseId: Long): Boolean {
        val newState = !isSetCompleted(setId)
        completedSets[setId] = newState
        recalculateExerciseCompletion(routineExerciseId)
        recalculateDayCompletionForExercise(routineExerciseId)
        return newState
    }

    fun markSetCompleted(setId: Long, routineExerciseId: Long, completed: Boolean) {
        completedSets[setId] = completed
        if (routineExerciseId != 0L) {
            recalculateExerciseCompletion(routineExerciseId)
            recalculateDayCompletionForExercise(routineExerciseId)
        }
    }

    // ── Exercise level (usando routineExerciseId) ─────────────────────────────

    fun isExerciseCompleted(routineExerciseId: Long): Boolean {
        val sets = setsByExercise[routineExerciseId] ?: return false
        if (sets.isEmpty()) return false
        return sets.any { completedSets[it] == true }
    }

    /**
     * Asigna directamente el estado completado a todos los sets de este ejercicio.
     */
    fun setExerciseCompleted(routineExerciseId: Long, completed: Boolean) {
        val sets = setsByExercise[routineExerciseId] ?: return
        sets.forEach { setId -> completedSets[setId] = completed }
        completedExercises[routineExerciseId] = completed

        val days = exerciseDayMap[routineExerciseId] ?: return
        days.forEach { day -> recalculateDayCompletion(day) }
    }

    private fun recalculateExerciseCompletion(routineExerciseId: Long) {
        val sets = setsByExercise[routineExerciseId] ?: emptyList()
        completedExercises[routineExerciseId] = sets.any { completedSets[it] == true }
    }

    private fun recalculateDayCompletionForExercise(routineExerciseId: Long) {
        val days = exerciseDayMap[routineExerciseId] ?: return
        days.forEach { day -> recalculateDayCompletion(day) }
    }

    // ── Day level ─────────────────────────────────────────────────────────────

    fun isDayCompleted(dayOfWeek: String): Boolean {
        val exercises = exercisesByDay[dayOfWeek] ?: return false
        if (exercises.isEmpty()) return false
        return exercises.any { isExerciseCompleted(it) }
    }

    /**
     * Marca todos los ejercicios de un día como completados o no.
     */
    fun setDayCompleted(dayOfWeek: String, completed: Boolean) {
        val exercises = exercisesByDay[dayOfWeek] ?: return
        exercises.forEach { routineExerciseId -> setExerciseCompleted(routineExerciseId, completed) }
        completedDays[dayOfWeek] = completed
    }

    private fun recalculateDayCompletion(dayOfWeek: String) {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList()
        completedDays[dayOfWeek] = exercises.any { isExerciseCompleted(it) }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registra un set asociado a un ejercicio de rutina (routineExerciseId).
     */
    fun registerSet(setId: Long, routineExerciseId: Long) {
        setsByExercise.getOrPut(routineExerciseId) { mutableListOf() }.apply {
            if (!contains(setId)) add(setId)
        }
        if (!completedSets.containsKey(setId)) {
            completedSets[setId] = false
        }
    }

    /**
     * Registra un ejercicio de rutina (con su ID único) en un día.
     */
    fun registerExercise(routineExerciseId: Long, dayOfWeek: String) {
        exercisesByDay.getOrPut(dayOfWeek) { mutableListOf() }.apply {
            if (!contains(routineExerciseId)) add(routineExerciseId)
        }
        exerciseDayMap.getOrPut(routineExerciseId) { mutableSetOf() }.add(dayOfWeek)
        if (!completedExercises.containsKey(routineExerciseId)) {
            completedExercises[routineExerciseId] = false
        }
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    fun getCompletedSetsCount(routineExerciseId: Long): Int =
        setsByExercise[routineExerciseId]?.count { completedSets[it] == true } ?: 0

    fun getTotalSetsCount(routineExerciseId: Long): Int =
        setsByExercise[routineExerciseId]?.size ?: 0

    fun getCompletedExercisesCount(dayOfWeek: String): Int =
        exercisesByDay[dayOfWeek]?.count { isExerciseCompleted(it) } ?: 0

    fun getTotalExercisesCount(dayOfWeek: String): Int =
        exercisesByDay[dayOfWeek]?.size ?: 0

    fun getAllCompletedSets(): Set<Long> =
        completedSets.filterValues { it }.keys

    fun getSkippedSets(): Set<Long> =
        completedSets.filterValues { !it }.keys

    fun hasAnyCompletedSets(): Boolean =
        completedSets.values.any { it }

    fun reset() {
        completedSets.replaceAll { _, _ -> false }
        completedExercises.clear()
        completedDays.clear()
    }

    fun debugPrint() {
        println("=== WORKOUT COMPLETION STATE ===")
        println("Sets: ${completedSets.filterValues { it }.size}/${completedSets.size} completed")
        println("Exercises: ${completedExercises.filterValues { it }.size}/${completedExercises.size} completed")
        println("Days: ${completedDays.filterValues { it }.size}/${completedDays.size} completed")
    }
}
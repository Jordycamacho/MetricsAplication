package com.fitapp.appfit.feature.workout.domain.model

/**
 * Rastrea el estado de completado de sets, ejercicios y días durante el entrenamiento.
 *
 * Ciclo de vida esperado:
 *  1. registerExercise / registerSet  → al cargar la rutina
 *  2. markSetCompleted / toggleSet    → al interactuar
 *  3. resetValues                     → para limpiar checks sin borrar la estructura
 *  4. reset                           → al terminar/guardar (limpia todo)
 */
data class WorkoutCompletionState(
    private val completedSets: MutableMap<Long, Boolean> = mutableMapOf(),
    private val completedExercises: MutableMap<Long, Boolean> = mutableMapOf(),
    private val completedDays: MutableMap<String, Boolean> = mutableMapOf(),
    private val exercisesByDay: MutableMap<String, MutableList<Long>> = mutableMapOf(),
    private val setsByExercise: MutableMap<Long, MutableList<Long>> = mutableMapOf(),
    private val exerciseDayMap: MutableMap<Long, MutableSet<String>> = mutableMapOf()
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

    // ── Exercise level ────────────────────────────────────────────────────────

    fun isExerciseCompleted(routineExerciseId: Long): Boolean {
        val sets = setsByExercise[routineExerciseId] ?: return false
        if (sets.isEmpty()) return false
        return sets.any { completedSets[it] == true }
    }

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
        exerciseDayMap[routineExerciseId]?.forEach { day -> recalculateDayCompletion(day) }
    }

    // ── Day level ─────────────────────────────────────────────────────────────

    fun isDayCompleted(dayOfWeek: String): Boolean {
        val exercises = exercisesByDay[dayOfWeek] ?: return false
        if (exercises.isEmpty()) return false
        return exercises.any { isExerciseCompleted(it) }
    }

    fun setDayCompleted(dayOfWeek: String, completed: Boolean) {
        val exercises = exercisesByDay[dayOfWeek] ?: return
        exercises.forEach { setExerciseCompleted(it, completed) }
        completedDays[dayOfWeek] = completed
    }

    private fun recalculateDayCompletion(dayOfWeek: String) {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList()
        completedDays[dayOfWeek] = exercises.any { isExerciseCompleted(it) }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    fun registerSet(setId: Long, routineExerciseId: Long) {
        setsByExercise.getOrPut(routineExerciseId) { mutableListOf() }.apply {
            if (!contains(setId)) add(setId)
        }
        if (!completedSets.containsKey(setId)) completedSets[setId] = false
    }

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

    fun getAllCompletedSets(): Set<Long> = completedSets.filterValues { it }.keys

    fun hasAnyCompletedSets(): Boolean = completedSets.values.any { it }

    // ── Reset ─────────────────────────────────────────────────────────────────

    /**
     * Pone todos los checks a false sin borrar la estructura registrada.
     * Útil si quieres reutilizar el objeto con la misma rutina.
     */
    fun resetValues() {
        completedSets.replaceAll { _, _ -> false }
        completedExercises.replaceAll { _, _ -> false }
        completedDays.replaceAll { _, _ -> false }
    }

    /**
     * Limpia absolutamente todo. Usar al finalizar/guardar el entrenamiento.
     * Después de esto hay que volver a llamar a registerExercise/registerSet.
     */
    fun reset() {
        completedSets.clear()
        completedExercises.clear()
        completedDays.clear()
        exercisesByDay.clear()
        setsByExercise.clear()
        exerciseDayMap.clear()
    }
}
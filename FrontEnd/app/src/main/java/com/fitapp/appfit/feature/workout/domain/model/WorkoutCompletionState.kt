package com.fitapp.appfit.feature.workout.domain.model

/**
 * Tracks completion state for sets, exercises, and days during a workout session.
 *
 * All internal collections are properly typed as MutableList to avoid ClassCastException
 * from the previous pattern of storing MutableList behind a List type.
 */
data class WorkoutCompletionState(
    private val completedSets: MutableMap<Long, Boolean> = mutableMapOf(),
    private val completedExercises: MutableMap<Long, Boolean> = mutableMapOf(),
    private val completedDays: MutableMap<String, Boolean> = mutableMapOf(),
    private val exercisesByDay: MutableMap<String, MutableList<Long>> = mutableMapOf(),
    // FIX: was MutableMap<Long, List<Long>> — cast to MutableList was fragile
    private val setsByExercise: MutableMap<Long, MutableList<Long>> = mutableMapOf(),
    // Track which day each exercise belongs to for recalculation
    private val exerciseDayMap: MutableMap<Long, String> = mutableMapOf()
) {

    // ── Set level ─────────────────────────────────────────────────────────────

    fun isSetCompleted(setId: Long): Boolean = completedSets[setId] ?: false

    fun toggleSet(setId: Long, exerciseId: Long): Boolean {
        val newState = !isSetCompleted(setId)
        completedSets[setId] = newState
        recalculateExerciseCompletion(exerciseId)
        recalculateDayCompletionForExercise(exerciseId)
        return newState
    }

    fun markSetCompleted(setId: Long, exerciseId: Long, completed: Boolean) {
        completedSets[setId] = completed
        // Only recalculate if exerciseId is known
        if (exerciseId != 0L) {
            recalculateExerciseCompletion(exerciseId)
            recalculateDayCompletionForExercise(exerciseId)
        }
    }

    // ── Exercise level ────────────────────────────────────────────────────────

    fun isExerciseCompleted(exerciseId: Long): Boolean {
        val sets = setsByExercise[exerciseId] ?: return false
        if (sets.isEmpty()) return false
        return sets.any { completedSets[it] == true }
    }

    fun toggleExercise(exerciseId: Long, dayOfWeek: String): Boolean {
        val sets = setsByExercise[exerciseId] ?: emptyList<Long>()
        if (sets.isEmpty()) return false

        val shouldComplete = sets.none { completedSets[it] == true }
        sets.forEach { setId -> completedSets[setId] = shouldComplete }

        completedExercises[exerciseId] = shouldComplete
        recalculateDayCompletion(dayOfWeek)
        return shouldComplete
    }

    private fun recalculateExerciseCompletion(exerciseId: Long) {
        val sets = setsByExercise[exerciseId] ?: emptyList<Long>()
        completedExercises[exerciseId] = sets.any { completedSets[it] == true }
    }

    private fun recalculateDayCompletionForExercise(exerciseId: Long) {
        val day = exerciseDayMap[exerciseId] ?: return
        recalculateDayCompletion(day)
    }

    // ── Day level ─────────────────────────────────────────────────────────────

    fun isDayCompleted(dayOfWeek: String): Boolean {
        val exercises = exercisesByDay[dayOfWeek] ?: return false
        if (exercises.isEmpty()) return false
        return exercises.any { isExerciseCompleted(it) }
    }

    fun toggleDay(dayOfWeek: String): Boolean {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList<Long>()
        if (exercises.isEmpty()) return false

        val shouldComplete = exercises.none { isExerciseCompleted(it) }
        exercises.forEach { exerciseId ->
            setsByExercise[exerciseId]?.forEach { setId ->
                completedSets[setId] = shouldComplete
            }
            completedExercises[exerciseId] = shouldComplete
        }

        completedDays[dayOfWeek] = shouldComplete
        return shouldComplete
    }

    private fun recalculateDayCompletion(dayOfWeek: String) {
        val exercises = exercisesByDay[dayOfWeek] ?: emptyList<Long>()
        completedDays[dayOfWeek] = exercises.any { isExerciseCompleted(it) }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    fun registerSet(setId: Long, exerciseId: Long) {
        // FIX: use getOrPut to avoid the old MutableList cast pattern
        setsByExercise.getOrPut(exerciseId) { mutableListOf() }.apply {
            if (!contains(setId)) add(setId)
        }
        if (!completedSets.containsKey(setId)) {
            completedSets[setId] = false
        }
    }

    fun registerExercise(exerciseId: Long, dayOfWeek: String) {
        exercisesByDay.getOrPut(dayOfWeek) { mutableListOf() }.apply {
            if (!contains(exerciseId)) add(exerciseId)
        }
        exerciseDayMap[exerciseId] = dayOfWeek
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    fun getCompletedSetsCount(exerciseId: Long): Int =
        setsByExercise[exerciseId]?.count { completedSets[it] == true } ?: 0

    fun getTotalSetsCount(exerciseId: Long): Int =
        setsByExercise[exerciseId]?.size ?: 0

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
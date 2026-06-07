package com.fitapp.appfit.feature.metrics.data.calculator

import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSetResultEntity

object VolumeCalculator {

    fun calculateSetVolume(
        results: List<WorkoutSetResultEntity>,
        setTemplateId: Long
    ): Double {
        val setResults = results.filter { it.setTemplateId == setTemplateId }
        val weight = setResults.mapNotNull { it.numericValue }.firstOrNull() ?: return 0.0
        val reps = setResults.mapNotNull { it.integerValue ?: it.repetitions }.firstOrNull() ?: return 0.0
        return weight * reps
    }

    fun calculateSessionVolume(results: List<WorkoutSetResultEntity>): Double {
        return results
            .groupBy { it.setTemplateId }
            .values
            .sumOf { setResults -> calculateSetVolume(setResults, setResults.first().setTemplateId) }
    }

    fun countDistinctSets(results: List<WorkoutSetResultEntity>): Int =
        results.map { it.setTemplateId }.distinct().size

    fun countDistinctExercises(results: List<WorkoutSetResultEntity>): Int =
        results.map { it.exerciseId }.distinct().count { it > 0L }
}

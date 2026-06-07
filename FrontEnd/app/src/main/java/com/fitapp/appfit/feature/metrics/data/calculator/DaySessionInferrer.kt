package com.fitapp.appfit.feature.metrics.data.calculator

import com.fitapp.appfit.core.database.AppDatabase
import com.fitapp.appfit.feature.metrics.domain.model.DaySessionLabel
import com.fitapp.appfit.feature.metrics.domain.util.DaySessionLabelFormatter
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSessionEntity
import com.fitapp.appfit.feature.workout.data.database.entity.WorkoutSetResultEntity

class DaySessionInferrer(private val db: AppDatabase) {

    suspend fun inferForSession(
        session: WorkoutSessionEntity,
        results: List<WorkoutSetResultEntity>
    ): DaySessionLabel {
        if (!session.dayOfWeek.isNullOrBlank() || session.sessionNumber != null) {
            return DaySessionLabelFormatter.from(session.dayOfWeek, session.sessionNumber)
        }
        return inferFromSetTemplates(session.routineId, results)
    }

    suspend fun inferFromSetTemplates(
        routineId: Long,
        results: List<WorkoutSetResultEntity>
    ): DaySessionLabel {
        if (results.isEmpty()) {
            return DaySessionLabelFormatter.from(null, null)
        }

        val setTemplateDao = db.setTemplateDao()
        val routineExerciseDao = db.routineExerciseDao()

        val dayCounts = mutableMapOf<String, Int>()
        val sessionCounts = mutableMapOf<Int, Int>()

        for (setTemplateId in results.map { it.setTemplateId }.distinct()) {
            val template = setTemplateDao.getSetTemplateById(setTemplateId) ?: continue
            val routineExercise = routineExerciseDao.getRoutineExerciseById(template.routineExerciseId)
                ?: continue
            routineExercise.dayOfWeek?.let { dayCounts[it] = (dayCounts[it] ?: 0) + 1 }
            routineExercise.sessionNumber?.let { sessionCounts[it] = (sessionCounts[it] ?: 0) + 1 }
        }

        val dominantDay = dayCounts.maxByOrNull { it.value }?.key
        val dominantSession = sessionCounts.maxByOrNull { it.value }?.key

        return DaySessionLabelFormatter.from(dominantDay, dominantSession)
    }
}

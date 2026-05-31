package com.fitapp.appfit.feature.workout.domain.model

import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse

enum class WorkoutGroupType { DAY, SESSION }

data class WorkoutDay(
    val groupKey: String,
    val displayTitle: String,
    val groupType: WorkoutGroupType,
    val sessionNumber: Int? = null,
    val dayOfWeek: String? = null,
    val exercises: List<RoutineExerciseResponse>
)

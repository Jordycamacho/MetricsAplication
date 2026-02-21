package com.fitapp.appfit.response.workout

import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse

data class WorkoutDay(
    val dayOfWeek: String,
    val exercises: List<RoutineExerciseResponse>
)
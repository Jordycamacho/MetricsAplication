package com.fitapp.appfit.feature.workout.model

import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse

data class WorkoutDay(
    val dayOfWeek: String,
    val exercises: List<RoutineExerciseResponse>
)
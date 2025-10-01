package com.fitapp.appfit.response.exercise

data class AddExercisesToRoutineRequest(
    val routineId: Long,
    val exercises: List<ExerciseRequest>
)
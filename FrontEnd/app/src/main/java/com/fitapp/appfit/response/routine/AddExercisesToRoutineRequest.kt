package com.fitapp.appfit.response.routine

data class AddExercisesToRoutineRequest(
    val routineId: Long,
    val exercises: List<ExerciseRequest>
)
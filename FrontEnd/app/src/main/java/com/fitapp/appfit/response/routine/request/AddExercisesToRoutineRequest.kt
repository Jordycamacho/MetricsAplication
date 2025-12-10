package com.fitapp.appfit.response.routine.request

import com.fitapp.appfit.response.routine.request.ExerciseRequest

data class AddExercisesToRoutineRequest(
    val routineId: Long,
    val exercises: List<ExerciseRequest>
)
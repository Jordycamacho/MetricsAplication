package com.fitapp.appfit.response.routine

data class RoutineExerciseResponse(
    val id: Long,
    val exerciseId: Long,
    val sets: Int,
    val targetReps: String,
    val targetWeight: Double?,
    val restIntervalSeconds: Int
)
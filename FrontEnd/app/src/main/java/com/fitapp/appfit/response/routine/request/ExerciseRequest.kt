package com.fitapp.appfit.response.routine.request

data class ExerciseRequest(
    val exerciseId: Long,
    val sets: Int,
    val targetReps: String,
    val targetWeight: Double?,
    val restIntervalSeconds: Int
)
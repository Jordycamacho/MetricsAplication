package com.fitapp.appfit.response.exercise

data class ExerciseRequest(
    val exerciseId: Long,
    val sets: Int,
    val targetReps: String,
    val targetWeight: Double?,
    val restIntervalSeconds: Int?,
    val position: Int,
    val notes: String?
)
package com.fitapp.appfit.response.routine

data class CreateRoutineRequest(
    val name: String,
    val description: String? = null,
    val sportId: Long? = null,
    val trainingDays: List<String> = emptyList(),
    val goal: String,
    val difficultyLevel: Int,
    val weeksDuration: Int,
    val sessionsPerWeek: Int,
    val equipmentNeeded: String
)
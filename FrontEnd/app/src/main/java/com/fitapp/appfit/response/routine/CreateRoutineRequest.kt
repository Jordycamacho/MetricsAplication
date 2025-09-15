package com.fitapp.appfit.response.routine

import com.google.gson.annotations.SerializedName

data class CreateRoutineRequest(
    val name: String,
    val description: String? = null,
    @SerializedName("sportId")
    val sportId: Long? = null,
    val trainingDays: List<String> = emptyList(),
    val goal: String,
    val difficultyLevel: Int,
    val weeksDuration: Int,
    val sessionsPerWeek: Int,
    val equipmentNeeded: String
)
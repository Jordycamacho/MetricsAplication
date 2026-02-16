package com.fitapp.appfit.response.routine.request

import com.google.gson.annotations.SerializedName

data class CreateRoutineRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("sportId") val sportId: Long? = null,
    @SerializedName("trainingDays") val trainingDays: List<String> = emptyList(),
    @SerializedName("goal") val goal: String,
    @SerializedName("sessionsPerWeek") val sessionsPerWeek: Int
)
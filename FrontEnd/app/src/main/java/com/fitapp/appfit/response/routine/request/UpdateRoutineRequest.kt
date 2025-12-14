package com.fitapp.appfit.response.routine.request

import com.google.gson.annotations.SerializedName

data class UpdateRoutineRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("sportId")
    val sportId: Long? = null,

    @SerializedName("trainingDays")
    val trainingDays: List<String>? = null,

    @SerializedName("goal")
    val goal: String? = null,

    @SerializedName("sessionsPerWeek")
    val sessionsPerWeek: Int? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null
)
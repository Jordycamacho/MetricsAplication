package com.fitapp.appfit.response.routine.response

import com.google.gson.annotations.SerializedName

data class RoutineResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sportId") val sportId: Long?,
    @SerializedName("sportName") val sportName: String?,
    @SerializedName("trainingDays") val trainingDays: Set<String>?,
    @SerializedName("goal") val goal: String?,
    @SerializedName("sessionsPerWeek") val sessionsPerWeek: Int?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("lastUsedAt") val lastUsedAt: String?,
    @SerializedName("exercises") val exercises: List<RoutineExerciseResponse>?
)
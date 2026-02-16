package com.fitapp.appfit.response.routine.response

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class RoutineSummaryResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sportId") val sportId: Long?,
    @SerializedName("sportName") val sportName: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("lastUsedAt") val lastUsedAt: String? = null,
    @SerializedName("trainingDays") val trainingDays: Set<String>,
    @SerializedName("goal") val goal: String,
    @SerializedName("sessionsPerWeek") val sessionsPerWeek: Int,
    @SerializedName("exerciseCount") val exerciseCount: Int
)
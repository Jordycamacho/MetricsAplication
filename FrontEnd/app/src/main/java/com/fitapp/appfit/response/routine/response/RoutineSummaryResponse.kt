package com.fitapp.appfit.response.routine.response

import java.time.LocalDateTime

data class RoutineSummaryResponse (
    val id: Long,
    val name: String,
    val description: String?,
    val sportId: Long?,
    val sportName: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastUsedAt: LocalDateTime? = null,
    val trainingDays: Set<String>,
    val goal: String,
    val sessionsPerWeek: Int,
    val exerciseCount: Int
)
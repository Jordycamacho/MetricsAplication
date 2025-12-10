package com.fitapp.appfit.response.routine.response

import java.time.LocalDateTime

data class RoutineResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val sportId: Long?,
    val sportName: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val exercises: List<RoutineExerciseResponse>,
    val sessionsPerWeek: Int
)
package com.fitapp.appfit.response.routine

import java.time.LocalDateTime

data class RoutineResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val sportId: Long?,
    val sportName: String?,
    val isActive: Boolean,
    val estimatedDuration: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val exercises: List<RoutineExerciseResponse>
)
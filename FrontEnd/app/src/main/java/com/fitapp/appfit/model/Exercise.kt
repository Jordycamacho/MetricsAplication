package com.fitapp.appfit.model

import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.exercise.response.ExerciseType
import com.fitapp.appfit.utils.DateUtils
import java.time.LocalDateTime

data class Exercise(
    val id: Long,
    val name: String,
    val description: String?,
    val exerciseType: ExerciseType?,
    val sports: Map<Long, String>,
    val createdById: Long?,
    val categoryIds: Set<Long>,
    val categoryNames: Set<String>,
    val supportedParameterIds: Set<Long>,
    val supportedParameterNames: Set<String>,
    val isActive: Boolean?,
    val isPublic: Boolean?,
    val usageCount: Int?,
    val rating: Double?,
    val ratingCount: Int?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val lastUsedAt: LocalDateTime?
) {
    fun sportsDisplayName(): String = sports.values.joinToString(", ").ifEmpty { "—" }

    companion object {
        fun fromResponse(response: ExerciseResponse): Exercise {
            return Exercise(
                id = response.id,
                name = response.name,
                description = response.description,
                exerciseType = response.exerciseType,
                sports = response.sports,
                createdById = response.createdById,
                categoryIds = response.categoryIds,
                categoryNames = response.categoryNames,
                supportedParameterIds = response.supportedParameterIds,
                supportedParameterNames = response.supportedParameterNames,
                isActive = response.isActive,
                isPublic = response.isPublic,
                usageCount = response.usageCount,
                rating = response.rating,
                ratingCount = response.ratingCount,
                createdAt = DateUtils.parseDateTime(response.createdAt),
                updatedAt = DateUtils.parseDateTime(response.updatedAt),
                lastUsedAt = DateUtils.parseDateTime(response.lastUsedAt)
            )
        }
    }
}
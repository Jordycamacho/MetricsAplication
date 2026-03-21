package com.fitapp.appfit.feature.exercise.model.exercise.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExerciseResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("exerciseType")
    val exerciseType: ExerciseType?,

    @SerializedName("sports")
    val sports: Map<String, String> = emptyMap(),

    @SerializedName("createdById")
    val createdById: Long?,

    @SerializedName("categoryIds")
    val categoryIds: Set<Long> = emptySet(),

    @SerializedName("categoryNames")
    val categoryNames: Set<String> = emptySet(),

    @SerializedName("supportedParameterIds")
    val supportedParameterIds: Set<Long> = emptySet(),

    @SerializedName("supportedParameterNames")
    val supportedParameterNames: Set<String> = emptySet(),

    @SerializedName("isActive")
    val isActive: Boolean?,

    @SerializedName("isPublic")
    val isPublic: Boolean?,

    @SerializedName("usageCount")
    val usageCount: Int?,

    @SerializedName("rating")
    val rating: Double?,

    @SerializedName("ratingCount")
    val ratingCount: Int?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("lastUsedAt")
    val lastUsedAt: String?
) : Serializable {

    fun sportIds(): Set<Long> =
        sports.keys.mapNotNull { it.toLongOrNull() }.toSet()

    fun sportsDisplayName(): String =
        sports.values.joinToString(", ").ifEmpty { "—" }
}
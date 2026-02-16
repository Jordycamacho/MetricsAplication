package com.fitapp.appfit.response.category.response

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ExerciseCategoryResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("isPredefined") val isPredefined: Boolean,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("isPublic") val isPublic: Boolean,
    @SerializedName("ownerId") val ownerId: Long?,
    @SerializedName("ownerName") val ownerName: String?,
    @SerializedName("sportId") val sportId: Long?,
    @SerializedName("sportName") val sportName: String?,
    @SerializedName("parentCategoryId") val parentCategoryId: Long?,
    @SerializedName("usageCount") val usageCount: Int,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)
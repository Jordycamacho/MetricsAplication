package com.fitapp.appfit.feature.marketplace.model.response

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class PackageResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("packageType")
    val packageType: String, // SPORT_PACK, PARAMETER_PACK, ROUTINE_PACK, EXERCISE_PACK, MIXED

    @SerializedName("status")
    val status: String, // DRAFT, PUBLISHED, DEPRECATED, SUSPENDED

    @SerializedName("isFree")
    val isFree: Boolean,

    @SerializedName("price")
    val price: Double?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("version")
    val version: String,

    @SerializedName("changelog")
    val changelog: String?,

    @SerializedName("requiresSubscription")
    val requiresSubscription: String, // FREE, STANDARD, PREMIUM

    @SerializedName("downloadCount")
    val downloadCount: Int,

    @SerializedName("rating")
    val rating: Double?,

    @SerializedName("ratingCount")
    val ratingCount: Int,

    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,

    @SerializedName("tags")
    val tags: String?,

    @SerializedName("createdBy")
    val createdBy: CreatorInfo?,

    @SerializedName("items")
    val items: List<PackageItemResponse>?,

    @SerializedName("canEdit")
    val canEdit: Boolean,

    @SerializedName("canAccess")
    val canAccess: Boolean,

    @SerializedName("isPurchased")
    val isPurchased: Boolean,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
) {
    data class CreatorInfo(
        @SerializedName("id")
        val id: Long,

        @SerializedName("username")
        val username: String?,

        @SerializedName("reputationScore")
        val reputationScore: Int?
    )
}
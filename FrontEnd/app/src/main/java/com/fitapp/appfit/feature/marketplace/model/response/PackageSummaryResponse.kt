package com.fitapp.appfit.feature.marketplace.model.response

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class PackageSummaryResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("packageType")
    val packageType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("isFree")
    val isFree: Boolean,

    @SerializedName("price")
    val price: Double?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("requiresSubscription")
    val requiresSubscription: String,

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

    @SerializedName("creatorId")
    val creatorId: Long?,

    @SerializedName("itemCount")
    val itemCount: Int,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)
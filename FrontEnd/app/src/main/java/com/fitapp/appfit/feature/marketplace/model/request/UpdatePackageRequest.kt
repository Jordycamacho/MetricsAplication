package com.fitapp.appfit.feature.marketplace.model.request

import com.google.gson.annotations.SerializedName

data class UpdatePackageRequest(
    @SerializedName("name")
    val name: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("isFree")
    val isFree: Boolean?,

    @SerializedName("price")
    val price: Double?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("requiresSubscription")
    val requiresSubscription: String?,

    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,

    @SerializedName("tags")
    val tags: String?,

    @SerializedName("changelog")
    val changelog: String?
)
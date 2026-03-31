package com.fitapp.appfit.feature.marketplace.model.request

import com.google.gson.annotations.SerializedName

data class PackageFilterRequest(
    @SerializedName("search")
    val search: String? = null,

    @SerializedName("packageType")
    val packageType: String? = null,

    @SerializedName("isFree")
    val isFree: Boolean? = null,

    @SerializedName("requiresSubscription")
    val requiresSubscription: String? = null,

    @SerializedName("minRating")
    val minRating: Double? = null,

    @SerializedName("createdByUserId")
    val createdByUserId: Long? = null,

    @SerializedName("sortBy")
    val sortBy: String = "createdAt",

    @SerializedName("sortDirection")
    val sortDirection: String = "DESC"
)
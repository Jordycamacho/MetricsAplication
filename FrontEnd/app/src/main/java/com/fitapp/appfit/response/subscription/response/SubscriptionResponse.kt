package com.fitapp.appfit.response.subscription.response

import com.google.gson.annotations.SerializedName


data class SubscriptionResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("type") val type: String,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("maxRoutines") val maxRoutines: Int
)
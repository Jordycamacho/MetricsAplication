package com.fitapp.appfit.response.subscription.response

import com.google.gson.annotations.SerializedName

data class SubscriptionResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("maxRoutines") val maxRoutines: Int?,
    @SerializedName("canExportRoutines") val canExportRoutines: Boolean = false,
    @SerializedName("canAccessMarketplace") val canAccessMarketplace: Boolean = false,
    @SerializedName("canSellOnMarketplace") val canSellOnMarketplace: Boolean = false,
    @SerializedName("advancedAnalytics") val advancedAnalytics: Boolean = false,
    @SerializedName("autoRenew") val autoRenew: Boolean = false
)
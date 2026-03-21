package com.fitapp.appfit.feature.profile.model.response

import com.fitapp.appfit.feature.subscription.model.response.SubscriptionResponse
import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("role") val role: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("emailVerified") val emailVerified: Boolean = false,
    @SerializedName("subscription") val subscription: SubscriptionResponse?
)
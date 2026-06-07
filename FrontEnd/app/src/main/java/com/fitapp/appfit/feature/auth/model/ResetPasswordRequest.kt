package com.fitapp.appfit.feature.auth.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)

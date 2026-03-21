package com.fitapp.appfit.feature.auth.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)
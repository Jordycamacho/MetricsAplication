package com.fitapp.appfit.feature.auth.model

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String,
    @SerializedName("requiresEmailVerification") val requiresEmailVerification: Boolean = true
)

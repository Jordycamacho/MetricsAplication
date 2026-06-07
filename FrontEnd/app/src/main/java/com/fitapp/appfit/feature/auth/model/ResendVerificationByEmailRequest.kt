package com.fitapp.appfit.feature.auth.model

import com.google.gson.annotations.SerializedName

data class ResendVerificationByEmailRequest(
    @SerializedName("email") val email: String
)

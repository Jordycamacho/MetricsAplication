package com.fitapp.appfit.response.user.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String
)
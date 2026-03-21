package com.fitapp.appfit.feature.profile.model.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String
)
package com.fitapp.appfit.core.network

import com.google.gson.annotations.SerializedName

data class ApiError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
)

package com.fitapp.appfit.response.sport.request

import com.google.gson.annotations.SerializedName

data class SportRequest(
    @SerializedName("name") val name: String,
    @SerializedName("parameterTemplate") val parameterTemplate: Map<String, String>? = null,
    @SerializedName("sourceType") val sourceType: String? = "USER_CREATED"
)
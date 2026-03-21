package com.fitapp.appfit.feature.parameter.model.request

import com.google.gson.annotations.SerializedName

data class CustomParameterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("parameterType") val parameterType: String,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("isGlobal") val isGlobal: Boolean = false
)
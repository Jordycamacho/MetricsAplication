package com.fitapp.appfit.response.parameter.request

import com.google.gson.annotations.SerializedName

data class CustomParameterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("parameterType") val parameterType: String,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("validationRules") val validationRules: Map<String, String>? = null,
    @SerializedName("isGlobal") val isGlobal: Boolean = false,
    @SerializedName("sportId") val sportId: Long? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("icon") val icon: String? = null
)
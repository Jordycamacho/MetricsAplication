package com.fitapp.appfit.response.sport.response

import com.google.gson.annotations.SerializedName

data class SportResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("isPredefined") val isPredefined: Boolean,
    @SerializedName("parameterTemplate") val parameterTemplate: Map<String, String>?,
    @SerializedName("category") val category: String?,
    @SerializedName("sourceType") val sourceType: String? = null
)
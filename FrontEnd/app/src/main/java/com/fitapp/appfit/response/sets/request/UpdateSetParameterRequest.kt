package com.fitapp.appfit.response.sets.request

import com.google.gson.annotations.SerializedName

data class UpdateSetParameterRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("numericValue") val numericValue: Double? = null,
    @SerializedName("durationValue") val durationValue: Long? = null,
    @SerializedName("integerValue") val integerValue: Int? = null,
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null
)
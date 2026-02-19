package com.fitapp.appfit.response.sets.response

import com.google.gson.annotations.SerializedName

data class RoutineSetParameterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("setTemplateId") val setTemplateId: Long,
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("parameterName") val parameterName: String?,
    @SerializedName("parameterType") val parameterType: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName("repetitions") val repetitions: Int?,
    @SerializedName("numericValue") val numericValue: Double?,
    @SerializedName("durationValue") val durationValue: Long?,
    @SerializedName("integerValue") val integerValue: Int?
)
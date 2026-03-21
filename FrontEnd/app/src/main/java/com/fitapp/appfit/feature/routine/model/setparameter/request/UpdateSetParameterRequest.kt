package com.fitapp.appfit.feature.routine.model.setparameter.request

import com.google.gson.annotations.SerializedName

data class UpdateSetParameterRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("repetitions") val repetitions: Int? = null,
    @SerializedName("numericValue") val numericValue: Double? = null,
    @SerializedName("durationValue") val durationValue: Long? = null,
    @SerializedName("integerValue") val integerValue: Int? = null,
)
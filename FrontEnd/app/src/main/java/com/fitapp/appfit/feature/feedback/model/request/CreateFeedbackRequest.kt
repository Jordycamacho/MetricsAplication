package com.fitapp.appfit.feature.feedback.model.request

import com.google.gson.annotations.SerializedName

data class CreateFeedbackRequest(
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String,
    @SerializedName("stepsToReproduce") val stepsToReproduce: String?,
    @SerializedName("includeTechnicalContext") val includeTechnicalContext: Boolean,
    @SerializedName("technicalContext") val technicalContext: Map<String, String>?
)

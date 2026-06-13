package com.fitapp.appfit.feature.feedback.model.response

import com.google.gson.annotations.SerializedName

data class FeedbackSubmitResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("message") val message: String
)

package com.fitapp.appfit.feature.feedback.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.feedback.model.request.CreateFeedbackRequest
import com.fitapp.appfit.feature.feedback.model.response.FeedbackSubmitResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackService {

    @POST("api/feedback")
    suspend fun submitFeedback(@Body request: CreateFeedbackRequest): Response<FeedbackSubmitResponse>

    companion object {
        val instance: FeedbackService by lazy {
            ApiClient.instance.create(FeedbackService::class.java)
        }
    }
}

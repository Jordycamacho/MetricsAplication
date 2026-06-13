package com.fitapp.appfit.feature.feedback.data

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.feedback.model.request.CreateFeedbackRequest
import com.fitapp.appfit.feature.feedback.model.response.FeedbackSubmitResponse
import timber.log.Timber

class FeedbackRepository {

    private val feedbackService = FeedbackService.instance

    suspend fun submitFeedback(request: CreateFeedbackRequest): Resource<FeedbackSubmitResponse> {
        return try {
            val response = feedbackService.submitFeedback(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Error al enviar feedback (${response.code()})")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error enviando feedback")
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}

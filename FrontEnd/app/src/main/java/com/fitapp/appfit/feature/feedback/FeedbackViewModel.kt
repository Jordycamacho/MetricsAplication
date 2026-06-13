package com.fitapp.appfit.feature.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.feedback.data.FeedbackRepository
import com.fitapp.appfit.feature.feedback.model.request.CreateFeedbackRequest
import com.fitapp.appfit.feature.feedback.model.response.FeedbackSubmitResponse
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    private val repository = FeedbackRepository()

    private val _submitState = MutableLiveData<Resource<FeedbackSubmitResponse>>()
    val submitState: LiveData<Resource<FeedbackSubmitResponse>> = _submitState

    fun submitFeedback(request: CreateFeedbackRequest) {
        _submitState.value = Resource.Loading()
        viewModelScope.launch {
            _submitState.value = repository.submitFeedback(request)
        }
    }
}

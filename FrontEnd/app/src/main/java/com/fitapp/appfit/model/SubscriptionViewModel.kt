package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.SubscriptionRepository
import com.fitapp.appfit.response.subscription.response.SubscriptionResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class SubscriptionViewModel : ViewModel() {

    private val repository = SubscriptionRepository()

    private val _subscriptionState = MutableLiveData<Resource<SubscriptionResponse>>()
    val subscriptionState: LiveData<Resource<SubscriptionResponse>> = _subscriptionState

    private val _cancelState = MutableLiveData<Resource<SubscriptionResponse>>()
    val cancelState: LiveData<Resource<SubscriptionResponse>> = _cancelState

    fun loadSubscription() {
        _subscriptionState.value = Resource.Loading()
        viewModelScope.launch {
            _subscriptionState.value = repository.getMySubscription()
        }
    }

    fun cancelSubscription(reason: String? = null) {
        _cancelState.value = Resource.Loading()
        viewModelScope.launch {
            _cancelState.value = repository.cancelSubscription(reason)
        }
    }
}
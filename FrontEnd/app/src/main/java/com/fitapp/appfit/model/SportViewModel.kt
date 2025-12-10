package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.SportRepository
import com.fitapp.appfit.response.sport.request.SportRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class SportViewModel : ViewModel() {
    private val repository = SportRepository()

    private val _sportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val sportsState: LiveData<Resource<List<SportResponse>>> = _sportsState

    private val _createSportState = MutableLiveData<Resource<SportResponse>>()
    val createSportState: LiveData<Resource<SportResponse>> = _createSportState

    fun getSports() {
        _sportsState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsState.value = repository.getSports()
        }
    }

    fun getPredefinedSports() {
        _sportsState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsState.value = repository.getPredefinedSports()
        }
    }

    fun createCustomSport(name: String, category: String, parameters: Map<String, String>) {
        _createSportState.value = Resource.Loading()
        viewModelScope.launch {
            val sportRequest = SportRequest(name, parameters, null, category)
            _createSportState.value = repository.createCustomSport(sportRequest)
        }
    }
}
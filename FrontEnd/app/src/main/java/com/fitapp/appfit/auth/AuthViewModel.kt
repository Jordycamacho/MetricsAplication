package com.fitapp.appfit.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.AuthRepository
import com.fitapp.appfit.repository.UserRepository
import com.fitapp.appfit.response.AuthResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _loginState = MutableLiveData<Resource<AuthResponse>>()
    val loginState: LiveData<Resource<AuthResponse>> = _loginState

    private val _registerState = MutableLiveData<Resource<AuthResponse>>()
    val registerState: LiveData<Resource<AuthResponse>> = _registerState

    private val _logoutState = MutableLiveData<Resource<Unit>>()
    val logoutState: LiveData<Resource<Unit>> = _logoutState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            _loginState.value = authRepository.login(email, password)
        }
    }

    fun register(email: String, password: String, fullName: String) {
        _registerState.value = Resource.Loading()
        viewModelScope.launch {
            _registerState.value = authRepository.register(email, password, fullName)
        }
    }

    fun logout() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            _logoutState.value = userRepository.logout()
        }
    }
}
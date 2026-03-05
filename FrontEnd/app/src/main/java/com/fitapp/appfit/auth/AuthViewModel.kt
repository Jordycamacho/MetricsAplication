package com.fitapp.appfit.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.AuthRepository
import com.fitapp.appfit.repository.GoogleAuthRepository
import com.fitapp.appfit.repository.UserRepository
import com.fitapp.appfit.response.AuthResponse
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val googleAuthRepository = GoogleAuthRepository()
    private val _googleLoginState = MutableLiveData<Resource<Unit>>()
    val googleLoginState: LiveData<Resource<Unit>> = _googleLoginState

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

    fun handleGoogleCallback(uri: android.net.Uri) {
        val result = googleAuthRepository.extractTokenFromUri(uri)
        when (result) {
            is Resource.Success -> {
                SessionManager.accessToken = result.data
                SessionManager.tokenExpiration = System.currentTimeMillis() + (12 * 60 * 60 * 1000)
                _googleLoginState.value = Resource.Success(Unit)
            }
            is Resource.Error -> {
                _googleLoginState.value = Resource.Error(result.message ?: "Error con Google")
            }
            else -> {}
        }
    }

    fun openGoogleLogin(context: android.content.Context) {
        googleAuthRepository.openGoogleLogin(context)
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
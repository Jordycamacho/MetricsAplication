package com.fitapp.appfit.feature.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.profile.data.UserRepository
import com.fitapp.appfit.feature.profile.model.response.UserResponse
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _profileState = MutableLiveData<Resource<UserResponse>>()
    val profileState: LiveData<Resource<UserResponse>> = _profileState

    private val _updateProfileState = MutableLiveData<Resource<UserResponse>>()
    val updateProfileState: LiveData<Resource<UserResponse>> = _updateProfileState

    private val _changePasswordState = MutableLiveData<Resource<Unit>>()
    val changePasswordState: LiveData<Resource<Unit>> = _changePasswordState

    private val _logoutState = MutableLiveData<Resource<Unit>>()
    val logoutState: LiveData<Resource<Unit>> = _logoutState

    private val _deleteAccountState = MutableLiveData<Resource<Unit>>()
    val deleteAccountState: LiveData<Resource<Unit>> = _deleteAccountState

    private val _resendVerificationState = MutableLiveData<Resource<Unit>>()
    val resendVerificationState: LiveData<Resource<Unit>> = _resendVerificationState

    fun loadProfile() {
        _profileState.value = Resource.Loading()
        viewModelScope.launch {
            _profileState.value = userRepository.getMyProfile()
        }
    }

    fun updateProfile(fullName: String) {
        if (fullName.isBlank()) {
            _updateProfileState.value = Resource.Error("El nombre no puede estar vacío")
            return
        }
        _updateProfileState.value = Resource.Loading()
        viewModelScope.launch {
            _updateProfileState.value = userRepository.updateProfile(fullName)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (newPassword.length < 8) {
            _changePasswordState.value =
                Resource.Error("La nueva contraseña debe tener al menos 8 caracteres")
            return
        }
        _changePasswordState.value = Resource.Loading()
        viewModelScope.launch {
            _changePasswordState.value = userRepository.changePassword(currentPassword, newPassword)
        }
    }

    fun logout() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            _logoutState.value = userRepository.logout()
        }
    }

    fun deleteAccount() {
        _deleteAccountState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteAccountState.value = userRepository.deleteAccount()
        }
    }

    fun resendVerification() {
        _resendVerificationState.value = Resource.Loading()
        viewModelScope.launch {
            _resendVerificationState.value = userRepository.resendVerification()
        }
    }
}
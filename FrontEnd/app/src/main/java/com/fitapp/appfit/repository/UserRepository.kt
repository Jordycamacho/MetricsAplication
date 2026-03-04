package com.fitapp.appfit.repository

import com.fitapp.appfit.response.user.request.ChangePasswordRequest
import com.fitapp.appfit.response.user.request.UpdateProfileRequest
import com.fitapp.appfit.response.user.request.UserResponse
import com.fitapp.appfit.service.UserService
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.utils.SessionManager
import timber.log.Timber

class UserRepository {
    private val userService = UserService.instance

    suspend fun getMyProfile(): Resource<UserResponse> {
        return try {
            val response = userService.getMyProfile()
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Error ${response.code()}: ${response.errorBody()?.string()}")
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo perfil")
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun updateProfile(fullName: String): Resource<UserResponse> {
        return try {
            val response = userService.updateMyProfile(UpdateProfileRequest(fullName))
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error("Error al actualizar perfil")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val response = userService.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            )
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(
                when (response.code()) {
                    400 -> "La contraseña actual es incorrecta"
                    else -> "Error al cambiar contraseña"
                }
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    /**
     * Cierra sesión: llama al backend solo si hay token activo,
     * y siempre limpia la sesión local independientemente del resultado.
     */
    suspend fun logout(): Resource<Unit> {
        val token = SessionManager.accessToken
        if (!token.isNullOrBlank()) {
            try {
                userService.logout()
            } catch (e: Exception) {
                Timber.w("Logout backend falló, se limpia sesión local: ${e.message}")
            }
        }
        SessionManager.clearSession()
        return Resource.Success(Unit)
    }

    suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val response = userService.deleteMyAccount()
            if (response.isSuccessful) {
                SessionManager.clearSession()
                Resource.Success(Unit)
            } else {
                Resource.Error("Error al eliminar cuenta")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun resendVerification(): Resource<Unit> {
        return try {
            val response = userService.resendVerification()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Error al enviar correo")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}
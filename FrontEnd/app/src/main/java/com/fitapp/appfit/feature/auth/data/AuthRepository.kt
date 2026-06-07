package com.fitapp.appfit.feature.auth.data

import com.fitapp.appfit.core.network.ApiError
import com.fitapp.appfit.core.network.AuthService
import com.fitapp.appfit.core.session.SessionManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.auth.model.AuthResponse
import com.fitapp.appfit.feature.auth.model.ForgotPasswordRequest
import com.fitapp.appfit.feature.auth.model.LoginRequest
import com.fitapp.appfit.feature.auth.model.RegisterRequest
import com.fitapp.appfit.feature.auth.model.RegisterResponse
import com.fitapp.appfit.feature.auth.model.ResendVerificationByEmailRequest
import com.fitapp.appfit.feature.auth.model.ResetPasswordRequest
import com.google.gson.Gson
import retrofit2.Response
import timber.log.Timber

class AuthRepository {
    private val authService = AuthService.instance
    private val gson = Gson()

    suspend fun register(email: String, password: String, fullName: String): Resource<RegisterResponse> {
        return try {
            val response = authService.register(RegisterRequest(email, password, fullName))
            when {
                response.isSuccessful -> {
                    response.body()?.let { Resource.Success(it) }
                        ?: Resource.Error("Respuesta vacía del servidor")
                }
                response.code() == 409 -> Resource.Error("EMAIL_EXISTS")
                else -> Resource.Error(parseErrorMessage(response))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        Timber.i("Intento de login: $email")
        return try {
            val response = authService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Timber.i("Login exitoso: $email")
            } else {
                Timber.w("Login fallido - código ${response.code()}: $email")
            }
            handleAuthResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Error en login: $email")
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun resendVerificationByEmail(email: String): Resource<Unit> {
        return try {
            val response = authService.resendVerificationByEmail(ResendVerificationByEmailRequest(email))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(parseErrorMessage(response))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun forgotPassword(email: String): Resource<Unit> {
        return try {
            val response = authService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(parseErrorMessage(response))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): Resource<Unit> {
        return try {
            val response = authService.resetPassword(ResetPasswordRequest(token, newPassword))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(parseErrorMessage(response))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    private fun handleAuthResponse(response: Response<AuthResponse>): Resource<AuthResponse> {
        return when {
            response.isSuccessful -> {
                response.body()?.let { authResponse ->
                    SessionManager.saveSession(authResponse)
                    Resource.Success(authResponse)
                } ?: Resource.Error("Respuesta vacía del servidor")
            }
            response.code() == 401 -> Resource.Error("Credenciales inválidas")
            response.code() == 403 -> {
                val error = parseApiError(response)
                if (error?.code == "EMAIL_NOT_VERIFIED") {
                    Resource.Error("EMAIL_NOT_VERIFIED")
                } else {
                    Resource.Error(error?.message ?: "Acceso denegado")
                }
            }
            else -> Resource.Error(parseErrorMessage(response))
        }
    }

    private fun parseApiError(response: Response<*>): ApiError? {
        return try {
            val body = response.errorBody()?.string() ?: return null
            gson.fromJson(body, ApiError::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseErrorMessage(response: Response<*>): String {
        val apiError = parseApiError(response)
        return apiError?.message ?: "Error ${response.code()}"
    }
}

package com.fitapp.appfit.feature.auth.data

import com.fitapp.appfit.core.network.AuthService
import com.fitapp.appfit.core.session.SessionManager
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.auth.model.AuthResponse
import com.fitapp.appfit.feature.auth.model.LoginRequest
import com.fitapp.appfit.feature.auth.model.RegisterRequest
import retrofit2.Response
import timber.log.Timber

class AuthRepository {
    private val authService = AuthService.Companion.instance

    suspend fun register(email: String, password: String, fullName: String): Resource<AuthResponse> {
        return try {
            val response = authService.register(RegisterRequest(email, password, fullName))
            handleAuthResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        Timber.Forest.i("Intento de login: $email")
        return try {
            val response = authService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Timber.Forest.i("Login exitoso: $email")
            } else {
                Timber.Forest.w("Login fallido - código ${response.code()}: $email")
            }
            handleAuthResponse(response)
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error en login: $email")
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    private fun handleAuthResponse(response: Response<AuthResponse>): Resource<AuthResponse> {
        return when {
            response.isSuccessful -> {
                response.body()?.let { authResponse ->
                    // Guardar tokens en sesión
                    SessionManager.saveSession(authResponse)
                    Resource.Success(authResponse)
                } ?: Resource.Error("Respuesta vacía del servidor")
            }

            response.code() == 401 -> Resource.Error("Credenciales inválidas")

            else -> {
                try {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Resource.Error("Error ${response.code()}: $errorBody")
                } catch (e: Exception) {
                    Resource.Error("Error ${response.code()}")
                }
            }
        }
    }
}
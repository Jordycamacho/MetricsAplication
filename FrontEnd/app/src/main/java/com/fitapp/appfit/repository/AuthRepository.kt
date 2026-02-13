package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.network.AuthService
import com.fitapp.appfit.response.AuthResponse
import com.fitapp.appfit.response.LoginRequest
import com.fitapp.appfit.response.RegisterRequest
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.utils.SessionManager
import retrofit2.Response
import timber.log.Timber

class AuthRepository {
    private val authService = AuthService.instance

    suspend fun register(email: String, password: String, fullName: String): Resource<AuthResponse> {
        return try {
            val response = authService.register(RegisterRequest(email, password, fullName))
            handleAuthResponse(response)
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
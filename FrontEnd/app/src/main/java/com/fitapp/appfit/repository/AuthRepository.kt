package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.network.AuthService
import com.fitapp.appfit.response.AuthResponse
import com.fitapp.appfit.response.LoginRequest
import com.fitapp.appfit.response.RegisterRequest
import com.fitapp.appfit.utils.Resource
import com.fitapp.appfit.utils.SessionManager
import retrofit2.Response

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
        return try {
            val response = authService.login(LoginRequest(email, password))
            Log.d("AuthRepository", "Response: $response")
            Log.d("AuthRepository", "Response code: ${response.code()}")
            Log.d("AuthRepository", "Response body: ${response.body()}")
            handleAuthResponse(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error: ${e.message}", e)
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
package com.fitapp.appfit.network

import com.fitapp.appfit.response.AuthResponse
import com.fitapp.appfit.response.LoginRequest
import com.fitapp.appfit.response.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    companion object {
        val instance: AuthService by lazy {
            ApiClient.instance.create(AuthService::class.java)
        }
    }
}
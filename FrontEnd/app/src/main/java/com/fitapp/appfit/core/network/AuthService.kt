package com.fitapp.appfit.core.network

import com.fitapp.appfit.feature.auth.model.AuthResponse
import com.fitapp.appfit.feature.auth.model.LoginRequest
import com.fitapp.appfit.feature.auth.model.RefreshTokenRequest
import com.fitapp.appfit.feature.auth.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    companion object {
        val instance: AuthService by lazy {
            ApiClient.instance.create(AuthService::class.java)
        }
    }
}
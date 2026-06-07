package com.fitapp.appfit.core.network

import com.fitapp.appfit.feature.auth.model.AuthResponse
import com.fitapp.appfit.feature.auth.model.ForgotPasswordRequest
import com.fitapp.appfit.feature.auth.model.LoginRequest
import com.fitapp.appfit.feature.auth.model.RefreshTokenRequest
import com.fitapp.appfit.feature.auth.model.RegisterRequest
import com.fitapp.appfit.feature.auth.model.RegisterResponse
import com.fitapp.appfit.feature.auth.model.ResendVerificationByEmailRequest
import com.fitapp.appfit.feature.auth.model.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/auth/resend-verification-email")
    suspend fun resendVerificationByEmail(@Body request: ResendVerificationByEmailRequest): Response<Void>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Void>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Void>

    companion object {
        val instance: AuthService by lazy {
            ApiClient.instance.create(AuthService::class.java)
        }
    }
}

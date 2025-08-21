package com.fitapp.appfit.response

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val expiresAt: Long
)

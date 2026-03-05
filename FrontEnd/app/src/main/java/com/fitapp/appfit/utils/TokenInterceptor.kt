package com.fitapp.appfit.utils

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {

    private val publicRoutes = listOf(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/verify-email"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (publicRoutes.any { path.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        if (SessionManager.shouldRefresh()) {
            SessionManager.refreshTokenIfNeeded()
        }

        val token = SessionManager.accessToken
        val request = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
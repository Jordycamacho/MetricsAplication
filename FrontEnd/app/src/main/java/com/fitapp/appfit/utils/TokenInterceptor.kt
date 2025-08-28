package com.fitapp.appfit.utils

import com.fitapp.appfit.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No agregar token para endpoints de auth
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val request = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer ${SessionManager.accessToken}")
            .build()
        return chain.proceed(request)
    }
}
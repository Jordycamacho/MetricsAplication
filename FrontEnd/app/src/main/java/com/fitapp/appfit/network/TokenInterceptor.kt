package com.fitapp.appfit.network

import com.fitapp.appfit.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${SessionManager.accessToken}")
            .build()
        return chain.proceed(request)
    }
}
package com.fitapp.appfit.network

import com.fitapp.appfit.service.RoutineService
import com.fitapp.appfit.utils.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://192.168.1.14:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor())
        .authenticator(TokenAuthenticator())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val routineService: RoutineService by lazy {
        instance.create(RoutineService::class.java)
    }
}
package com.fitapp.appfit.service

import com.fitapp.appfit.network.ApiClient
import com.fitapp.appfit.response.subscription.response.SubscriptionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SubscriptionService {

    @GET("api/subscriptions/me")
    suspend fun getMySubscription(): Response<SubscriptionResponse>

    @POST("api/subscriptions/me/cancel")
    suspend fun cancelSubscription(
        @Query("reason") reason: String? = null
    ): Response<SubscriptionResponse>

    companion object {
        val instance: SubscriptionService by lazy {
            ApiClient.instance.create(SubscriptionService::class.java)
        }
    }
}
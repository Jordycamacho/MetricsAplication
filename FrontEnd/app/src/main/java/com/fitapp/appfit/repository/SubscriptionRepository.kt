package com.fitapp.appfit.repository

import com.fitapp.appfit.response.subscription.response.SubscriptionResponse
import com.fitapp.appfit.service.SubscriptionService
import com.fitapp.appfit.utils.Resource
import timber.log.Timber

class SubscriptionRepository {

    private val subscriptionService = SubscriptionService.instance

    suspend fun getMySubscription(): Resource<SubscriptionResponse> {
        return try {
            val response = subscriptionService.getMySubscription()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error ${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo suscripción")
            Resource.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun cancelSubscription(reason: String? = null): Resource<SubscriptionResponse> {
        return try {
            val response = subscriptionService.cancelSubscription(reason)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cancelar suscripción")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error de conexión")
        }
    }
}
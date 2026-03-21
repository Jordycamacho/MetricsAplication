package com.fitapp.appfit.feature.subscription.data

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.subscription.model.response.SubscriptionResponse
import com.fitapp.appfit.feature.subscription.data.SubscriptionService
import timber.log.Timber

class SubscriptionRepository {

    private val subscriptionService = SubscriptionService.Companion.instance

    suspend fun getMySubscription(): Resource<SubscriptionResponse> {
        return try {
            val response = subscriptionService.getMySubscription()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error ${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error obteniendo suscripción")
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
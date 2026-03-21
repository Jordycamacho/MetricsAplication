package com.fitapp.appfit.feature.profile.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.profile.model.request.ChangePasswordRequest
import com.fitapp.appfit.feature.profile.model.request.UpdateProfileRequest
import com.fitapp.appfit.feature.profile.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserService {

    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserResponse>

    @PUT("api/users/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @DELETE("api/users/me")
    suspend fun deleteMyAccount(): Response<Unit>

    @POST("api/users/me/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(): Response<Unit>

    companion object {
        val instance: UserService by lazy {
            ApiClient.instance.create(UserService::class.java)
        }
    }
}
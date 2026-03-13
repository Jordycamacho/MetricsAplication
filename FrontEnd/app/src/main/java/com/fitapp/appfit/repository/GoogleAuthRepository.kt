package com.fitapp.appfit.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fitapp.appfit.response.AuthResponse
import com.fitapp.appfit.utils.Resource

class GoogleAuthRepository {

    private val googleLoginUrl = "http://192.168.1.14:8080/oauth2/authorization/google"

    fun openGoogleLogin(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleLoginUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun extractTokenFromUri(uri: Uri?): Resource<AuthResponse> {
        if (uri == null) return Resource.Error("URI nula")

        val token = uri.getQueryParameter("token")
        val refreshToken = uri.getQueryParameter("refreshToken") ?: ""
        val expiresAt = uri.getQueryParameter("expiresAt") ?: ""

        return if (!token.isNullOrBlank()) {
            Resource.Success(AuthResponse(token, refreshToken, expiresAt))
        } else {
            Resource.Error("No se encontró el token en la URL")
        }
    }
}
package com.fitapp.appfit.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fitapp.appfit.response.AuthResponse

object SessionManager {
    private const val ACCESS_TOKEN_KEY = "fitapp_access_token"
    private const val REFRESH_TOKEN_KEY = "fitapp_refresh_token"
    private const val EXPIRATION_KEY = "fitapp_expiration"

    private lateinit var sharedPreferences: EncryptedSharedPreferences

    fun initialize(context: Context) {
        val cryptoManager = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPreferences = EncryptedSharedPreferences.create(
            "fitapp_secure_prefs",
            cryptoManager,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    var accessToken: String?
        get() = if (::sharedPreferences.isInitialized) sharedPreferences.getString(ACCESS_TOKEN_KEY, null) else null
        set(value) {
            if (::sharedPreferences.isInitialized && value != null) {
                sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, value).apply()
            }
        }

    var refreshToken: String?
        get() = if (::sharedPreferences.isInitialized) sharedPreferences.getString(REFRESH_TOKEN_KEY, null) else null
        set(value) {
            if (::sharedPreferences.isInitialized && value != null) {
                sharedPreferences.edit().putString(REFRESH_TOKEN_KEY, value).apply()
            }
        }

    var tokenExpiration: Long
        get() = if (::sharedPreferences.isInitialized) sharedPreferences.getLong(EXPIRATION_KEY, 0) else 0
        set(value) {
            if (::sharedPreferences.isInitialized) {
                sharedPreferences.edit().putLong(EXPIRATION_KEY, value).apply()
            }
        }

    fun saveSession(authResponse: AuthResponse) {
        accessToken = authResponse.token
        refreshToken = authResponse.refreshToken
        tokenExpiration = authResponse.expiresAt
    }

    fun isTokenValid(): Boolean {
        if (!::sharedPreferences.isInitialized) return false
        val expirationTime = tokenExpiration
        return !accessToken.isNullOrEmpty() && System.currentTimeMillis() < expirationTime
    }

    fun clearSession() {
        if (::sharedPreferences.isInitialized) {
            sharedPreferences.edit().apply {
                remove(ACCESS_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(EXPIRATION_KEY)
                apply()
            }
        }
    }
}
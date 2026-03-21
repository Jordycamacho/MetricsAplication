package com.fitapp.appfit.core.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fitapp.appfit.feature.auth.model.AuthResponse
import timber.log.Timber
import java.time.Instant
import java.util.Date

object SessionManager {
    private const val ACCESS_TOKEN_KEY = "fitapp_access_token"
    private const val REFRESH_TOKEN_KEY = "fitapp_refresh_token"
    private const val EXPIRATION_KEY = "fitapp_expiration"
    private lateinit var sharedPreferences: EncryptedSharedPreferences

    fun initialize(context: Context) {
        try {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPreferences = EncryptedSharedPreferences.create(
                "fitapp_secure_prefs",
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            Timber.Forest.w("EncryptedSharedPreferences corrupto, recreando...")
            context.deleteSharedPreferences("fitapp_secure_prefs")
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPreferences = EncryptedSharedPreferences.create(
                "fitapp_secure_prefs",
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        }
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

    var onSessionExpired: (() -> Unit)? = null

    fun saveSession(authResponse: AuthResponse) {
        accessToken = authResponse.token
        refreshToken = authResponse.refreshToken
        tokenExpiration = try {
            Instant.parse(authResponse.expiresAt).toEpochMilli()
        } catch (e: Exception) {
            Timber.Forest.e(e, "Error parseando expiresAt: ${authResponse.expiresAt}")
            0L
        }
        Timber.Forest.i("Sesión guardada - Token expira: ${Date(tokenExpiration)}")
    }

    fun isTokenValid(): Boolean {
        val expirationTime = tokenExpiration
        return !accessToken.isNullOrEmpty() && System.currentTimeMillis() < expirationTime
    }

    fun hasSession(): Boolean {
        return !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()
    }

    fun clearSession() {
        if (::sharedPreferences.isInitialized) {
            sharedPreferences.edit().apply {
                remove(ACCESS_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(EXPIRATION_KEY)
                apply()
            }
            Timber.Forest.w("Sesión eliminada")
            onSessionExpired?.invoke()
        }
    }
}
package com.fitapp.appfit.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fitapp.appfit.response.AuthResponse
import timber.log.Timber
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.runBlocking
import com.fitapp.appfit.network.AuthService
import com.fitapp.appfit.response.RefreshTokenRequest

object SessionManager {
    private const val ACCESS_TOKEN_KEY = "fitapp_access_token"
    private const val REFRESH_TOKEN_KEY = "fitapp_refresh_token"
    private const val EXPIRATION_KEY = "fitapp_expiration"

    private lateinit var sharedPreferences: EncryptedSharedPreferences

    fun initialize(context: Context) {
        try {
            val cryptoManager = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPreferences = EncryptedSharedPreferences.create(
                "fitapp_secure_prefs",
                cryptoManager,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            Timber.w("EncryptedSharedPreferences corrupto, recreando...")
            context.deleteSharedPreferences("fitapp_secure_prefs")
            val cryptoManager = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPreferences = EncryptedSharedPreferences.create(
                "fitapp_secure_prefs",
                cryptoManager,
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

    fun saveSession(authResponse: AuthResponse) {
        accessToken = authResponse.token
        refreshToken = authResponse.refreshToken
        tokenExpiration = try {
            Instant.parse(authResponse.expiresAt).toEpochMilli()
        } catch (e: Exception) {
            Timber.e(e, "Error parseando expiresAt: ${authResponse.expiresAt}")
            0L
        }
        Timber.i("Sesión guardada - Token expira en: ${Date(tokenExpiration)}")
    }

    fun shouldRefresh(): Boolean {
        val expiration = tokenExpiration
        return expiration > 0 && System.currentTimeMillis() > expiration - (5 * 60 * 1000)
    }

    fun isTokenValid(): Boolean {
        if (!::sharedPreferences.isInitialized) return false
        val expirationTime = tokenExpiration
        return !accessToken.isNullOrEmpty() && System.currentTimeMillis() < expirationTime
    }

    fun refreshTokenIfNeeded(): Boolean {
        if (!shouldRefresh()) return true

        val currentRefreshToken = refreshToken ?: return false

        return try {
            runBlocking {
                val response = AuthService.instance.refreshToken(RefreshTokenRequest(currentRefreshToken))
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        saveSession(authResponse)
                        Timber.i("Token refrescado preventivamente")
                        true
                    } ?: false
                } else {
                    Timber.e("Error al refrescar token: ${response.code()}")
                    clearSession()
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Excepción al refrescar token")
            clearSession()
            false
        }
    }

    fun clearSession() {
        if (::sharedPreferences.isInitialized) {
            sharedPreferences.edit().apply {
                remove(ACCESS_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(EXPIRATION_KEY)
                apply()
            }
            Timber.w("Sesión eliminada")
        }
    }
}
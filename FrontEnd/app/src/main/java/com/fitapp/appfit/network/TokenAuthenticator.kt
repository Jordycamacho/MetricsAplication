package com.fitapp.appfit.utils

import com.fitapp.appfit.network.AuthService
import com.fitapp.appfit.response.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("X-Retry-With-Refresh") != null) {
            Timber.w("[AUTHENTICATOR] Ya se intentó refresh, abortando - sesión expirada")
            SessionManager.clearSession()
            return null
        }

        Timber.i("[AUTHENTICATOR] 401 recibido en ${response.request.url}, intentando refresh...")

        val refreshToken = SessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Timber.w("[AUTHENTICATOR] No hay refresh token guardado, limpiando sesión")
            SessionManager.clearSession()
            return null
        }

        Timber.i("[AUTHENTICATOR] Refresh token encontrado, llamando a /api/auth/refresh")

        return runBlocking {
            try {
                val authService = AuthService.instance
                val refreshResponse = authService.refreshToken(RefreshTokenRequest(refreshToken))

                if (refreshResponse.isSuccessful) {
                    val newAuth = refreshResponse.body()
                    if (newAuth != null) {
                        Timber.i("[AUTHENTICATOR] Refresh exitoso - nuevo token guardado, reintentando request original")
                        SessionManager.saveSession(newAuth)
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newAuth.token}")
                            .header("X-Retry-With-Refresh", "true")
                            .build()
                    } else {
                        Timber.e("[AUTHENTICATOR] Refresh devolvió body null")
                        SessionManager.clearSession()
                        null
                    }
                } else {
                    Timber.e("[AUTHENTICATOR] Refresh FALLÓ - código ${refreshResponse.code()}: ${refreshResponse.errorBody()?.string()}")
                    SessionManager.clearSession()
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "[AUTHENTICATOR] Excepción durante refresh: ${e.message}")
                SessionManager.clearSession()
                null
            }
        }
    }
}
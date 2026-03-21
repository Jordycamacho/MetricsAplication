package com.fitapp.appfit.core.network

import com.fitapp.appfit.feature.auth.model.RefreshTokenRequest
import com.fitapp.appfit.core.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("X-Retry-With-Refresh") != null) {
            Timber.Forest.w("[AUTHENTICATOR] Ya se intentó refresh, abortando - sesión expirada")
            SessionManager.clearSession()
            return null
        }

        Timber.Forest.i("[AUTHENTICATOR] 401 recibido en ${response.request.url}, intentando refresh...")

        val refreshToken = SessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Timber.Forest.w("[AUTHENTICATOR] No hay refresh token guardado, limpiando sesión")
            SessionManager.clearSession()
            return null
        }

        Timber.Forest.i("[AUTHENTICATOR] Refresh token encontrado, llamando a /api/auth/refresh")

        return runBlocking {
            try {
                val authService = AuthService.instance
                val refreshResponse = authService.refreshToken(RefreshTokenRequest(refreshToken))

                if (refreshResponse.isSuccessful) {
                    val newAuth = refreshResponse.body()
                    if (newAuth != null) {
                        Timber.Forest.i("[AUTHENTICATOR] Refresh exitoso - nuevo token guardado, reintentando request original")
                        SessionManager.saveSession(newAuth)
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newAuth.token}")
                            .header("X-Retry-With-Refresh", "true")
                            .build()
                    } else {
                        Timber.Forest.e("[AUTHENTICATOR] Refresh devolvió body null")
                        SessionManager.clearSession()
                        null
                    }
                } else {
                    Timber.Forest.e(
                        "[AUTHENTICATOR] Refresh FALLÓ - código ${refreshResponse.code()}: ${
                            refreshResponse.errorBody()?.string()
                        }"
                    )
                    SessionManager.clearSession()
                    null
                }
            } catch (e: Exception) {
                Timber.Forest.e(e, "[AUTHENTICATOR] Excepción durante refresh: ${e.message}")
                SessionManager.clearSession()
                null
            }
        }
    }
}
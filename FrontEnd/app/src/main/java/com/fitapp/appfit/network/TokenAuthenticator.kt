package com.fitapp.appfit.network

import com.fitapp.appfit.response.RefreshTokenRequest
import com.fitapp.appfit.utils.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class TokenAuthenticator : Authenticator {

    companion object {
        private const val RETRY_HEADER = "X-Retry-After-Refresh"
    }

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.request.header(RETRY_HEADER) != null) {
            Timber.w("Token sigue inválido tras refresh, cerrando sesión")
            SessionManager.clearSession()
            return null
        }

        val refreshToken = SessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Timber.w("No hay refresh token disponible, cerrando sesión")
            SessionManager.clearSession()
            return null
        }

        return synchronized(this) {
            try {
                Timber.i("Token expirado, intentando refresh...")

                val newAuthResponse = runBlocking {
                    AuthService.instance.refreshToken(RefreshTokenRequest(refreshToken))
                }

                if (newAuthResponse.isSuccessful) {
                    val authResponse = newAuthResponse.body()
                    if (authResponse != null) {
                        SessionManager.saveSession(authResponse)
                        Timber.i("Token refrescado exitosamente, reintentando request")
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${authResponse.token}")
                            .header(RETRY_HEADER, "true")
                            .build()
                    } else {
                        Timber.e("Cuerpo de respuesta de refresh vacío")
                        SessionManager.clearSession()
                        null
                    }
                } else {
                    Timber.e("Refresh falló con código: ${newAuthResponse.code()}")
                    SessionManager.clearSession()
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción durante el refresh de token")
                SessionManager.clearSession()
                null
            }
        }
    }
}
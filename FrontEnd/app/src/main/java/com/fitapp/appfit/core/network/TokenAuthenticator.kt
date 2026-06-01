package com.fitapp.appfit.core.network

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
            Timber.w("[AUTHENTICATOR] Ya se intentó refresh, abortando - sesión expirada")
            SessionManager.clearSession()
            return null
        }

        Timber.i("[AUTHENTICATOR] 401 recibido en ${response.request.url}, intentando refresh...")

        if (response.request.url.encodedPath.contains("/api/auth/refresh")) {
            Timber.w("[AUTHENTICATOR] 401 en refresh, no reintentar")
            return null
        }

        return runBlocking {
            val newToken = TokenRefreshCoordinator.obtainAccessTokenAfter401(
                response.request.header("Authorization")
            )
            if (newToken != null) {
                Timber.i("[AUTHENTICATOR] Token renovado, reintentando request original")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("X-Retry-With-Refresh", "true")
                    .build()
            } else {
                Timber.w("[AUTHENTICATOR] No se pudo renovar la sesión")
                null
            }
        }
    }
}

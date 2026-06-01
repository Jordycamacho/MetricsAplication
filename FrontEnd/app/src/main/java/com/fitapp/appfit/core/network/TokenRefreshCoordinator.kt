package com.fitapp.appfit.core.network

import com.fitapp.appfit.BuildConfig
import com.fitapp.appfit.core.session.SessionManager
import com.fitapp.appfit.feature.auth.model.RefreshTokenRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Renueva tokens de forma serializada y con un OkHttpClient separado
 * (evita deadlock del Authenticator al reutilizar el mismo cliente).
 */
object TokenRefreshCoordinator {

    private val mutex = Mutex()

    private val refreshAuthService: AuthService by lazy {
        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    suspend fun refreshSession(): Boolean = mutex.withLock {
        if (performRefresh()) return@withLock true
        SessionManager.clearSession()
        false
    }

    /**
     * Tras un 401, devuelve un access token válido o null si la sesión no se puede recuperar.
     * Si otro hilo ya renovó el token, reutiliza el guardado sin llamar otra vez a /refresh.
     */
    suspend fun obtainAccessTokenAfter401(authorizationHeader: String?): String? = mutex.withLock {
        val currentToken = SessionManager.accessToken
        val failedToken = authorizationHeader
            ?.removePrefix("Bearer ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (!currentToken.isNullOrEmpty() && failedToken != null && currentToken != failedToken) {
            Timber.i("[REFRESH] Reutilizando token ya renovado por otra petición")
            return@withLock currentToken
        }

        if (performRefresh()) {
            return@withLock SessionManager.accessToken
        }

        SessionManager.clearSession()
        null
    }

    private suspend fun performRefresh(): Boolean {
        val refreshToken = SessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Timber.w("[REFRESH] No hay refresh token guardado")
            return false
        }

        return try {
            Timber.i("[REFRESH] Llamando a /api/auth/refresh")
            val response = refreshAuthService.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    SessionManager.saveSession(body)
                    Timber.i("[REFRESH] Refresh exitoso")
                    true
                } else {
                    Timber.e("[REFRESH] Respuesta sin body")
                    false
                }
            } else {
                Timber.e(
                    "[REFRESH] Refresh falló - código ${response.code()}: ${
                        response.errorBody()?.string()
                    }"
                )
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "[REFRESH] Excepción durante refresh: ${e.message}")
            false
        }
    }
}

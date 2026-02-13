package com.fitapp.appfit.network

import com.fitapp.appfit.response.RefreshTokenRequest
import com.fitapp.appfit.utils.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import timber.log.Timber

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = SessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) {
            Timber.w("No hay refresh token, redirigiendo a login")
            SessionManager.clearSession()
            return null
        }

        synchronized(this) {
            val currentToken = SessionManager.accessToken
            if (currentToken != null && !currentToken.isNullOrEmpty()) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            return try {
                Timber.i("Intentando refrescar token...")
                val newAuthResponse = runBlocking {
                    AuthService.instance.refreshToken(RefreshTokenRequest(refreshToken))
                }

                if (newAuthResponse.isSuccessful) {
                    val authResponse = newAuthResponse.body()
                    if (authResponse != null) {
                        SessionManager.saveSession(authResponse)
                        Timber.i("Token refrescado exitosamente")
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${authResponse.token}")
                            .build()
                    } else {
                        Timber.e("Respuesta de refresh vacía")
                        null
                    }
                } else {
                    Timber.e("Error al refrescar token: ${newAuthResponse.code()}")
                    SessionManager.clearSession()
                    null
                }
            } catch (e: HttpException) {
                Timber.e(e, "Excepción HTTP al refrescar")
                SessionManager.clearSession()
                null
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al refrescar")
                SessionManager.clearSession()
                null
            }
        }
    }
}
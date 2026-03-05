package com.fitapp.appfit.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * NetworkMonitor — observa cambios de conectividad.
 *
 * Cuando la conexión vuelve, dispara SyncWorker automáticamente.
 * Usar el Flow isOnline en la UI para mostrar banner "Sin conexión".
 */
class NetworkMonitor(private val context: Context) {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val online = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            val wasOffline = !_isOnline.value
            _isOnline.value = online

            if (online && wasOffline) {
                Timber.i("Conexión recuperada → sincronizando")
                SyncWorker.syncNow(context)
            }
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
            Timber.w("Conexión perdida")
        }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Estado inicial
        val current = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        _isOnline.value = current?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            Timber.w(e, "Error al desregistrar NetworkCallback")
        }
    }
}
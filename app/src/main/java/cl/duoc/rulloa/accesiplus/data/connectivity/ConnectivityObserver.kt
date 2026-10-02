package cl.duoc.rulloa.accesiplus.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Informa si el dispositivo tiene conexión a internet (mitigación del riesgo de conectividad). */
interface ConnectivityObserver {
    fun observarConexion(): Flow<Boolean>
}

class AndroidConnectivityObserver(context: Context) : ConnectivityObserver {

    // getSystemService<T>() es una extensión de core-ktx (evita el cast manual)
    private val cm = requireNotNull(context.getSystemService<ConnectivityManager>())

    override fun observarConexion(): Flow<Boolean> = callbackFlow {
        fun tieneInternet(): Boolean {
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            // onAvailable no garantiza internet: se espera a onCapabilitiesChanged (VALIDATED)
            override fun onAvailable(network: Network) {}

            // En onLost, activeNetwork todavía puede apuntar a la red perdida: se emite false directo
            override fun onLost(network: Network) { trySend(false) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                )
            }
        }
        trySend(tieneInternet())
        cm.registerDefaultNetworkCallback(callback)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}

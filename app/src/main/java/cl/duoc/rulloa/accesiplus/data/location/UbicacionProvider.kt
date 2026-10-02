package cl.duoc.rulloa.accesiplus.data.location

import android.annotation.SuppressLint
import android.content.Context
import cl.duoc.rulloa.accesiplus.data.repository.resultado
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class Coordenadas(val lat: Double, val lon: Double)

interface UbicacionProvider {
    /** Requiere que la UI ya haya obtenido el permiso de ubicación. */
    suspend fun ubicacionActual(): Result<Coordenadas>
}

class FusedUbicacionProvider(context: Context) : UbicacionProvider {

    private val cliente = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    // El permiso se pide en tiempo de ejecución en BuscarDispositivoScreen antes de llamar aquí
    @SuppressLint("MissingPermission")
    override suspend fun ubicacionActual(): Result<Coordenadas> = resultado {
        val token = CancellationTokenSource()
        val actual = cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).await()
        // Si el GPS aún no tiene posición, se usa la última conocida
        val ubicacion = actual ?: cliente.lastLocation.await()
            ?: error("No se pudo obtener la ubicación. Activa el GPS e intenta otra vez.")
        Coordenadas(ubicacion.latitude, ubicacion.longitude)
    }
}

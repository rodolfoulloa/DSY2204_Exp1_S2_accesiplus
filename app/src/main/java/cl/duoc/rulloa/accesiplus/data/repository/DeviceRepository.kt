package cl.duoc.rulloa.accesiplus.data.repository

import cl.duoc.rulloa.accesiplus.data.model.Device
import cl.duoc.rulloa.accesiplus.data.model.deviceFromMap
import cl.duoc.rulloa.accesiplus.data.model.toMap
import cl.duoc.rulloa.accesiplus.data.remote.RutasFirebase
import cl.duoc.rulloa.accesiplus.data.remote.awaitEscritura
import cl.duoc.rulloa.accesiplus.data.remote.observarValor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** CRUD de dispositivos (audífono, implante, teléfono…) en users/{uid}/devices. */
interface DeviceRepository {
    fun observarDispositivos(uid: String): Flow<List<Device>>
    suspend fun crear(uid: String, dispositivo: Device): Result<String>
    suspend fun actualizar(uid: String, dispositivo: Device): Result<Unit>
    suspend fun eliminar(uid: String, id: String): Result<Unit>
    suspend fun guardarUbicacion(uid: String, id: String, lat: Double, lon: Double): Result<Unit>
}

class FirebaseDeviceRepository(
    private val rutas: RutasFirebase,
    private val reloj: () -> Long = System::currentTimeMillis
) : DeviceRepository {

    override fun observarDispositivos(uid: String): Flow<List<Device>> =
        rutas.dispositivos(uid).observarValor().map { snap ->
            snap.children.mapNotNull { hijo -> deviceFromMap(hijo.key.orEmpty(), hijo.value as? Map<*, *>) }
                .sortedByDescending { it.createdAt }
        }

    override suspend fun crear(uid: String, dispositivo: Device) = resultado {
        val ref = rutas.dispositivos(uid).push()
        ref.setValue(dispositivo.copy(createdAt = reloj()).toMap()).awaitEscritura()
        ref.key.orEmpty()
    }

    override suspend fun actualizar(uid: String, dispositivo: Device) = resultado {
        require(dispositivo.id.isNotBlank()) { "Dispositivo sin id" }
        rutas.dispositivos(uid).child(dispositivo.id).setValue(dispositivo.toMap()).awaitEscritura()
    }

    override suspend fun eliminar(uid: String, id: String) = resultado {
        rutas.dispositivos(uid).child(id).removeValue().awaitEscritura()
    }

    override suspend fun guardarUbicacion(uid: String, id: String, lat: Double, lon: Double) = resultado {
        // updateChildren modifica solo estos tres campos sin pisar el resto
        rutas.dispositivos(uid).child(id).updateChildren(
            mapOf("lat" to lat, "lon" to lon, "locationAt" to reloj())
        ).awaitEscritura()
    }
}

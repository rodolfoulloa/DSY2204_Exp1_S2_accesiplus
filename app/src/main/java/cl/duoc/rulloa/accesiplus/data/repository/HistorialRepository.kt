package cl.duoc.rulloa.accesiplus.data.repository

import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.registroHistorialFromMap
import cl.duoc.rulloa.accesiplus.data.model.toMap
import cl.duoc.rulloa.accesiplus.data.remote.RutasFirebase
import cl.duoc.rulloa.accesiplus.data.remote.awaitEscritura
import cl.duoc.rulloa.accesiplus.data.remote.observarValor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Historial automático de Escribir y Hablar en users/{uid}/history. */
interface HistorialRepository {
    /** Últimos [limite] registros, del más reciente al más antiguo. */
    fun observar(uid: String, limite: Int = LIMITE_HISTORIAL): Flow<List<RegistroHistorial>>
    suspend fun agregar(uid: String, registro: RegistroHistorial): Result<String>
    suspend fun eliminar(uid: String, id: String): Result<Unit>

    /** Vuelve a escribir un registro borrado con su id y su hora originales (Deshacer). */
    suspend fun restaurar(uid: String, registro: RegistroHistorial): Result<Unit>
    suspend fun borrarTodo(uid: String): Result<Unit>

    companion object {
        const val LIMITE_HISTORIAL = 200
    }
}

/** Lee un hijo de users/{uid}/history: la clave del nodo es el id del registro. */
fun DataSnapshot.aRegistroHistorial(): RegistroHistorial? =
    registroHistorialFromMap(key.orEmpty(), value as? Map<*, *>)

class FirebaseHistorialRepository(private val rutas: RutasFirebase) : HistorialRepository {

    override fun observar(uid: String, limite: Int): Flow<List<RegistroHistorial>> =
        // orderByChild + limitToLast: Firebase entrega solo los N más nuevos (usa ".indexOn")
        rutas.historial(uid).orderByChild(CAMPO_TIMESTAMP).limitToLast(limite).observarValor().map { snap ->
            snap.children.mapNotNull { it.aRegistroHistorial() }.sortedByDescending { it.timestamp }
        }

    override suspend fun agregar(uid: String, registro: RegistroHistorial) = resultado {
        val ref = rutas.historial(uid).push()
        // La hora la pone el servidor: no depende del reloj del teléfono. Sin conexión,
        // Firebase usa una estimación local y la corrige al sincronizar.
        ref.setValue(registro.toMap(marcaTiempo = ServerValue.TIMESTAMP)).awaitEscritura()
        ref.key.orEmpty()
    }

    override suspend fun eliminar(uid: String, id: String) = resultado {
        require(id.isNotBlank()) { "Registro sin id" }
        rutas.historial(uid).child(id).removeValue().awaitEscritura()
    }

    override suspend fun restaurar(uid: String, registro: RegistroHistorial) = resultado {
        require(registro.id.isNotBlank()) { "Registro sin id" }
        rutas.historial(uid).child(registro.id).setValue(registro.toMap()).awaitEscritura()
    }

    override suspend fun borrarTodo(uid: String) = resultado {
        rutas.historial(uid).removeValue().awaitEscritura()
    }

    private companion object {
        const val CAMPO_TIMESTAMP = "timestamp"
    }
}

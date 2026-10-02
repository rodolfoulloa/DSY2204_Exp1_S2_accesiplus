package cl.duoc.rulloa.accesiplus.data.repository

import cl.duoc.rulloa.accesiplus.data.model.Phrase
import cl.duoc.rulloa.accesiplus.data.model.phraseFromMap
import cl.duoc.rulloa.accesiplus.data.model.toMap
import cl.duoc.rulloa.accesiplus.data.remote.RutasFirebase
import cl.duoc.rulloa.accesiplus.data.remote.awaitEscritura
import cl.duoc.rulloa.accesiplus.data.remote.observarValor
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** CRUD de frases guardadas en users/{uid}/phrases (Escribir y Hablar). */
interface PhraseRepository {
    fun observarFrases(uid: String): Flow<List<Phrase>>
    suspend fun crear(uid: String, frase: Phrase): Result<String>
    suspend fun actualizar(uid: String, frase: Phrase): Result<Unit>
    suspend fun eliminar(uid: String, id: String): Result<Unit>
    suspend fun registrarUso(uid: String, id: String): Result<Unit>
}

class FirebasePhraseRepository(
    private val rutas: RutasFirebase,
    private val reloj: () -> Long = System::currentTimeMillis
) : PhraseRepository {

    override fun observarFrases(uid: String): Flow<List<Phrase>> =
        rutas.frases(uid).observarValor().map { snap ->
            snap.children.mapNotNull { hijo -> phraseFromMap(hijo.key.orEmpty(), hijo.value as? Map<*, *>) }
        }

    override suspend fun crear(uid: String, frase: Phrase) = resultado {
        val ref = rutas.frases(uid).push()
        val ahora = reloj()
        ref.setValue(frase.copy(createdAt = ahora, updatedAt = ahora).toMap()).awaitEscritura()
        ref.key.orEmpty()
    }

    override suspend fun actualizar(uid: String, frase: Phrase) = resultado {
        require(frase.id.isNotBlank()) { "Frase sin id" }
        rutas.frases(uid).child(frase.id).setValue(frase.copy(updatedAt = reloj()).toMap()).awaitEscritura()
    }

    override suspend fun eliminar(uid: String, id: String) = resultado {
        rutas.frases(uid).child(id).removeValue().awaitEscritura()
    }

    override suspend fun registrarUso(uid: String, id: String) = resultado {
        // ServerValue.increment evita perder usos si dos dispositivos escriben a la vez
        rutas.frases(uid).child(id).child("uses").setValue(ServerValue.increment(1)).awaitEscritura()
    }
}

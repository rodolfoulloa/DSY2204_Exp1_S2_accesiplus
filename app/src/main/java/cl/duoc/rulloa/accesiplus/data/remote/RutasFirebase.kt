package cl.duoc.rulloa.accesiplus.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Único punto de acceso a referencias de Realtime Database.
 * Todas las rutas cuelgan de users/{uid}: así las reglas de seguridad
 * (auth.uid === $uid) cubren cualquier lectura o escritura de la app.
 */
class RutasFirebase(private val db: FirebaseDatabase) {
    fun usuario(uid: String): DatabaseReference {
        require(uid.isNotBlank()) { "uid vacío" }
        return db.getReference(NODO_USERS).child(uid)
    }

    fun perfil(uid: String) = usuario(uid).child(NODO_PROFILE)
    fun frases(uid: String) = usuario(uid).child(NODO_PHRASES)
    fun dispositivos(uid: String) = usuario(uid).child(NODO_DEVICES)

    companion object {
        const val NODO_USERS = "users"
        const val NODO_PROFILE = "profile"
        const val NODO_PHRASES = "phrases"
        const val NODO_DEVICES = "devices"
    }
}

/** Convierte un ValueEventListener en un Flow frío que se cancela al dejar de observar. */
fun DatabaseReference.observarValor(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            trySend(snapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

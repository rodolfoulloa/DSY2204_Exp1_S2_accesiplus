package cl.duoc.rulloa.accesiplus.data.repository

import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import cl.duoc.rulloa.accesiplus.data.model.toMap
import cl.duoc.rulloa.accesiplus.data.model.userProfileFromMap
import cl.duoc.rulloa.accesiplus.data.remote.RutasFirebase
import cl.duoc.rulloa.accesiplus.data.remote.awaitEscritura
import cl.duoc.rulloa.accesiplus.data.remote.observarValor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** CRUD del perfil en users/{uid}/profile. */
interface UserRepository {
    fun observarPerfil(uid: String): Flow<UserProfile?>
    suspend fun crearPerfil(uid: String, perfil: UserProfile): Result<Unit>
    suspend fun actualizarNombre(uid: String, nombre: String): Result<Unit>
    /** Borra todo users/{uid} (perfil, frases y dispositivos). */
    suspend fun eliminarDatos(uid: String): Result<Unit>
}

class FirebaseUserRepository(private val rutas: RutasFirebase) : UserRepository {

    override fun observarPerfil(uid: String): Flow<UserProfile?> =
        rutas.perfil(uid).observarValor().map { userProfileFromMap(it.value as? Map<*, *>) }

    override suspend fun crearPerfil(uid: String, perfil: UserProfile) = resultado {
        rutas.perfil(uid).setValue(perfil.toMap()).awaitEscritura()
    }

    override suspend fun actualizarNombre(uid: String, nombre: String) = resultado {
        rutas.perfil(uid).child("name").setValue(nombre.trim()).awaitEscritura()
    }

    override suspend fun eliminarDatos(uid: String) = resultado {
        // Aquí sí se exige confirmación del servidor: después se borra la cuenta de Auth
        // y sin sesión las reglas ya no permitirían limpiar el nodo.
        rutas.usuario(uid).removeValue().await()
        Unit
    }
}

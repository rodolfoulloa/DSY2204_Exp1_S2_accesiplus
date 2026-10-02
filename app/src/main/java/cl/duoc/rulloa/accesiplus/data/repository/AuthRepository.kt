package cl.duoc.rulloa.accesiplus.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

/** Autenticación con correo y contraseña. Los ViewModels solo conocen esta interfaz. */
interface AuthRepository {
    val uidActual: String?
    val correoActual: String?

    /** Emite el uid de la sesión activa o null si no hay sesión. */
    fun estadoSesion(): Flow<String?>

    suspend fun iniciarSesion(correo: String, clave: String): Result<String>
    suspend fun registrar(correo: String, clave: String): Result<String>
    suspend fun enviarRecuperacion(correo: String): Result<Unit>
    suspend fun reautenticar(clave: String): Result<Unit>
    suspend fun eliminarCuenta(): Result<Unit>
    fun cerrarSesion()
}

class FirebaseAuthRepository(private val auth: FirebaseAuth) : AuthRepository {

    override val uidActual: String? get() = auth.currentUser?.uid
    override val correoActual: String? get() = auth.currentUser?.email

    override fun estadoSesion(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun iniciarSesion(correo: String, clave: String) = resultado {
        val res = auth.signInWithEmailAndPassword(correo.trim(), clave).await()
        res.user?.uid ?: error("Sin usuario")
    }

    override suspend fun registrar(correo: String, clave: String) = resultado {
        val res = auth.createUserWithEmailAndPassword(correo.trim(), clave).await()
        res.user?.uid ?: error("Sin usuario")
    }

    override suspend fun enviarRecuperacion(correo: String) = resultado {
        auth.sendPasswordResetEmail(correo.trim()).await()
        Unit
    }

    override suspend fun reautenticar(clave: String) = resultado {
        val usuario = auth.currentUser ?: error("Sin sesión")
        val credencial = EmailAuthProvider.getCredential(usuario.email.orEmpty(), clave)
        usuario.reauthenticate(credencial).await()
        Unit
    }

    override suspend fun eliminarCuenta() = resultado {
        val usuario = auth.currentUser ?: error("Sin sesión")
        usuario.delete().await()
        Unit
    }

    override fun cerrarSesion() = auth.signOut()
}

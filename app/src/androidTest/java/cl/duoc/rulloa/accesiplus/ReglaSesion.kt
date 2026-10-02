package cl.duoc.rulloa.accesiplus

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.rules.ExternalResource

/**
 * Deja la sesión de Firebase en el estado pedido ANTES de que se abra MainActivity.
 * Las credenciales llegan como argumentos del runner desde local.properties
 * (no están en el código ni en el repositorio).
 */
class ReglaSesion(private val iniciada: Boolean) : ExternalResource() {

    override fun before() {
        val auth = Firebase.auth
        auth.signOut()
        if (iniciada) {
            runBlocking { auth.signInWithEmailAndPassword(correo, clave).await() }
        }
    }

    companion object {
        private val args get() = InstrumentationRegistry.getArguments()
        val correo: String get() = requireNotNull(args.getString("pruebaCorreo")) {
            "Falta accesiplus.pruebaCorreo en local.properties"
        }
        val clave: String get() = requireNotNull(args.getString("pruebaClave")) {
            "Falta accesiplus.pruebaClave en local.properties"
        }
    }
}

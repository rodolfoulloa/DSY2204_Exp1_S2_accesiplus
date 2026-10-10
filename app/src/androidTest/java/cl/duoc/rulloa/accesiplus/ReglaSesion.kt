package cl.duoc.rulloa.accesiplus

import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.rulloa.accesiplus.data.local.PreferenciasApp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.rules.ExternalResource

/**
 * Deja la sesión de Firebase en el estado pedido ANTES de que se abra MainActivity.
 * Las credenciales llegan como argumentos del runner desde local.properties
 * (no están en el código ni en el repositorio).
 *
 * @param tutorialVisto true (por defecto) para que las pruebas lleguen directo al menú;
 * false para probar el tutorial de primer inicio.
 */
class ReglaSesion(
    private val iniciada: Boolean,
    private val tutorialVisto: Boolean = true
) : ExternalResource() {

    override fun before() {
        val auth = Firebase.auth
        auth.signOut()
        // Se inicia sesión siempre para conocer el uid y fijar la bandera del tutorial
        val uid = runBlocking { auth.signInWithEmailAndPassword(correo, clave).await() }.user!!.uid
        val prefs = PreferenciasApp(InstrumentationRegistry.getInstrumentation().targetContext)
        if (tutorialVisto) prefs.marcarTutorialVisto(uid) else prefs.reiniciarTutorial(uid)
        if (!iniciada) auth.signOut()
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

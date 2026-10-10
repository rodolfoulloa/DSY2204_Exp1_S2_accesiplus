package cl.duoc.rulloa.accesiplus.ui.profile

import cl.duoc.rulloa.accesiplus.ReglaDispatcherPrincipal
import cl.duoc.rulloa.accesiplus.VibradorDePrueba
import cl.duoc.rulloa.accesiplus.data.haptica.PatronVibracion
import cl.duoc.rulloa.accesiplus.data.local.Preferencias
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/** Interruptor "Vibración" del perfil: se guarda, se aplica al instante y da una muestra al activarlo. */
class ProfileViewModelVibracionTest {

    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    /** Preferencias en memoria conectadas al interruptor del vibrador de prueba. */
    private class PrefsEnMemoria(private val vibrador: VibradorDePrueba) : Preferencias {
        var guardada = true
        override fun tutorialVisto(uid: String) = true
        override fun marcarTutorialVisto(uid: String) = Unit
        override fun reiniciarTutorial(uid: String) = Unit
        override fun vibracionActiva() = guardada
        override fun cambiarVibracion(activa: Boolean) {
            guardada = activa
            vibrador.activa = activa
        }
    }

    private val vibrador = VibradorDePrueba()
    private val prefs = PrefsEnMemoria(vibrador)

    private fun crear(): ProfileViewModel {
        val auth: AuthRepository = mock { on { estadoSesion() } doReturn emptyFlow() }
        val usuarios: UserRepository = mock()
        return ProfileViewModel(auth, usuarios, prefs, vibrador.haptica)
    }

    @Test
    fun `el interruptor parte con el valor guardado (activado por defecto)`() {
        assertTrue(crear().vibracion.value)
        prefs.guardada = false
        assertFalse(crear().vibracion.value)
    }

    @Test
    fun `apagar guarda la preferencia y no vibra`() {
        val vm = crear()
        vm.cambiarVibracion(false)
        assertFalse(vm.vibracion.value)
        assertFalse(prefs.guardada)
        assertTrue(vibrador.patrones.isEmpty())
    }

    @Test
    fun `encender guarda la preferencia y da un toque de muestra`() {
        val vm = crear()
        vm.cambiarVibracion(false)
        vm.cambiarVibracion(true)
        assertTrue(prefs.guardada)
        assertEquals(listOf(PatronVibracion.EXITO), vibrador.patrones)
    }
}

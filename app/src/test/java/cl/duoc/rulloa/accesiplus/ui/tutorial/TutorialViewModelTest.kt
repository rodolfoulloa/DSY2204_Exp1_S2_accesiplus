package cl.duoc.rulloa.accesiplus.ui.tutorial

import cl.duoc.rulloa.accesiplus.data.local.Preferencias
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/** Mockito: cuándo se muestra el tutorial y cómo se marca como visto. */
class TutorialViewModelTest {

    /** Preferencias en memoria: misma lógica que SharedPreferences, sin Android. */
    private class PreferenciasEnMemoria : Preferencias {
        val vistos = mutableSetOf<String>()
        override fun tutorialVisto(uid: String) = uid in vistos
        override fun marcarTutorialVisto(uid: String) { vistos += uid }
        override fun reiniciarTutorial(uid: String) { vistos -= uid }
    }

    private fun auth(uid: String?): AuthRepository = mock { on { uidActual } doReturn uid }

    @Test
    fun `se muestra la primera vez y no despues de completarlo`() {
        val vm = TutorialViewModel(auth("u1"), PreferenciasEnMemoria())
        assertTrue(vm.debeMostrar())
        vm.completar()
        assertFalse(vm.debeMostrar())
    }

    @Test
    fun `cada usuario tiene su propia bandera`() {
        val prefs = PreferenciasEnMemoria()
        TutorialViewModel(auth("u1"), prefs).completar()
        assertTrue(TutorialViewModel(auth("u2"), prefs).debeMostrar())
        assertEquals(setOf("u1"), prefs.vistos)
    }

    @Test
    fun `sin sesion no se muestra ni se guarda nada`() {
        val prefs: Preferencias = mock()
        val vm = TutorialViewModel(auth(null), prefs)
        assertFalse(vm.debeMostrar())
        vm.completar()
        verify(prefs, never()).marcarTutorialVisto(any())
    }

    @Test
    fun `botones y texto del indicador segun la pagina`() {
        assertEquals(4, PasosTutorial.paginas.size)
        assertEquals("Siguiente", PasosTutorial.textoBoton(0))
        assertEquals("Siguiente", PasosTutorial.textoBoton(2))
        assertEquals("Comenzar", PasosTutorial.textoBoton(3))
        assertTrue(PasosTutorial.esUltima(3))
        assertEquals("Página 2 de 4", PasosTutorial.descripcionIndicador(1))
    }
}

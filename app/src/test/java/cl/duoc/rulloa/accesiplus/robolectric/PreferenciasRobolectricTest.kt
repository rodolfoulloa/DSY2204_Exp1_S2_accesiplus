package cl.duoc.rulloa.accesiplus.robolectric

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.rulloa.accesiplus.data.local.PreferenciasApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Robolectric: SharedPreferences reales para la bandera del tutorial por usuario. */
@RunWith(AndroidJUnit4::class)
class PreferenciasRobolectricTest {

    private lateinit var context: Context
    private lateinit var prefs: PreferenciasApp

    @Before
    fun preparar() {
        context = ApplicationProvider.getApplicationContext()
        prefs = PreferenciasApp(context)
    }

    @Test
    fun `un usuario nuevo no ha visto el tutorial`() {
        assertFalse(prefs.tutorialVisto("u1"))
    }

    @Test
    fun `marcar visto se guarda solo para ese uid`() {
        prefs.marcarTutorialVisto("u1")
        assertTrue(prefs.tutorialVisto("u1"))
        assertFalse(prefs.tutorialVisto("u2"))
    }

    @Test
    fun `la bandera sobrevive a una nueva instancia (persistencia real)`() {
        prefs.marcarTutorialVisto("u1")
        assertTrue(PreferenciasApp(context).tutorialVisto("u1"))
        val guardado = context.getSharedPreferences(PreferenciasApp.ARCHIVO, Context.MODE_PRIVATE)
        assertEquals(true, guardado.getBoolean("tutorial_visto_u1", false))
    }

    @Test
    fun `la vibracion viene activada y el cambio se guarda`() {
        assertTrue(prefs.vibracionActiva())
        prefs.cambiarVibracion(false)
        assertFalse(PreferenciasApp(context).vibracionActiva())
        prefs.cambiarVibracion(true)
        assertTrue(PreferenciasApp(context).vibracionActiva())
    }

    @Test
    fun `reiniciar vuelve a mostrar el tutorial`() {
        prefs.marcarTutorialVisto("u1")
        prefs.reiniciarTutorial("u1")
        assertFalse(prefs.tutorialVisto("u1"))
    }
}

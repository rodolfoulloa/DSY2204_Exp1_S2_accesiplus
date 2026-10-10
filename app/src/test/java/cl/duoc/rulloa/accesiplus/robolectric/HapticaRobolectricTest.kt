package cl.duoc.rulloa.accesiplus.robolectric

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.rulloa.accesiplus.data.haptica.EventoHaptico
import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.local.PreferenciasApp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/** Robolectric: Haptica con el Vibrator del sistema simulado (VibratorManager en API 31+). */
@RunWith(AndroidJUnit4::class)
class HapticaRobolectricTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `obtiene el vibrador del sistema y vibra`() {
        val vibrator = Haptica.obtenerVibrator(context)
        assertNotNull(vibrator)
        assertTrue(Haptica(context) { true }.avisar(EventoHaptico.FRASE_GUARDADA))
        assertTrue(shadowOf(vibrator).isVibrating)
    }

    @Test
    fun `respeta el interruptor guardado en SharedPreferences`() {
        val prefs = PreferenciasApp(context)
        prefs.cambiarVibracion(false)
        val haptica = Haptica(context) { prefs.vibracionActiva() }
        assertFalse(haptica.avisar(EventoHaptico.ERROR_LOGIN))
        assertFalse(shadowOf(Haptica.obtenerVibrator(context)).isVibrating)
    }
}

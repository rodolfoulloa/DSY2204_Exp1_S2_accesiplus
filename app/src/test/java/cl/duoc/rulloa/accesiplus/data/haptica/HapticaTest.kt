package cl.duoc.rulloa.accesiplus.data.haptica

import cl.duoc.rulloa.accesiplus.VibradorDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** JUnit: selección de patrón por evento y respeto del interruptor "Vibración". */
class HapticaTest {

    @Test
    fun `cada evento elige el patron que le corresponde`() {
        assertEquals(PatronVibracion.EXITO, Haptica.patronPara(EventoHaptico.ESCUCHA_TERMINADA))
        assertEquals(PatronVibracion.EXITO, Haptica.patronPara(EventoHaptico.VOZ_TERMINADA))
        assertEquals(PatronVibracion.GUARDADO, Haptica.patronPara(EventoHaptico.FRASE_GUARDADA))
        assertEquals(PatronVibracion.GUARDADO, Haptica.patronPara(EventoHaptico.DISPOSITIVO_GUARDADO))
        assertEquals(PatronVibracion.ERROR, Haptica.patronPara(EventoHaptico.ERROR_RECONOCEDOR))
        assertEquals(PatronVibracion.ERROR, Haptica.patronPara(EventoHaptico.ERROR_VOZ))
        assertEquals(PatronVibracion.ERROR, Haptica.patronPara(EventoHaptico.ERROR_LOGIN))
    }

    @Test
    fun `todos los errores vibran largo y ningun exito usa el patron de error`() {
        val errores = EventoHaptico.entries.filter { it.name.startsWith("ERROR") }
        assertTrue(errores.all { Haptica.patronPara(it) == PatronVibracion.ERROR })
        assertTrue((EventoHaptico.entries - errores.toSet()).none { Haptica.patronPara(it) == PatronVibracion.ERROR })
    }

    @Test
    fun `los patrones se distinguen por cantidad de pulsos y duracion`() {
        // tiempos = [espera, vibra, espera, vibra…]: las posiciones impares son pulsos
        fun pulsos(p: PatronVibracion) = p.tiempos.filterIndexed { i, _ -> i % 2 == 1 }
        assertEquals(1, pulsos(PatronVibracion.EXITO).size)
        assertEquals(2, pulsos(PatronVibracion.GUARDADO).size)
        assertEquals(1, pulsos(PatronVibracion.ERROR).size)
        assertTrue(pulsos(PatronVibracion.ERROR).single() >= 4 * pulsos(PatronVibracion.EXITO).single())
    }

    @Test
    fun `con el interruptor apagado no vibra`() {
        val v = VibradorDePrueba(activa = false)
        assertFalse(v.haptica.avisar(EventoHaptico.FRASE_GUARDADA))
        assertTrue(v.patrones.isEmpty())
    }

    @Test
    fun `el interruptor se lee en cada aviso`() {
        val v = VibradorDePrueba(activa = true)
        assertTrue(v.haptica.avisar(EventoHaptico.ERROR_LOGIN))
        v.activa = false
        assertFalse(v.haptica.avisar(EventoHaptico.ERROR_LOGIN))
        v.activa = true
        v.haptica.avisar(EventoHaptico.VOZ_TERMINADA)
        assertEquals(listOf(PatronVibracion.ERROR, PatronVibracion.EXITO), v.patrones)
    }

    @Test
    fun `sin motor de vibracion no hace nada`() {
        val sinMotor = Haptica(salida = null) { true }
        assertFalse(sinMotor.avisar(EventoHaptico.VOZ_TERMINADA))
        assertFalse(Haptica.NINGUNA.avisar(EventoHaptico.VOZ_TERMINADA))
    }
}

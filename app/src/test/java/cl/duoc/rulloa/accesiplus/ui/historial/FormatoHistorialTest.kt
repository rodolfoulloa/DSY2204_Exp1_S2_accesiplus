package cl.duoc.rulloa.accesiplus.ui.historial

import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

/** JUnit: fechas del historial en español de Chile. */
class FormatoHistorialTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val dia = 24 * 60 * 60 * 1000L
    private val lunes = 1_759_708_800_000L // 2025-10-06 00:00 UTC

    @Test
    fun `hoy, ayer y fechas anteriores con el nombre del dia`() {
        val ahora = lunes + 2 * dia + 3_600_000 // miércoles 8, 01:00
        assertEquals("Hoy", FormatoHistorial.etiquetaDia(lunes + 2 * dia, ahora, utc))
        assertEquals("Ayer", FormatoHistorial.etiquetaDia(lunes + dia, ahora, utc))
        assertEquals("Lunes 6 de octubre de 2025", FormatoHistorial.etiquetaDia(lunes, ahora, utc))
    }

    @Test
    fun `hora en formato de 24 horas`() {
        assertEquals("14:05", FormatoHistorial.hora(lunes + (14 * 60 + 5) * 60_000L, utc))
    }
}

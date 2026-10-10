package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** JUnit: filtros, orden, agrupación por día y antiduplicados del historial. */
class FiltrosHistorialTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val dia = 24 * 60 * 60 * 1000L
    private val lunes = 1_759_708_800_000L // 2025-10-06 00:00 UTC

    private val registros = listOf(
        RegistroHistorial("a", TipoHistorial.ESCRIBIR, "Hola", timestamp = lunes + 1_000),
        RegistroHistorial("b", TipoHistorial.HABLAR, "Gracias", timestamp = lunes + dia + 5_000),
        RegistroHistorial("c", TipoHistorial.HABLAR, "Ayuda", timestamp = lunes + 9_000),
        RegistroHistorial("d", TipoHistorial.ESCRIBIR, "Buenos días", timestamp = lunes + dia + 1_000)
    )

    @Test
    fun `prepararTexto recorta espacios, limita el largo e ignora vacios`() {
        assertEquals("Hola", FiltrosHistorial.prepararTexto("  Hola  "))
        assertNull(FiltrosHistorial.prepararTexto("   "))
        assertEquals(FiltrosHistorial.LARGO_MAX_TEXTO, FiltrosHistorial.prepararTexto("a".repeat(3000))!!.length)
    }

    @Test
    fun `porTipo filtra y null devuelve todos`() {
        assertEquals(listOf("b", "c"), FiltrosHistorial.porTipo(registros, TipoHistorial.HABLAR).map { it.id })
        assertEquals(4, FiltrosHistorial.porTipo(registros, null).size)
    }

    @Test
    fun `ordenar deja primero el mas reciente`() {
        assertEquals(listOf("b", "d", "c", "a"), FiltrosHistorial.ordenar(registros).map { it.id })
    }

    @Test
    fun `agruparPorDia separa por fecha y conserva el orden descendente`() {
        val grupos = FiltrosHistorial.agruparPorDia(registros, utc)
        assertEquals(listOf(lunes + dia, lunes), grupos.keys.toList())
        assertEquals(listOf("b", "d"), grupos.getValue(lunes + dia).map { it.id })
        assertEquals(listOf("c", "a"), grupos.getValue(lunes).map { it.id })
    }

    @Test
    fun `conteoPorTipo cuenta cada tipo`() {
        assertEquals(mapOf(TipoHistorial.ESCRIBIR to 2, TipoHistorial.HABLAR to 2), FiltrosHistorial.conteoPorTipo(registros))
    }

    @Test
    fun `filtro de duplicados ignora el mismo texto dentro de la ventana`() {
        var ahora = 0L
        val filtro = FiltroDuplicados(ventanaMs = 2_000, reloj = { ahora })
        assertFalse(filtro.esRepetido(TipoHistorial.HABLAR, "Hola"))
        ahora = 500
        assertTrue(filtro.esRepetido(TipoHistorial.HABLAR, "hola ")) // mismo texto normalizado
        assertFalse(filtro.esRepetido(TipoHistorial.ESCRIBIR, "Hola")) // otro tipo no es duplicado
        ahora = 5_000
        assertFalse(filtro.esRepetido(TipoHistorial.ESCRIBIR, "Hola")) // fuera de la ventana
    }
}

package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** JUnit: indicación del siguiente paso (paso 6) y saludo con nombre (paso 2). */
class GuiaAccionTest {

    @Test
    fun `etapas de Escribir con el error como prioridad`() {
        assertEquals(EtapaAccion.INICIO, GuiaAccion.etapaEscribir(false, false, false, false))
        assertEquals(EtapaAccion.ESCUCHANDO, GuiaAccion.etapaEscribir(true, false, false, true))
        assertEquals(EtapaAccion.PROCESANDO, GuiaAccion.etapaEscribir(false, true, false, false))
        assertEquals(EtapaAccion.LISTO, GuiaAccion.etapaEscribir(false, false, false, true))
        assertEquals(EtapaAccion.ERROR, GuiaAccion.etapaEscribir(true, true, true, true))
    }

    @Test
    fun `etapas de Hablar`() {
        assertEquals(EtapaAccion.PROCESANDO, GuiaAccion.etapaHablar(preparando = true, hablando = false, hayError = false, completada = false))
        assertEquals(EtapaAccion.PROCESANDO, GuiaAccion.etapaHablar(false, hablando = true, hayError = false, completada = true))
        assertEquals(EtapaAccion.LISTO, GuiaAccion.etapaHablar(false, false, false, completada = true))
        assertEquals(EtapaAccion.ERROR, GuiaAccion.etapaHablar(false, true, hayError = true, completada = true))
    }

    @Test
    fun `cada etapa tiene un mensaje distinto`() {
        val escribir = EtapaAccion.entries.map(GuiaAccion::mensajeEscribir)
        assertEquals(escribir.size, escribir.toSet().size)
        assertTrue(GuiaAccion.mensajeEscribir(EtapaAccion.LISTO).contains("volver al menú"))
    }

    @Test
    fun `Hablar guia segun si hay texto y si la voz se esta preparando`() {
        assertTrue(GuiaAccion.mensajeHablar(EtapaAccion.INICIO, hayTexto = false).startsWith("Paso 1"))
        assertTrue(GuiaAccion.mensajeHablar(EtapaAccion.INICIO, hayTexto = true).startsWith("Paso 2"))
        assertEquals("Preparando la voz…", GuiaAccion.mensajeHablar(EtapaAccion.PROCESANDO, false, preparando = true))
    }

    @Test
    fun `saludo usa el primer nombre del perfil`() {
        val perfil = UserProfile(name = "  Ana María Pérez ")
        assertEquals("Ana", Saludos.nombre(perfil, "ana@correo.cl"))
        assertEquals("Hola, Ana", Saludos.texto(Saludos.nombre(perfil, null)))
    }

    @Test
    fun `sin nombre en el perfil se usa el correo y sin datos queda Hola`() {
        assertEquals("rodolfo", Saludos.nombre(UserProfile(name = " "), "rodolfo@duoc.cl"))
        assertEquals("rodolfo", Saludos.nombre(null, "rodolfo@duoc.cl"))
        assertNull(Saludos.nombre(null, null))
        assertEquals("Hola", Saludos.texto(null))
    }
}

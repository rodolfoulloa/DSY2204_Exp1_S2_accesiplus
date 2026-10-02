package cl.duoc.rulloa.accesiplus.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** JUnit: conversión entre modelos y los Map que usa Realtime Database. */
class MapeoTest {

    @Test
    fun `frase ida y vuelta conserva los datos`() {
        val f = Phrase("abc", "Hola", "Salud", favorite = true, uses = 3, createdAt = 10, updatedAt = 20)
        assertEquals(f, phraseFromMap("abc", f.toMap()))
    }

    @Test
    fun `numeros llegan como Long o Double desde Firebase`() {
        val f = phraseFromMap("x", mapOf("text" to "Hola", "uses" to 2.0, "createdAt" to 5L))
        assertEquals(2L, f?.uses)
        assertEquals(5L, f?.createdAt)
    }

    @Test
    fun `frase sin texto se descarta y campos faltantes toman valores por defecto`() {
        assertNull(phraseFromMap("x", mapOf("category" to "Salud")))
        val f = phraseFromMap("x", mapOf("text" to "Hola"))!!
        assertEquals(Phrase.CATEGORIA_PROPIA, f.category)
        assertFalse(f.favorite)
    }

    @Test
    fun `dispositivo con y sin ubicacion`() {
        val sin = deviceFromMap("d1", mapOf("name" to "Audífono", "type" to "Audífono", "createdAt" to 1L))!!
        assertFalse(sin.tieneUbicacion)
        val con = deviceFromMap("d1", sin.copy(lat = -33.45, lon = -70.64, locationAt = 9L).toMap())!!
        assertTrue(con.tieneUbicacion)
        assertEquals(-33.45, con.lat!!, 0.0001)
        assertEquals(9L, con.locationAt)
    }

    @Test
    fun `perfil ida y vuelta`() {
        val p = UserProfile("Ana", "ana@correo.cl", "Cuidador", "Femenino", 123L)
        assertEquals(p, userProfileFromMap(p.toMap()))
        assertNull(userProfileFromMap(null))
    }
}

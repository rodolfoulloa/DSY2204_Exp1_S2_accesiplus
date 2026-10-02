package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.Phrase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** JUnit: filtros de frases (lógica pura, sin Android). */
class FiltrosFrasesTest {

    private val frases = listOf(
        Phrase(id = "1", text = "Necesito una hora con el médico.", category = "Salud", favorite = true, updatedAt = 100),
        Phrase(id = "2", text = "¿Cuánto cuesta esto?", category = "Compras", updatedAt = 200),
        Phrase(id = "3", text = "Me duele aquí.", category = "Salud", favorite = true, updatedAt = 300),
        Phrase(id = "4", text = "Hola, soy sordo", category = Phrase.CATEGORIA_PROPIA)
    )

    @Test
    fun `normalizar quita tildes, mayusculas y espacios`() {
        assertEquals("medico cuanto", FiltrosFrases.normalizar("  Médico CUÁNTO "))
    }

    @Test
    fun `porCategoria devuelve solo las frases de esa categoria`() {
        val salud = FiltrosFrases.porCategoria(frases, "Salud")
        assertEquals(listOf("1", "3"), salud.map { it.id })
    }

    @Test
    fun `porCategoria sin categoria devuelve todas`() {
        assertEquals(4, FiltrosFrases.porCategoria(frases, null).size)
        assertEquals(4, FiltrosFrases.porCategoria(frases, "").size)
    }

    @Test
    fun `porTexto ignora tildes y mayusculas`() {
        assertEquals(listOf("1"), FiltrosFrases.porTexto(frases, "MEDICO").map { it.id })
        assertEquals(listOf("2"), FiltrosFrases.porTexto(frases, "cuanto").map { it.id })
    }

    @Test
    fun `porTexto vacio no filtra`() {
        assertEquals(frases, FiltrosFrases.porTexto(frases, "   "))
    }

    @Test
    fun `favoritas y ultima favorita`() {
        assertEquals(listOf("1", "3"), FiltrosFrases.favoritas(frases).map { it.id })
        assertEquals("3", FiltrosFrases.ultimaFavorita(frases)?.id)
    }

    @Test
    fun `ultimaFavorita es null si no hay favoritas`() {
        assertNull(FiltrosFrases.ultimaFavorita(frases.filterNot { it.favorite }))
    }

    @Test
    fun `existe detecta duplicados en la misma categoria`() {
        assertTrue(FiltrosFrases.existe(frases, "me DUELE aqui.", "Salud"))
        assertFalse(FiltrosFrases.existe(frases, "Me duele aquí.", "Compras"))
    }
}

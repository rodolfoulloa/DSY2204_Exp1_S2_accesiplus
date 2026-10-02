package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.Phrase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD: ordenar frases por uso. Estas pruebas se escribieron ANTES de implementar
 * OrdenFrases.ordenarPorUso (commit "test(rojo)") y luego se implementó la función
 * hasta que pasaron (commit "feat(verde)").
 *
 * Regla: más usos primero; si empatan, favoritas primero; si siguen empatadas,
 * la modificada más recientemente primero.
 */
class OrdenFrasesTest {

    private fun f(id: String, usos: Long, fav: Boolean = false, t: Long = 0) =
        Phrase(id = id, text = "Frase $id", uses = usos, favorite = fav, updatedAt = t)

    @Test
    fun `la mas usada va primero`() {
        val orden = OrdenFrases.ordenarPorUso(listOf(f("a", 1), f("b", 5), f("c", 3)))
        assertEquals(listOf("b", "c", "a"), orden.map { it.id })
    }

    @Test
    fun `con igual uso la favorita va primero`() {
        val orden = OrdenFrases.ordenarPorUso(listOf(f("a", 2), f("b", 2, fav = true)))
        assertEquals(listOf("b", "a"), orden.map { it.id })
    }

    @Test
    fun `con igual uso y favorita gana la mas reciente`() {
        val orden = OrdenFrases.ordenarPorUso(listOf(f("a", 0, t = 10), f("b", 0, t = 30), f("c", 0, t = 20)))
        assertEquals(listOf("b", "c", "a"), orden.map { it.id })
    }

    @Test
    fun `lista vacia devuelve lista vacia`() {
        assertTrue(OrdenFrases.ordenarPorUso(emptyList()).isEmpty())
    }

    @Test
    fun `no modifica la lista original`() {
        val original = listOf(f("a", 1), f("b", 9))
        OrdenFrases.ordenarPorUso(original)
        assertEquals(listOf("a", "b"), original.map { it.id })
    }

    @Test
    fun `top devuelve solo las n mas usadas`() {
        val top = OrdenFrases.masUsadas(listOf(f("a", 1), f("b", 5), f("c", 3), f("d", 0)), 2)
        assertEquals(listOf("b", "c"), top.map { it.id })
    }
}

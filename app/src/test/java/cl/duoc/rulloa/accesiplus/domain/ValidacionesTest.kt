package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.ui.devices.ConsejosFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** JUnit: validaciones que no dependen de Android (el correo se prueba con Robolectric). */
class ValidacionesTest {

    @Test
    fun `contrasena requiere al menos 6 caracteres`() {
        assertFalse(Validaciones.isPasswordValid(""))
        assertFalse(Validaciones.isPasswordValid("12345"))
        assertTrue(Validaciones.isPasswordValid("123456"))
    }

    @Test
    fun `nombre no puede estar vacio ni pasar 80 caracteres`() {
        assertFalse(Validaciones.isNameValid("   "))
        assertTrue(Validaciones.isNameValid("Ana"))
        assertFalse(Validaciones.isNameValid("a".repeat(81)))
    }

    @Test
    fun `frase entre 1 y 500 caracteres`() {
        assertFalse(Validaciones.isPhraseValid(""))
        assertTrue(Validaciones.isPhraseValid("Hola"))
        assertTrue(Validaciones.isPhraseValid("a".repeat(500)))
        assertFalse(Validaciones.isPhraseValid("a".repeat(501)))
    }

    @Test
    fun `consejos segun el tipo de dispositivo`() {
        assertEquals(4, ConsejosFragment.consejosPara("Audífono").size)
        assertEquals(ConsejosFragment.consejosPara("Audífono"), ConsejosFragment.consejosPara("Implante coclear"))
        assertEquals(3, ConsejosFragment.consejosPara("Teléfono").size)
        assertEquals(2, ConsejosFragment.consejosPara("Otro").size)
    }
}

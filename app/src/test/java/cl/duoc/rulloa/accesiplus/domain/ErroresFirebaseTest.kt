package cl.duoc.rulloa.accesiplus.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** JUnit: mapeo de códigos de error de Firebase Auth a mensajes en español simple. */
class ErroresFirebaseTest {

    @Test
    fun `credenciales invalidas dan el mismo mensaje sin revelar cual fallo`() {
        val esperado = "Correo o contraseña incorrectos."
        assertEquals(esperado, ErroresFirebase.porCodigo("ERROR_WRONG_PASSWORD"))
        assertEquals(esperado, ErroresFirebase.porCodigo("ERROR_INVALID_CREDENTIAL"))
        assertEquals(esperado, ErroresFirebase.porCodigo("ERROR_USER_NOT_FOUND"))
    }

    @Test
    fun `correo ya registrado`() {
        assertEquals(
            "Ese correo ya tiene una cuenta. Inicia sesión.",
            ErroresFirebase.porCodigo("ERROR_EMAIL_ALREADY_IN_USE")
        )
    }

    @Test
    fun `contrasena debil, correo invalido y reautenticacion`() {
        assertEquals("La contraseña es muy débil. Usa al menos 6 caracteres.", ErroresFirebase.porCodigo("ERROR_WEAK_PASSWORD"))
        assertEquals("El correo no tiene un formato válido.", ErroresFirebase.porCodigo("ERROR_INVALID_EMAIL"))
        assertEquals("Por seguridad, vuelve a escribir tu contraseña.", ErroresFirebase.porCodigo("ERROR_REQUIRES_RECENT_LOGIN"))
    }

    @Test
    fun `red y demasiados intentos`() {
        assertEquals(ErroresFirebase.SIN_CONEXION, ErroresFirebase.porCodigo("ERROR_NETWORK_REQUEST_FAILED"))
        assertEquals("Hiciste muchos intentos. Espera unos minutos.", ErroresFirebase.porCodigo("ERROR_TOO_MANY_REQUESTS"))
    }

    @Test
    fun `codigo desconocido o nulo da mensaje generico`() {
        assertEquals(ErroresFirebase.GENERICO, ErroresFirebase.porCodigo("ERROR_RARO"))
        assertEquals(ErroresFirebase.GENERICO, ErroresFirebase.porCodigo(null))
    }

    @Test
    fun `excepciones genericas`() {
        assertEquals(ErroresFirebase.GENERICO, ErroresFirebase.mensaje(null))
        assertEquals(ErroresFirebase.GENERICO, ErroresFirebase.mensaje(IllegalStateException("x")))
        assertEquals(
            "No tienes permiso para ver o cambiar estos datos.",
            ErroresFirebase.mensaje(RuntimeException("Firebase Database error: Permission denied"))
        )
    }
}

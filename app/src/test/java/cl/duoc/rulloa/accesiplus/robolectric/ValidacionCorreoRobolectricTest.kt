package cl.duoc.rulloa.accesiplus.robolectric

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Robolectric: isEmailValid usa android.util.Patterns, que solo existe en Android.
 * Robolectric entrega la implementación real del framework dentro de la JVM.
 */
@RunWith(AndroidJUnit4::class)
class ValidacionCorreoRobolectricTest {

    @Test
    fun `acepta correos validos`() {
        assertTrue(Validaciones.isEmailValid("prueba.s8@accesiplus.cl"))
        assertTrue(Validaciones.isEmailValid("ana_perez+duoc@correo.com"))
        assertTrue(Validaciones.isEmailValid("  ana@correo.cl  "))
    }

    @Test
    fun `rechaza correos invalidos`() {
        assertFalse(Validaciones.isEmailValid(""))
        assertFalse(Validaciones.isEmailValid("ana"))
        assertFalse(Validaciones.isEmailValid("ana@"))
        assertFalse(Validaciones.isEmailValid("@correo.cl"))
        assertFalse(Validaciones.isEmailValid("ana correo@cl"))
    }

    @Test
    fun `excepciones reales de Firebase se traducen`() {
        // Las excepciones de Firebase usan TextUtils al construirse: requieren Robolectric
        assertEquals(ErroresFirebase.SIN_CONEXION, ErroresFirebase.mensaje(FirebaseNetworkException("sin red")))
        assertEquals(
            "Correo o contraseña incorrectos.",
            ErroresFirebase.mensaje(FirebaseAuthException("ERROR_INVALID_CREDENTIAL", "mala"))
        )
        assertEquals(
            "Ese correo ya tiene una cuenta. Inicia sesión.",
            ErroresFirebase.mensaje(FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "usado"))
        )
    }
}

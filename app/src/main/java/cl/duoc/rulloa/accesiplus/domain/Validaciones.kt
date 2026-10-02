package cl.duoc.rulloa.accesiplus.domain

import android.util.Patterns

/** Reglas de validación de formularios (riesgo "validación de formularios" de la Exp. 1). */
object Validaciones {
    const val LARGO_MIN_CLAVE = 6
    const val LARGO_MAX_NOMBRE = 80
    const val LARGO_MAX_FRASE = 500

    /** Usa el patrón oficial de Android (android.util.Patterns): se prueba con Robolectric. */
    fun isEmailValid(correo: String): Boolean =
        correo.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()

    fun isPasswordValid(clave: String): Boolean = clave.length >= LARGO_MIN_CLAVE

    fun isNameValid(nombre: String): Boolean =
        nombre.isNotBlank() && nombre.trim().length <= LARGO_MAX_NOMBRE

    fun isPhraseValid(texto: String): Boolean =
        texto.isNotBlank() && texto.trim().length <= LARGO_MAX_FRASE
}

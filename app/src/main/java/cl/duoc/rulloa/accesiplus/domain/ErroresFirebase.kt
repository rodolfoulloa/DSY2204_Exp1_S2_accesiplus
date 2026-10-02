package cl.duoc.rulloa.accesiplus.domain

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseException

/**
 * Traduce los errores de Firebase a mensajes en español simple,
 * pensados para personas con baja alfabetización digital.
 */
object ErroresFirebase {

    const val GENERICO = "Algo salió mal. Intenta otra vez."
    const val SIN_CONEXION = "No hay conexión a internet. Revisa tu red e intenta otra vez."

    fun mensaje(error: Throwable?): String = when (error) {
        null -> GENERICO
        is FirebaseNetworkException -> SIN_CONEXION
        is FirebaseTooManyRequestsException -> porCodigo("ERROR_TOO_MANY_REQUESTS")
        is FirebaseAuthException -> porCodigo(error.errorCode)
        is DatabaseException -> "No se pudieron guardar los datos. Intenta otra vez."
        else -> if (error.message?.contains("Permission denied", ignoreCase = true) == true) {
            "No tienes permiso para ver o cambiar estos datos."
        } else GENERICO
    }

    /** Códigos de FirebaseAuthException.errorCode. */
    fun porCodigo(codigo: String?): String = when (codigo) {
        "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido."
        "ERROR_WRONG_PASSWORD",
        "ERROR_INVALID_CREDENTIAL",
        "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos."
        "ERROR_USER_DISABLED" -> "Esta cuenta está desactivada."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya tiene una cuenta. Inicia sesión."
        "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil. Usa al menos 6 caracteres."
        "ERROR_REQUIRES_RECENT_LOGIN" -> "Por seguridad, vuelve a escribir tu contraseña."
        "ERROR_TOO_MANY_REQUESTS" -> "Hiciste muchos intentos. Espera unos minutos."
        "ERROR_NETWORK_REQUEST_FAILED" -> SIN_CONEXION
        else -> GENERICO
    }
}

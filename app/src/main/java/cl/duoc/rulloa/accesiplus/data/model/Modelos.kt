package cl.duoc.rulloa.accesiplus.data.model

/**
 * Modelos de dominio. Se guardan en Realtime Database bajo users/{uid}/...
 * El mapeo hacia/desde Map es manual (ver Mapeo.kt) para no depender de reflexión
 * y para poder probarlo con JUnit sin Firebase.
 */
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = "Usuario Final",
    val gender: String = "Otro",
    val createdAt: Long = 0L
)

data class Phrase(
    val id: String = "",
    val text: String = "",
    val category: String = CATEGORIA_PROPIA,
    val favorite: Boolean = false,
    val uses: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    companion object {
        /** Categoría de las frases escritas por el usuario (no rápidas). */
        const val CATEGORIA_PROPIA = "Propias"

        /** Textos transcritos o tecleados en Escribir que el usuario decidió guardar. */
        const val CATEGORIA_ESCRITA = "Escritas"
    }
}

data class Device(
    val id: String = "",
    val name: String = "",
    val type: String = TIPOS.first(),
    val notes: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val locationAt: Long? = null,
    val createdAt: Long = 0L
) {
    val tieneUbicacion: Boolean get() = lat != null && lon != null

    companion object {
        val TIPOS = listOf("Audífono", "Implante coclear", "Teléfono", "Tablet", "Otro")
    }
}

/** Función de la app que generó el registro del historial. */
enum class TipoHistorial(val etiqueta: String) {
    ESCRIBIR("Escribir"),
    HABLAR("Hablar")
}

/** De dónde salió el texto que se dijo en voz alta (en Escribir no aplica: viene de la voz). */
enum class OrigenHistorial(val etiqueta: String) {
    TEXTO_LIBRE("Texto escrito"),
    FRASE_GUARDADA("Frase guardada"),
    WIDGET("Widget")
}

/**
 * Solicitud y respuesta guardada automáticamente en users/{uid}/history.
 * En Escribir el texto es lo que reconoció el micrófono; en Hablar, lo que dijo el teléfono.
 * [timestamp] lo asigna el servidor (ServerValue.TIMESTAMP).
 */
data class RegistroHistorial(
    val id: String = "",
    val tipo: TipoHistorial = TipoHistorial.HABLAR,
    val texto: String = "",
    val origen: OrigenHistorial? = null,
    val timestamp: Long = 0L
)

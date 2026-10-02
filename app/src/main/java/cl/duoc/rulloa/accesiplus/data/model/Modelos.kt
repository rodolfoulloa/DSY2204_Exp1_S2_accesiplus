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

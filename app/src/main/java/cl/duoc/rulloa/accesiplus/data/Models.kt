package cl.duoc.rulloa.accesiplus.data

data class CommunicationCategory(
    val id: Int,
    val title: String,
    val description: String,
    val iconName: String, // Referencia para iconos
    val importance: String // e.g., "Alta", "Media", "Baja"
)

data class AccessInfo(
    val id: Int,
    val feature: String,
    val status: String,
    val detail: String
)

object MockData {
    // Datos para el entorno de Accesibilidad Auditiva
    val communicationCategories = listOf(
        CommunicationCategory(1, "Salud", "Frases para emergencias médicas y farmacia.", "Medical", "Alta"),
        CommunicationCategory(2, "Compras", "Interacción en supermercados y tiendas.", "Shopping", "Media"),
        CommunicationCategory(3, "Transporte", "Consultas sobre rutas, buses y metros.", "Transport", "Media"),
        CommunicationCategory(4, "Hogar", "Comunicación diaria con familiares.", "Home", "Alta"),
        CommunicationCategory(5, "Trabajo", "Reuniones y coordinación laboral.", "Work", "Media")
    )

    val accessibilityStatus = listOf(
        AccessInfo(1, "Texto a Voz", "Activo", "Motor de síntesis Google configurado."),
        AccessInfo(2, "Vibración Háptica", "Activo", "Alertas táctiles para notificaciones."),
        AccessInfo(3, "Subtítulos en tiempo real", "Inactivo", "Requiere conexión a internet."),
        AccessInfo(4, "Flash de Notificación", "Activo", "Visualización de alertas mediante luz."),
        AccessInfo(5, "Interfaz Simplificada", "Activo", "Diseño de alto contraste habilitado.")
    )
}

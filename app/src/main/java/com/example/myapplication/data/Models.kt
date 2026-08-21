package com.example.myapplication.data

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String
)

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
    // Requerimiento Semana 2: Lista de 5 usuarios registrados
    val registeredUsers = listOf(
        User(1, "admin@test.com", "admin123", "Administrador"),
        User(2, "usuario1@test.com", "pass123", "Juan Pérez"),
        User(3, "ayuda@accesibilidad.org", "accesible", "Soporte"),
        User(4, "estudiante@duoc.cl", "duoc2026", "Alumno"),
        User(5, "maria@correo.com", "maria456", "María González")
    )

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

package cl.duoc.rulloa.accesiplus.data.model

/**
 * Conversión entre modelos y el Map que entrega/recibe Realtime Database.
 * Realtime Database devuelve los números como Long o Double, por eso se usa Number.
 */

fun UserProfile.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "email" to email,
    "role" to role,
    "gender" to gender,
    "createdAt" to createdAt
)

fun userProfileFromMap(map: Map<*, *>?): UserProfile? {
    if (map == null) return null
    return UserProfile(
        name = map["name"] as? String ?: "",
        email = map["email"] as? String ?: "",
        role = map["role"] as? String ?: "Usuario Final",
        gender = map["gender"] as? String ?: "Otro",
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
    )
}

/** El id no se guarda dentro del nodo: es la clave del hijo. */
fun Phrase.toMap(): Map<String, Any?> = mapOf(
    "text" to text,
    "category" to category,
    "favorite" to favorite,
    "uses" to uses,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

fun phraseFromMap(id: String, map: Map<*, *>?): Phrase? {
    if (map == null) return null
    val text = map["text"] as? String ?: return null
    return Phrase(
        id = id,
        text = text,
        category = map["category"] as? String ?: Phrase.CATEGORIA_PROPIA,
        favorite = map["favorite"] as? Boolean ?: false,
        uses = (map["uses"] as? Number)?.toLong() ?: 0L,
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
        updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L
    )
}

fun Device.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "type" to type,
    "notes" to notes,
    "lat" to lat,
    "lon" to lon,
    "locationAt" to locationAt,
    "createdAt" to createdAt
)

fun deviceFromMap(id: String, map: Map<*, *>?): Device? {
    if (map == null) return null
    val name = map["name"] as? String ?: return null
    return Device(
        id = id,
        name = name,
        type = map["type"] as? String ?: Device.TIPOS.last(),
        notes = map["notes"] as? String ?: "",
        lat = (map["lat"] as? Number)?.toDouble(),
        lon = (map["lon"] as? Number)?.toDouble(),
        locationAt = (map["locationAt"] as? Number)?.toLong(),
        createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
    )
}

/**
 * [marcaTiempo] permite que el repositorio envíe ServerValue.TIMESTAMP (la hora la pone
 * el servidor) sin que este archivo dependa de Firebase y siga probándose con JUnit puro.
 * Los enum se guardan por nombre ("ESCRIBIR"), que es lo que validan las reglas.
 */
fun RegistroHistorial.toMap(marcaTiempo: Any = timestamp): Map<String, Any?> = mapOf(
    "tipo" to tipo.name,
    "texto" to texto,
    "origen" to origen?.name,
    "timestamp" to marcaTiempo
)

/** Descarta registros sin texto o con un tipo desconocido (por ejemplo, de una versión futura). */
fun registroHistorialFromMap(id: String, map: Map<*, *>?): RegistroHistorial? {
    if (map == null) return null
    val texto = map["texto"] as? String ?: return null
    val tipo = TipoHistorial.entries.firstOrNull { it.name == map["tipo"] } ?: return null
    return RegistroHistorial(
        id = id,
        tipo = tipo,
        texto = texto,
        origen = OrigenHistorial.entries.firstOrNull { it.name == map["origen"] },
        timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
    )
}

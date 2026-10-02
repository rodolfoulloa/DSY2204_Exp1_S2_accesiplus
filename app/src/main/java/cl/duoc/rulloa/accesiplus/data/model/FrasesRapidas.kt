package cl.duoc.rulloa.accesiplus.data.model

/**
 * Frases rápidas por categoría. Viven en el código (no en la base de datos):
 * son iguales para todos los usuarios y así no existe ningún nodo compartido.
 */
data class CategoriaFrases(
    val nombre: String,
    val descripcion: String,
    val frases: List<String>
)

object FrasesRapidas {
    val categorias = listOf(
        CategoriaFrases(
            "Salud", "Consultorio, farmacia y urgencias",
            listOf(
                "Soy una persona sorda. Por favor, escríbame lo que me dice.",
                "Necesito una hora con el médico.",
                "Me duele aquí.",
                "Soy alérgico a este medicamento.",
                "Necesito ayuda urgente. Llame a una ambulancia, por favor."
            )
        ),
        CategoriaFrases(
            "Compras", "Supermercado, feria y tiendas",
            listOf(
                "¿Cuánto cuesta esto?",
                "¿Tiene esto en otra talla?",
                "¿Puedo pagar con tarjeta?",
                "Necesito la boleta, por favor.",
                "¿Me puede mostrar el precio en la pantalla?"
            )
        ),
        CategoriaFrases(
            "Transporte", "Micro, metro y taxi",
            listOf(
                "¿Este bus pasa por esta dirección?",
                "Por favor, avíseme cuando lleguemos a mi parada.",
                "¿Dónde queda la estación de metro más cercana?",
                "Lléveme a esta dirección, por favor.",
                "No escucho los anuncios. ¿Me puede indicar con señas?"
            )
        ),
        CategoriaFrases(
            "Hogar", "Familia y visitas",
            listOf(
                "Hay alguien en la puerta.",
                "Por favor, mírame cuando me hables.",
                "La comida está lista.",
                "¿Me ayudas con esto?",
                "Te quiero mucho."
            )
        ),
        CategoriaFrases(
            "Trabajo", "Reuniones y compañeros",
            listOf(
                "Por favor, escríbame las instrucciones.",
                "¿Podemos usar el chat durante la reunión?",
                "Hable más lento y de frente, por favor.",
                "Ya terminé esta tarea.",
                "¿Me puede enviar el resumen por correo?"
            )
        )
    )

    val nombres: List<String> = categorias.map { it.nombre }

    fun de(categoria: String): List<String> =
        categorias.firstOrNull { it.nombre == categoria }?.frases.orEmpty()
}

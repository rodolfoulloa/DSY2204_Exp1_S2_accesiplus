package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.Phrase
import java.text.Normalizer

/** Filtros de frases usados por Escribir y Hablar (lógica pura, probada con JUnit). */
object FiltrosFrases {

    /** Quita tildes y mayúsculas: "Médico" coincide con "medico". */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .trim()

    fun porCategoria(frases: List<Phrase>, categoria: String?): List<Phrase> =
        if (categoria.isNullOrBlank()) frases else frases.filter { it.category == categoria }

    fun porTexto(frases: List<Phrase>, busqueda: String): List<Phrase> {
        val q = normalizar(busqueda)
        if (q.isEmpty()) return frases
        return frases.filter { normalizar(it.text).contains(q) }
    }

    fun favoritas(frases: List<Phrase>): List<Phrase> = frases.filter { it.favorite }

    /** Última favorita modificada: la muestra el widget "Frase rápida". */
    fun ultimaFavorita(frases: List<Phrase>): Phrase? =
        favoritas(frases).maxByOrNull { maxOf(it.updatedAt, it.createdAt) }

    /** Evita guardar dos veces la misma frase en la misma categoría. */
    fun existe(frases: List<Phrase>, texto: String, categoria: String): Boolean =
        frases.any { it.category == categoria && normalizar(it.text) == normalizar(texto) }
}

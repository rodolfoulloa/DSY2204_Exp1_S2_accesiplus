package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.Phrase

/** Orden de frases por uso (desarrollada con TDD: ver OrdenFrasesTest). */
object OrdenFrases {

    /** Más usos primero; si empatan, favoritas primero; luego la modificada más recientemente. */
    private val porUso: Comparator<Phrase> =
        compareByDescending<Phrase> { it.uses }
            .thenByDescending { it.favorite }
            .thenByDescending { maxOf(it.updatedAt, it.createdAt) }

    /** Devuelve una lista nueva ordenada (sortedWith no modifica la original). */
    fun ordenarPorUso(frases: List<Phrase>): List<Phrase> = frases.sortedWith(porUso)

    fun masUsadas(frases: List<Phrase>, n: Int): List<Phrase> = ordenarPorUso(frases).take(n)
}

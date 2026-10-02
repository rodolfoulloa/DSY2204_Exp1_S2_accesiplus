package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.Phrase

/** Orden de frases por uso (desarrollada con TDD: ver OrdenFrasesTest). */
object OrdenFrases {

    // TDD paso 1 (rojo): firma mínima para que las pruebas compilen; todavía no ordena.
    fun ordenarPorUso(frases: List<Phrase>): List<Phrase> = frases

    fun masUsadas(frases: List<Phrase>, n: Int): List<Phrase> = frases
}

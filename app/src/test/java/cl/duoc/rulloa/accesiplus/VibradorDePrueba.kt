package cl.duoc.rulloa.accesiplus

import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.haptica.PatronVibracion
import cl.duoc.rulloa.accesiplus.data.haptica.SalidaVibracion

/**
 * Haptica real con una salida falsa: en vez de vibrar, anota qué patrón se habría sentido.
 * [activa] simula el interruptor "Vibración" del perfil.
 */
class VibradorDePrueba(var activa: Boolean = true) {
    val patrones = mutableListOf<PatronVibracion>()

    val haptica = Haptica(
        salida = SalidaVibracion { tiempos -> patrones += PatronVibracion.entries.first { it.tiempos.contentEquals(tiempos) } },
        activa = { activa }
    )
}

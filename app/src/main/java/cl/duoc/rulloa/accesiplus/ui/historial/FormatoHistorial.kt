package cl.duoc.rulloa.accesiplus.ui.historial

import cl.duoc.rulloa.accesiplus.domain.FiltrosHistorial
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Fechas legibles en español de Chile para el historial ("Hoy", "Ayer", "lunes 6 de octubre de 2026"). */
object FormatoHistorial {
    private val ES_CL: Locale = Locale.forLanguageTag("es-CL")
    private const val DIA_MS = 24 * 60 * 60 * 1000L

    /** [dia] es la medianoche que entrega FiltrosHistorial.agruparPorDia. */
    fun etiquetaDia(dia: Long, ahora: Long = System.currentTimeMillis(), zona: TimeZone = TimeZone.getDefault()): String {
        val hoy = FiltrosHistorial.inicioDelDia(ahora, zona)
        // Se compara con el inicio del día anterior calculado con Calendar (respeta cambios de horario)
        val ayer = FiltrosHistorial.inicioDelDia(hoy - DIA_MS / 2, zona)
        return when (dia) {
            hoy -> "Hoy"
            ayer -> "Ayer"
            else -> formato("EEEE d 'de' MMMM 'de' yyyy", zona).format(Date(dia))
                .replaceFirstChar { it.titlecase(ES_CL) }
        }
    }

    fun hora(instante: Long, zona: TimeZone = TimeZone.getDefault()): String =
        formato("HH:mm", zona).format(Date(instante))

    // SimpleDateFormat no es seguro entre hilos: se crea uno por llamada
    private fun formato(patron: String, zona: TimeZone) =
        SimpleDateFormat(patron, ES_CL).apply { timeZone = zona }
}

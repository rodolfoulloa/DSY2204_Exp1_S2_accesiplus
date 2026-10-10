package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import java.util.Calendar
import java.util.TimeZone

/**
 * Lógica pura del historial (probada con JUnit). Se usa Calendar y no java.time
 * porque java.time no existe en Android 7 (minSdk 24) sin agregar desugaring.
 */
object FiltrosHistorial {
    const val LARGO_MAX_TEXTO = 2000

    /** Texto listo para guardar: sin espacios sobrantes y recortado al máximo; null si está vacío. */
    fun prepararTexto(texto: String): String? =
        texto.trim().take(LARGO_MAX_TEXTO).takeIf { it.isNotEmpty() }

    /** null = "Todos". */
    fun porTipo(registros: List<RegistroHistorial>, tipo: TipoHistorial?): List<RegistroHistorial> =
        if (tipo == null) registros else registros.filter { it.tipo == tipo }

    fun ordenar(registros: List<RegistroHistorial>): List<RegistroHistorial> =
        registros.sortedByDescending { it.timestamp }

    /** Cantidad de registros de cada tipo, para mostrarla en los chips de filtro. */
    fun conteoPorTipo(registros: List<RegistroHistorial>): Map<TipoHistorial, Int> =
        registros.groupingBy { it.tipo }.eachCount()

    /**
     * Agrupa por día (clave = medianoche de ese día en [zona]). Al agrupar una lista
     * ordenada, groupBy conserva el orden: primero hoy y dentro de cada día lo más nuevo.
     */
    fun agruparPorDia(
        registros: List<RegistroHistorial>,
        zona: TimeZone = TimeZone.getDefault()
    ): Map<Long, List<RegistroHistorial>> =
        ordenar(registros).groupBy { inicioDelDia(it.timestamp, zona) }

    fun inicioDelDia(instante: Long, zona: TimeZone = TimeZone.getDefault()): Long =
        Calendar.getInstance(zona).apply {
            timeInMillis = instante
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}

/**
 * Evita guardar dos veces lo mismo por un doble disparo (doble toque, recomposición,
 * evento repetido del reconocedor): ignora la misma clave dentro de [ventanaMs].
 */
class FiltroDuplicados(
    private val ventanaMs: Long = VENTANA_MS,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    private var ultimaClave: String? = null
    private var ultimoInstante = 0L

    /** true si es repetido; si no lo es, lo recuerda como el último aceptado. */
    @Synchronized
    fun esRepetido(tipo: TipoHistorial, texto: String): Boolean {
        val clave = "${tipo.name}|${FiltrosFrases.normalizar(texto)}"
        val ahora = reloj()
        if (clave == ultimaClave && ahora - ultimoInstante < ventanaMs) return true
        ultimaClave = clave
        ultimoInstante = ahora
        return false
    }

    companion object {
        const val VENTANA_MS = 2_000L
    }
}

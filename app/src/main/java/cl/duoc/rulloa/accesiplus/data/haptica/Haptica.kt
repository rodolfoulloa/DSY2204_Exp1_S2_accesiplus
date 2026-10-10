package cl.duoc.rulloa.accesiplus.data.haptica

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

/**
 * Patrones de vibración: [tiempos] alterna espera y vibración en milisegundos
 * (el formato que usan Vibrator.vibrate y VibrationEffect.createWaveform).
 * Se distinguen por ritmo y duración, no solo por intensidad: se sienten distintos
 * aunque el teléfono no permita controlar la fuerza de la vibración.
 */
enum class PatronVibracion(val tiempos: LongArray) {
    /** Un toque corto: la acción terminó bien. */
    EXITO(longArrayOf(0, 60)),

    /** Dos toques cortos: algo quedó guardado. */
    GUARDADO(longArrayOf(0, 60, 100, 60)),

    /** Una vibración larga: hay un problema que revisar en pantalla. */
    ERROR(longArrayOf(0, 450))
}

/** Momentos de la app que producen vibración (para usuarios sordos reemplaza al sonido de aviso). */
enum class EventoHaptico {
    ESCUCHA_TERMINADA,
    ERROR_RECONOCEDOR,
    VOZ_TERMINADA,
    ERROR_VOZ,
    FRASE_GUARDADA,
    DISPOSITIVO_GUARDADO,
    REGISTRO_RECUPERADO,
    ERROR_GUARDADO,
    ERROR_LOGIN,
    VIBRACION_ACTIVADA
}

/** Lo que realmente hace vibrar el teléfono. Es una interfaz para poder probar Haptica sin Android. */
fun interface SalidaVibracion {
    fun vibrar(tiempos: LongArray)
}

/**
 * Retroalimentación háptica. Respeta el interruptor "Vibración" del perfil ([activa])
 * y no hace nada si el teléfono no tiene motor de vibración.
 */
class Haptica(
    private val salida: SalidaVibracion?,
    private val activa: () -> Boolean
) {

    /** Constructor de la app: usa el Vibrator del sistema y la preferencia guardada. */
    constructor(context: Context, activa: () -> Boolean) : this(salidaAndroid(context), activa)

    /** @return true si vibró (sirve para las pruebas y para no hacer nada con el interruptor apagado). */
    fun avisar(evento: EventoHaptico): Boolean = disparar(patronPara(evento))

    fun disparar(patron: PatronVibracion): Boolean {
        val s = salida ?: return false
        if (!activa()) return false
        s.vibrar(patron.tiempos)
        return true
    }

    companion object {
        /** Selección del patrón según el evento: un solo lugar decide cómo se siente cada cosa. */
        fun patronPara(evento: EventoHaptico): PatronVibracion = when (evento) {
            EventoHaptico.ESCUCHA_TERMINADA,
            EventoHaptico.VOZ_TERMINADA,
            EventoHaptico.VIBRACION_ACTIVADA -> PatronVibracion.EXITO

            EventoHaptico.FRASE_GUARDADA,
            EventoHaptico.DISPOSITIVO_GUARDADO,
            EventoHaptico.REGISTRO_RECUPERADO -> PatronVibracion.GUARDADO

            EventoHaptico.ERROR_RECONOCEDOR,
            EventoHaptico.ERROR_VOZ,
            EventoHaptico.ERROR_GUARDADO,
            EventoHaptico.ERROR_LOGIN -> PatronVibracion.ERROR
        }

        /** Sin vibración (pruebas o vistas previas). */
        val NINGUNA = Haptica(salida = null) { false }

        /**
         * Android 12 (API 31) reemplazó Vibrator por VibratorManager; antes se usa
         * getSystemService<Vibrator>() de core-ktx, que evita el cast manual.
         */
        fun obtenerVibrator(context: Context): Vibrator? {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService<VibratorManager>()?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService<Vibrator>()
            }
            return vibrator?.takeIf { it.hasVibrator() }
        }

        private fun salidaAndroid(context: Context): SalidaVibracion? {
            val vibrator = obtenerVibrator(context.applicationContext) ?: return null
            return SalidaVibracion { tiempos ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // -1 = no repetir el patrón
                    vibrator.vibrate(VibrationEffect.createWaveform(tiempos, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(tiempos, -1)
                }
            }
        }
    }
}

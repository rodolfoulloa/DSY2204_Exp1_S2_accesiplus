package cl.duoc.rulloa.accesiplus.data.tts

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Estado del motor de texto a voz después de onInit (riesgo de la Exp. 2: inicialización asíncrona). */
enum class EstadoTts {
    INICIANDO,
    LISTO,

    /** Habla, pero no encontró voz en español: usa el idioma por defecto del motor. */
    SIN_ESPANOL,

    /** El dispositivo no tiene ningún motor TTS instalado (o falló al iniciar). */
    SIN_MOTOR
}

enum class FinVoz { TERMINADA, ERROR }

interface Voz {
    val estado: StateFlow<EstadoTts>

    /** true mientras el motor está diciendo una frase (UtteranceProgressListener). */
    val hablando: StateFlow<Boolean>

    /** Evento por cada frase que termina sola o con error (no cuando el usuario la detiene). */
    val finalizaciones: SharedFlow<FinVoz>
    fun hablar(texto: String): Boolean
    fun detener()
}

/**
 * Envoltorio de TextToSpeech con ciclo de vida de la aplicación (una sola instancia).
 * Expone el resultado de onInit como StateFlow para que la UI muestre si se puede hablar.
 */
class TtsService(context: Context) : Voz, TextToSpeech.OnInitListener {

    private val _estado = MutableStateFlow(EstadoTts.INICIANDO)
    override val estado: StateFlow<EstadoTts> = _estado.asStateFlow()

    private val _hablando = MutableStateFlow(false)
    override val hablando: StateFlow<Boolean> = _hablando.asStateFlow()

    // Con búfer: tryEmit nunca bloquea el hilo del motor de voz
    private val _finalizaciones = MutableSharedFlow<FinVoz>(extraBufferCapacity = 8)
    override val finalizaciones: SharedFlow<FinVoz> = _finalizaciones.asSharedFlow()

    /** Los callbacks llegan en un hilo del motor de voz: los Flow de corrutinas son seguros entre hilos. */
    private val progreso = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) { _hablando.value = true }

        override fun onDone(utteranceId: String?) {
            _hablando.value = false
            _finalizaciones.tryEmit(FinVoz.TERMINADA)
        }

        // Detenida por el usuario (cerrar la pantalla grande): no es un éxito ni un error
        override fun onStop(utteranceId: String?, interrupted: Boolean) { _hablando.value = false }

        @Deprecated("Requerido por la clase base en API 21+")
        override fun onError(utteranceId: String?) = fallo()
        override fun onError(utteranceId: String?, errorCode: Int) = fallo()

        private fun fallo() {
            _hablando.value = false
            _finalizaciones.tryEmit(FinVoz.ERROR)
        }
    }

    private val tts: TextToSpeech? = try {
        TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
        Log.e(TAG, "No se pudo crear TextToSpeech", e)
        _estado.value = EstadoTts.SIN_MOTOR
        null
    }

    override fun onInit(status: Int) {
        val motor = tts
        if (status != TextToSpeech.SUCCESS || motor == null) {
            _estado.value = EstadoTts.SIN_MOTOR
            return
        }
        motor.setOnUtteranceProgressListener(progreso)
        // Prueba español de Chile, luego latino y luego cualquier español
        val idiomas = listOf("es-CL", "es-419", "es-US", "es-ES", "es").map(Locale::forLanguageTag)
        val elegido = idiomas.firstOrNull { loc ->
            motor.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE
        }
        _estado.value = if (elegido != null) {
            motor.language = elegido
            EstadoTts.LISTO
        } else {
            EstadoTts.SIN_ESPANOL
        }
    }

    override fun hablar(texto: String): Boolean {
        val motor = tts ?: return false
        if (texto.isBlank()) return false
        if (_estado.value != EstadoTts.LISTO && _estado.value != EstadoTts.SIN_ESPANOL) return false
        return motor.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "accesiplus-${texto.hashCode()}") ==
            TextToSpeech.SUCCESS
    }

    override fun detener() {
        tts?.stop()
        _hablando.value = false
    }

    companion object {
        private const val TAG = "TtsService"

        /** Abre la instalación de voces del sistema (si el fabricante no trae motor). */
        fun intentInstalarVoces(): Intent =
            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

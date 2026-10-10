package cl.duoc.rulloa.accesiplus.ui.escribir

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Voz a texto con SpeechRecognizer. El estado se expone como estado de Compose
 * para que la pantalla muestre "Escuchando…", el texto parcial y los errores.
 */
class ReconocedorVoz(private val context: Context) {

    val disponible: Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    var escuchando by mutableStateOf(false)
        private set
    /** Entre el fin del habla y el resultado final: el servicio está convirtiendo la voz en texto. */
    var procesando by mutableStateOf(false)
        private set
    var parcial by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var reconocedor: SpeechRecognizer? = null

    fun iniciar(onResultado: (String) -> Unit) {
        if (!disponible) {
            error = "Este teléfono no tiene reconocimiento de voz. Escribe con el teclado."
            return
        }
        error = null
        parcial = ""
        procesando = false
        val sr = reconocedor ?: SpeechRecognizer.createSpeechRecognizer(context).also { reconocedor = it }
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { escuchando = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                escuchando = false
                procesando = true
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onPartialResults(partialResults: Bundle?) {
                parcial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
            }

            override fun onResults(results: Bundle?) {
                escuchando = false
                procesando = false
                val texto = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                parcial = ""
                if (!texto.isNullOrBlank()) onResultado(texto)
            }

            override fun onError(codigo: Int) {
                escuchando = false
                procesando = false
                error = mensajeError(codigo)
            }
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CL")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        escuchando = true
        sr.startListening(intent)
    }

    fun detener() {
        reconocedor?.stopListening()
        // Al detener, el servicio igual entrega el resultado (onResults) o un error (onError)
        if (escuchando) procesando = true
        escuchando = false
    }

    fun liberar() {
        reconocedor?.destroy()
        reconocedor = null
        procesando = false
    }

    fun limpiarError() { error = null }

    companion object {
        fun mensajeError(codigo: Int): String = when (codigo) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No entendí lo que se dijo. Intenta otra vez."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se escuchó ninguna voz. Acerca el teléfono e intenta otra vez."
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Sin conexión: el reconocimiento de voz necesita internet."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta el permiso del micrófono."
            SpeechRecognizer.ERROR_AUDIO -> "No se pudo usar el micrófono."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocimiento está ocupado. Espera un momento."
            else -> "No se pudo reconocer la voz. Puedes escribir con el teclado."
        }
    }
}

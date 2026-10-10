package cl.duoc.rulloa.accesiplus.ui.phrases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.accesiplus.data.haptica.EventoHaptico
import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.model.OrigenHistorial
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.HistorialRepository
import cl.duoc.rulloa.accesiplus.data.repository.PhraseRepository
import cl.duoc.rulloa.accesiplus.data.tts.EstadoTts
import cl.duoc.rulloa.accesiplus.data.tts.FinVoz
import cl.duoc.rulloa.accesiplus.data.tts.Voz
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
import cl.duoc.rulloa.accesiplus.domain.FiltroDuplicados
import cl.duoc.rulloa.accesiplus.domain.FiltrosFrases
import cl.duoc.rulloa.accesiplus.domain.FiltrosHistorial
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhraseUiState(
    val error: String? = null,
    val mensaje: String? = null,
    /** Frase que se está mostrando en grande en pantalla. */
    val enPantalla: String? = null,
    /** true después de una transcripción final o de decir algo en voz alta: se ofrece "Volver al menú". */
    val completada: Boolean = false
)

/**
 * CRUD de frases para Escribir y Hablar, más la reproducción por voz.
 * Además guarda automáticamente cada solicitud y su respuesta en el historial.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PhraseViewModel(
    private val auth: AuthRepository,
    private val repo: PhraseRepository,
    private val voz: Voz,
    private val historial: HistorialRepository,
    private val haptica: Haptica,
    private val duplicados: FiltroDuplicados = FiltroDuplicados()
) : ViewModel() {

    private val _ui = MutableStateFlow(PhraseUiState())
    val ui: StateFlow<PhraseUiState> = _ui.asStateFlow()

    val estadoVoz: StateFlow<EstadoTts> = voz.estado
    val hablando: StateFlow<Boolean> = voz.hablando

    init {
        // Hablar: vibra cuando la frase termina de decirse o si el motor de voz falla a la mitad
        viewModelScope.launch {
            voz.finalizaciones.collect { fin ->
                haptica.avisar(if (fin == FinVoz.TERMINADA) EventoHaptico.VOZ_TERMINADA else EventoHaptico.ERROR_VOZ)
            }
        }
    }

    val frases: StateFlow<List<Phrase>> = auth.estadoSesion()
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repo.observarFrases(uid) }
        .catch { e -> _ui.update { it.copy(error = ErroresFirebase.mensaje(e)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deCategoria(lista: List<Phrase>, categoria: String) = FiltrosFrases.porCategoria(lista, categoria)

    fun crear(texto: String, categoria: String, favorita: Boolean = false) {
        val uid = auth.uidActual ?: return
        if (!Validaciones.isPhraseValid(texto)) {
            _ui.update { it.copy(error = "La frase debe tener entre 1 y ${Validaciones.LARGO_MAX_FRASE} letras.") }
            haptica.avisar(EventoHaptico.ERROR_GUARDADO)
            return
        }
        if (FiltrosFrases.existe(frases.value, texto, categoria)) {
            _ui.update { it.copy(mensaje = "Esa frase ya está guardada.") }
            return
        }
        lanzar("Frase guardada.", EventoHaptico.FRASE_GUARDADA) {
            repo.crear(uid, Phrase(text = texto.trim(), category = categoria, favorite = favorita)).map { }
        }
    }

    fun editar(frase: Phrase, nuevoTexto: String) {
        val uid = auth.uidActual ?: return
        if (!Validaciones.isPhraseValid(nuevoTexto)) {
            _ui.update { it.copy(error = "La frase no puede quedar vacía.") }
            haptica.avisar(EventoHaptico.ERROR_GUARDADO)
            return
        }
        lanzar("Frase actualizada.", EventoHaptico.FRASE_GUARDADA) { repo.actualizar(uid, frase.copy(text = nuevoTexto.trim())) }
    }

    fun eliminar(frase: Phrase) {
        val uid = auth.uidActual ?: return
        lanzar("Frase eliminada.") { repo.eliminar(uid, frase.id) }
    }

    fun alternarFavorita(frase: Phrase) {
        val uid = auth.uidActual ?: return
        val msg = if (frase.favorite) "Quitada de favoritas." else "Marcada como favorita."
        // Marcarla como favorita es guardarla; quitarla no se confirma con vibración
        val evento = if (frase.favorite) null else EventoHaptico.FRASE_GUARDADA
        lanzar(msg, evento) { repo.actualizar(uid, frase.copy(favorite = !frase.favorite)) }
    }

    /**
     * Lee en voz alta y muestra la frase en grande. Si es una frase guardada, suma un uso.
     * Siempre queda en el historial: aunque no haya voz, la respuesta se mostró en pantalla.
     */
    fun hablar(
        texto: String,
        frase: Phrase? = null,
        origen: OrigenHistorial = if (frase != null) OrigenHistorial.FRASE_GUARDADA else OrigenHistorial.TEXTO_LIBRE
    ) {
        // Un intento nuevo limpia el error anterior: la guía no queda pegada en "Hubo un problema"
        _ui.update { it.copy(enPantalla = texto, completada = texto.isNotBlank(), error = null) }
        if (!voz.hablar(texto)) {
            _ui.update { it.copy(error = "La voz no está disponible. La frase se muestra en pantalla.") }
            haptica.avisar(EventoHaptico.ERROR_VOZ)
        }
        val uid = auth.uidActual
        if (frase != null && frase.id.isNotBlank() && uid != null) {
            viewModelScope.launch { repo.registrarUso(uid, frase.id) }
        }
        registrarEnHistorial(TipoHistorial.HABLAR, texto, origen)
    }

    /** Escribir: se llama solo con el resultado final del reconocedor de voz (no con los parciales). */
    fun registrarEscrito(texto: String) {
        if (texto.isNotBlank()) {
            _ui.update { it.copy(completada = true) }
            // Terminó de escuchar con resultado: el usuario sabe que ya puede mirar la pantalla
            haptica.avisar(EventoHaptico.ESCUCHA_TERMINADA)
        }
        registrarEnHistorial(TipoHistorial.ESCRIBIR, texto, origen = null)
    }

    /** Escribir: el reconocedor de voz informó un error (sin red, sin permiso, no entendió…). */
    fun avisarErrorReconocedor() {
        haptica.avisar(EventoHaptico.ERROR_RECONOCEDOR)
    }

    private fun registrarEnHistorial(tipo: TipoHistorial, texto: String, origen: OrigenHistorial?) {
        val uid = auth.uidActual ?: return
        val limpio = FiltrosHistorial.prepararTexto(texto) ?: return
        if (duplicados.esRepetido(tipo, limpio)) return
        val registro = RegistroHistorial(tipo = tipo, texto = limpio, origen = origen)
        // setValue deja la escritura en la caché local al instante: aunque el usuario salga
        // de la pantalla (y se cancele esta corrutina) o no haya red, el registro no se pierde.
        viewModelScope.launch {
            // El éxito ya lo confirmó la vibración de fin de escucha o de voz: solo se avisa el error
            historial.agregar(uid, registro).onFailure { e ->
                _ui.update { it.copy(error = "No se pudo guardar en el historial. ${ErroresFirebase.mensaje(e)}") }
                haptica.avisar(EventoHaptico.ERROR_GUARDADO)
            }
        }
    }

    fun mostrar(texto: String) = _ui.update { it.copy(enPantalla = texto) }
    fun ocultarPantalla() {
        voz.detener()
        _ui.update { it.copy(enPantalla = null) }
    }

    fun limpiarMensajes() = _ui.update { it.copy(error = null, mensaje = null) }

    /** @param eventoExito vibración al terminar bien (null = sin vibración); si falla, siempre vibra como error. */
    private fun lanzar(exito: String, eventoExito: EventoHaptico? = null, accion: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            val res = accion()
            _ui.update {
                it.copy(
                    error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
                    mensaje = if (res.isSuccess) exito else null
                )
            }
            when {
                res.isFailure -> haptica.avisar(EventoHaptico.ERROR_GUARDADO)
                eventoExito != null -> haptica.avisar(eventoExito)
            }
        }
    }
}

package cl.duoc.rulloa.accesiplus.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.accesiplus.data.haptica.EventoHaptico
import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.HistorialRepository
import cl.duoc.rulloa.accesiplus.data.tts.Voz
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
import cl.duoc.rulloa.accesiplus.domain.FiltrosHistorial
import java.util.TimeZone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    /** false hasta que llega la primera lectura (evita mostrar "vacío" mientras carga). */
    val cargado: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    /** Último registro borrado: la pantalla ofrece "Deshacer" en el Snackbar. */
    val eliminado: RegistroHistorial? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModel(
    private val auth: AuthRepository,
    private val repo: HistorialRepository,
    private val voz: Voz,
    private val haptica: Haptica,
    private val zona: TimeZone = TimeZone.getDefault()
) : ViewModel() {

    private val _ui = MutableStateFlow(HistorialUiState())
    val ui: StateFlow<HistorialUiState> = _ui.asStateFlow()

    /** Filtro de los chips: null = Todos. */
    private val _filtro = MutableStateFlow<TipoHistorial?>(null)
    val filtro: StateFlow<TipoHistorial?> = _filtro.asStateFlow()

    val registros: StateFlow<List<RegistroHistorial>> = auth.estadoSesion()
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repo.observar(uid) }
        .onEach { _ui.update { it.copy(cargado = true) } }
        .catch { e -> _ui.update { it.copy(cargado = true, error = ErroresFirebase.mensaje(e)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Registros del filtro elegido, agrupados por día (clave = medianoche) y del más nuevo al más antiguo. */
    val porDia: StateFlow<Map<Long, List<RegistroHistorial>>> = combine(registros, _filtro) { lista, tipo ->
        FiltrosHistorial.agruparPorDia(FiltrosHistorial.porTipo(lista, tipo), zona)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Cantidad por tipo para mostrarla en cada chip ("Escribir (3)"). */
    val conteo: StateFlow<Map<TipoHistorial, Int>> = registros
        .map(FiltrosHistorial::conteoPorTipo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun filtrar(tipo: TipoHistorial?) = _filtro.update { tipo }

    /** Repetir no crea un registro nuevo: solo vuelve a decir algo que ya está en el historial. */
    fun repetir(registro: RegistroHistorial) {
        if (!voz.hablar(registro.texto)) {
            _ui.update { it.copy(error = "La voz no está disponible en este momento.") }
            haptica.avisar(EventoHaptico.ERROR_VOZ)
        }
    }

    fun eliminar(registro: RegistroHistorial) {
        val uid = auth.uidActual ?: return
        viewModelScope.launch {
            val res = repo.eliminar(uid, registro.id)
            _ui.update {
                it.copy(
                    error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
                    eliminado = if (res.isSuccess) registro else null
                )
            }
            if (res.isFailure) haptica.avisar(EventoHaptico.ERROR_GUARDADO)
        }
    }

    fun deshacer() {
        val registro = _ui.value.eliminado ?: return
        val uid = auth.uidActual ?: return
        _ui.update { it.copy(eliminado = null) }
        viewModelScope.launch {
            val res = repo.restaurar(uid, registro)
            _ui.update {
                it.copy(
                    error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
                    mensaje = if (res.isSuccess) "Registro recuperado." else null
                )
            }
            // Deshacer vuelve a guardar el registro: doble toque como cualquier guardado
            haptica.avisar(if (res.isSuccess) EventoHaptico.REGISTRO_RECUPERADO else EventoHaptico.ERROR_GUARDADO)
        }
    }

    fun borrarTodo() {
        val uid = auth.uidActual ?: return
        viewModelScope.launch {
            val res = repo.borrarTodo(uid)
            _ui.update {
                it.copy(
                    error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
                    mensaje = if (res.isSuccess) "Historial borrado." else null,
                    eliminado = null
                )
            }
            if (res.isFailure) haptica.avisar(EventoHaptico.ERROR_GUARDADO)
        }
    }

    fun limpiarEliminado() = _ui.update { it.copy(eliminado = null) }
    fun limpiarMensajes() = _ui.update { it.copy(error = null, mensaje = null) }
}

package cl.duoc.rulloa.accesiplus.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.accesiplus.data.haptica.EventoHaptico
import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.location.UbicacionProvider
import cl.duoc.rulloa.accesiplus.data.model.Device
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.DeviceRepository
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
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

data class DeviceUiState(
    val error: String? = null,
    val mensaje: String? = null,
    /** id del dispositivo cuya ubicación se está obteniendo. */
    val ubicando: String? = null
)

/** CRUD de dispositivos y registro de su ubicación (BuscarDispositivo). */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceViewModel(
    private val auth: AuthRepository,
    private val repo: DeviceRepository,
    private val ubicacion: UbicacionProvider,
    private val haptica: Haptica
) : ViewModel() {

    private val _ui = MutableStateFlow(DeviceUiState())
    val ui: StateFlow<DeviceUiState> = _ui.asStateFlow()

    val dispositivos: StateFlow<List<Device>> = auth.estadoSesion()
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repo.observarDispositivos(uid) }
        .catch { e -> _ui.update { it.copy(error = ErroresFirebase.mensaje(e)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun esValido(nombre: String) = nombre.isNotBlank() && nombre.trim().length <= 60

    fun guardar(id: String?, nombre: String, tipo: String, notas: String) {
        val uid = auth.uidActual ?: return
        if (!esValido(nombre)) {
            _ui.update { it.copy(error = "Escribe un nombre para el dispositivo.") }
            haptica.avisar(EventoHaptico.ERROR_GUARDADO)
            return
        }
        viewModelScope.launch {
            val actual = dispositivos.value.firstOrNull { it.id == id }
            val res = if (actual == null) {
                repo.crear(uid, Device(name = nombre.trim(), type = tipo, notes = notas.trim())).map { }
            } else {
                repo.actualizar(uid, actual.copy(name = nombre.trim(), type = tipo, notes = notas.trim()))
            }
            terminar(res, if (actual == null) "Dispositivo agregado." else "Dispositivo actualizado.")
            haptica.avisar(if (res.isSuccess) EventoHaptico.DISPOSITIVO_GUARDADO else EventoHaptico.ERROR_GUARDADO)
        }
    }

    fun eliminar(dispositivo: Device) {
        val uid = auth.uidActual ?: return
        viewModelScope.launch { terminar(repo.eliminar(uid, dispositivo.id), "Dispositivo eliminado.") }
    }

    /** La UI llama a esta función solo después de obtener el permiso de ubicación. */
    fun registrarUbicacion(dispositivo: Device) {
        val uid = auth.uidActual ?: return
        if (_ui.value.ubicando != null) return
        _ui.update { it.copy(ubicando = dispositivo.id) }
        viewModelScope.launch {
            val res = ubicacion.ubicacionActual().mapCatching { c ->
                repo.guardarUbicacion(uid, dispositivo.id, c.lat, c.lon).getOrThrow()
            }
            _ui.update {
                it.copy(
                    ubicando = null,
                    error = res.exceptionOrNull()?.let { e ->
                        if (e is IllegalStateException) e.message else ErroresFirebase.mensaje(e)
                    },
                    mensaje = if (res.isSuccess) "Ubicación guardada para ${dispositivo.name}." else null
                )
            }
            haptica.avisar(if (res.isSuccess) EventoHaptico.DISPOSITIVO_GUARDADO else EventoHaptico.ERROR_GUARDADO)
        }
    }

    fun permisoDenegado() = _ui.update {
        it.copy(error = "Sin permiso de ubicación no se puede registrar dónde quedó el dispositivo.")
    }

    fun sinAppMapas() = _ui.update { it.copy(error = "No hay una app de mapas instalada para abrir la ubicación.") }

    fun limpiarMensajes() = _ui.update { it.copy(error = null, mensaje = null) }

    private fun terminar(res: Result<Unit>, exito: String) = _ui.update {
        it.copy(
            error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
            mensaje = if (res.isSuccess) exito else null
        )
    }
}

package cl.duoc.rulloa.accesiplus.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.accesiplus.data.haptica.EventoHaptico
import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.local.Preferencias
import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
import cl.duoc.rulloa.accesiplus.domain.Saludos
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

sealed interface EstadoSaludo {
    data object Cargando : EstadoSaludo
    data class Listo(val texto: String) : EstadoSaludo
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val auth: AuthRepository,
    private val usuarios: UserRepository,
    private val prefs: Preferencias,
    private val haptica: Haptica,
    private val esperaSaludoMs: Long = 4_000L
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    /** Interruptor "Vibración": se lee de SharedPreferences al abrir el perfil. */
    private val _vibracion = MutableStateFlow(prefs.vibracionActiva())
    val vibracion: StateFlow<Boolean> = _vibracion.asStateFlow()

    fun cambiarVibracion(activa: Boolean) {
        prefs.cambiarVibracion(activa)
        _vibracion.value = activa
        // Al activarla, un toque corto de muestra confirma que funciona
        if (activa) haptica.avisar(EventoHaptico.VIBRACION_ACTIVADA)
    }

    private val perfilRemoto = auth.estadoSesion()
        .flatMapLatest { uid -> if (uid == null) emptyFlow() else usuarios.observarPerfil(uid) }

    val perfil: StateFlow<UserProfile?> = perfilRemoto
        .catch { e -> _ui.update { it.copy(error = ErroresFirebase.mensaje(e)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Saludo del menú (paso 2). Empieza en Cargando para mostrar un marcador accesible en vez
     * de un "Hola" incompleto. Si el perfil no llega en [esperaSaludoMs] (primer inicio sin red)
     * o falla la lectura, saluda con el correo: nunca queda sin nombre.
     */
    val saludo: StateFlow<EstadoSaludo> = channelFlow {
        val respaldo = launch {
            delay(esperaSaludoMs)
            send(EstadoSaludo.Listo(Saludos.texto(Saludos.nombre(null, auth.correoActual))))
        }
        perfilRemoto
            .catch {
                respaldo.cancel()
                send(EstadoSaludo.Listo(Saludos.texto(Saludos.nombre(null, auth.correoActual))))
            }
            .collect { p ->
                respaldo.cancel()
                send(EstadoSaludo.Listo(Saludos.texto(Saludos.nombre(p, auth.correoActual))))
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoSaludo.Cargando)

    val correo: String get() = auth.correoActual.orEmpty()

    fun guardarNombre(nombre: String) {
        val uid = auth.uidActual ?: return
        if (!Validaciones.isNameValid(nombre)) {
            _ui.update { it.copy(error = "Escribe un nombre de hasta ${Validaciones.LARGO_MAX_NOMBRE} letras.") }
            return
        }
        viewModelScope.launch {
            _ui.update { ProfileUiState(cargando = true) }
            val res = usuarios.actualizarNombre(uid, nombre)
            _ui.update {
                ProfileUiState(
                    error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
                    mensaje = if (res.isSuccess) "Nombre guardado." else null
                )
            }
        }
    }

    fun cerrarSesion() = auth.cerrarSesion()

    /**
     * Elimina la cuenta: 1) confirma la contraseña, 2) borra users/{uid} mientras
     * aún hay sesión (las reglas lo exigen) y 3) borra el usuario de Firebase Auth.
     */
    fun eliminarCuenta(clave: String) {
        val uid = auth.uidActual ?: return
        if (clave.isEmpty()) {
            _ui.update { it.copy(error = "Escribe tu contraseña para confirmar.") }
            return
        }
        viewModelScope.launch {
            _ui.update { ProfileUiState(cargando = true) }
            val res = auth.reautenticar(clave)
                .mapCatching { usuarios.eliminarDatos(uid).getOrThrow() }
                .mapCatching { auth.eliminarCuenta().getOrThrow() }
            _ui.update {
                ProfileUiState(error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje))
            }
        }
    }

    fun limpiarMensajes() = _ui.update { it.copy(error = null, mensaje = null) }
}

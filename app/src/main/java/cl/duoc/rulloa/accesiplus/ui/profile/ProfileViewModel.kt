package cl.duoc.rulloa.accesiplus.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val auth: AuthRepository,
    private val usuarios: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    val perfil: StateFlow<UserProfile?> = auth.estadoSesion()
        .flatMapLatest { uid -> if (uid == null) emptyFlow() else usuarios.observarPerfil(uid) }
        .catch { e -> _ui.update { it.copy(error = ErroresFirebase.mensaje(e)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

package cl.duoc.rulloa.accesiplus.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import cl.duoc.rulloa.accesiplus.domain.ErroresFirebase
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la sesión que decide qué parte de la navegación se puede mostrar. */
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val uid: String) : EstadoSesion
}

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class AuthViewModel(
    private val auth: AuthRepository,
    private val usuarios: UserRepository,
    private val reloj: () -> Long = System::currentTimeMillis,
    // Inyectable: android.util.Patterns no existe en pruebas JVM puras (se prueba aparte con Robolectric)
    private val validarCorreo: (String) -> Boolean = Validaciones::isEmailValid
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    /** La sesión persiste entre aperturas: Firebase Auth guarda el usuario en el dispositivo. */
    val sesion: StateFlow<EstadoSesion> = auth.estadoSesion()
        .map { uid -> if (uid == null) EstadoSesion.SinSesion else EstadoSesion.Activa(uid) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    fun isEmailValid(correo: String) = validarCorreo(correo)
    fun isPasswordValid(clave: String) = Validaciones.isPasswordValid(clave)

    fun login(correo: String, clave: String) {
        if (!isEmailValid(correo) || clave.isEmpty()) {
            _ui.update { it.copy(error = "Escribe un correo válido y tu contraseña.") }
            return
        }
        ejecutar { auth.iniciarSesion(correo, clave).map { } }
    }

    fun registrar(nombre: String, correo: String, clave: String, rol: String, genero: String) {
        if (!Validaciones.isNameValid(nombre) || !isEmailValid(correo) || !isPasswordValid(clave)) {
            _ui.update { it.copy(error = "Revisa los datos del formulario.") }
            return
        }
        ejecutar {
            auth.registrar(correo, clave).mapCatching { uid ->
                // Firebase guarda el correo del token en minúsculas: la regla lo compara así
                val perfil = UserProfile(nombre.trim(), correo.trim().lowercase(), rol, genero, reloj())
                usuarios.crearPerfil(uid, perfil).getOrThrow()
            }
        }
    }

    fun recuperar(correo: String) {
        if (!isEmailValid(correo)) {
            _ui.update { it.copy(error = "Escribe un correo válido.") }
            return
        }
        ejecutar(
            exito = "Si el correo está registrado, te llegará un enlace para crear una nueva contraseña."
        ) { auth.enviarRecuperacion(correo) }
    }

    fun limpiarMensajes() = _ui.update { it.copy(error = null, mensaje = null) }

    private fun ejecutar(exito: String? = null, accion: suspend () -> Result<Unit>) {
        if (_ui.value.cargando) return
        _ui.update { AuthUiState(cargando = true) }
        viewModelScope.launch {
            val res = accion()
            _ui.update {
                AuthUiState(
                    cargando = false,
                    error = res.exceptionOrNull()?.let(ErroresFirebase::mensaje),
                    mensaje = if (res.isSuccess) exito else null
                )
            }
        }
    }
}

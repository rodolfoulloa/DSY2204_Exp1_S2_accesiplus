package cl.duoc.rulloa.accesiplus.ui.auth

import cl.duoc.rulloa.accesiplus.ReglaDispatcherPrincipal
import cl.duoc.rulloa.accesiplus.VibradorDePrueba
import cl.duoc.rulloa.accesiplus.data.haptica.PatronVibracion
import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

/** Mockito: AuthViewModel con repositorios simulados (sin Firebase). */
class AuthViewModelTest {

    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private val sesion = MutableStateFlow<String?>(null)
    private lateinit var auth: AuthRepository
    private lateinit var usuarios: UserRepository
    private lateinit var vm: AuthViewModel
    private val vibrador = VibradorDePrueba()

    // Validación simple de correo para JVM (la real usa android.util.Patterns)
    private val correoSimple: (String) -> Boolean = { it.contains("@") && it.contains(".") }

    @Before
    fun preparar() {
        auth = mock { on { estadoSesion() } doReturn sesion }
        usuarios = mock()
        vm = AuthViewModel(auth, usuarios, vibrador.haptica, reloj = { 1000L }, validarCorreo = correoSimple)
    }

    @Test
    fun `la sesion refleja el uid del repositorio`() {
        assertEquals(EstadoSesion.SinSesion, vm.sesion.value)
        sesion.value = "uid-1"
        assertEquals(EstadoSesion.Activa("uid-1"), vm.sesion.value)
        sesion.value = null
        assertEquals(EstadoSesion.SinSesion, vm.sesion.value)
    }

    @Test
    fun `login con correo invalido no llama a Firebase`() = runTest {
        vm.login("correo-malo", "123456")
        verify(auth, never()).iniciarSesion(any(), any())
        assertEquals("Escribe un correo válido y tu contraseña.", vm.ui.value.error)
        assertEquals(listOf(PatronVibracion.ERROR), vibrador.patrones)
    }

    @Test
    fun `login correcto deja la UI sin error ni carga`() = runTest {
        whenever(auth.iniciarSesion("ana@correo.cl", "secreta")).thenReturn(Result.success("uid-1"))
        vm.login("ana@correo.cl", "secreta")
        verify(auth).iniciarSesion("ana@correo.cl", "secreta")
        assertNull(vm.ui.value.error)
        assertEquals(false, vm.ui.value.cargando)
        assertTrue(vibrador.patrones.isEmpty()) // login correcto: sin vibración de error
    }

    @Test
    fun `login fallido traduce el error de Firebase`() = runTest {
        val error = mock<FirebaseAuthException> { on { errorCode } doReturn "ERROR_INVALID_CREDENTIAL" }
        whenever(auth.iniciarSesion(any(), any())).thenReturn(Result.failure(error))
        vm.login("ana@correo.cl", "mala")
        assertEquals("Correo o contraseña incorrectos.", vm.ui.value.error)
        assertEquals(listOf(PatronVibracion.ERROR), vibrador.patrones)
    }

    @Test
    fun `registro crea la cuenta y luego el perfil con correo en minusculas`() = runTest {
        whenever(auth.registrar(any(), any())).thenReturn(Result.success("uid-9"))
        whenever(usuarios.crearPerfil(any(), any())).thenReturn(Result.success(Unit))

        vm.registrar("  Ana Pérez ", "Ana@Correo.cl", "secreta", "Cuidador", "Femenino")

        verify(usuarios).crearPerfil(
            eq("uid-9"),
            argThat<UserProfile> { name == "Ana Pérez" && email == "ana@correo.cl" && role == "Cuidador" && createdAt == 1000L }
        )
        assertNull(vm.ui.value.error)
    }

    @Test
    fun `registro con datos invalidos no llama a ningun repositorio`() = runTest {
        vm.registrar("", "ana@correo.cl", "123", "Usuario Final", "Otro")
        verify(auth, never()).registrar(any(), any())
        verifyNoInteractions(usuarios)
        assertEquals("Revisa los datos del formulario.", vm.ui.value.error)
    }

    @Test
    fun `recuperar muestra un mensaje que no revela si el correo existe`() = runTest {
        whenever(auth.enviarRecuperacion("ana@correo.cl")).thenReturn(Result.success(Unit))
        vm.recuperar("ana@correo.cl")
        assertEquals(
            "Si el correo está registrado, te llegará un enlace para crear una nueva contraseña.",
            vm.ui.value.mensaje
        )
    }
}

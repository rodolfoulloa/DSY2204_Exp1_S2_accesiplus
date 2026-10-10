package cl.duoc.rulloa.accesiplus.ui.profile

import cl.duoc.rulloa.accesiplus.ReglaDispatcherPrincipal
import cl.duoc.rulloa.accesiplus.data.haptica.Haptica
import cl.duoc.rulloa.accesiplus.data.model.UserProfile
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/** Saludo del menú (paso 2): marcador mientras carga, nombre del perfil y respaldo con el correo. */
class ProfileViewModelSaludoTest {

    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private fun crear(perfil: Flow<UserProfile?>): ProfileViewModel {
        val auth: AuthRepository = mock {
            on { estadoSesion() } doReturn flowOf("u1")
            on { correoActual } doReturn "rodolfo@duoc.cl"
        }
        val usuarios: UserRepository = mock { on { observarPerfil("u1") } doReturn perfil }
        return ProfileViewModel(auth, usuarios, mock(), Haptica.NINGUNA, esperaSaludoMs = 4_000)
    }

    private fun TestScope.observar(vm: ProfileViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.saludo.collect {} }
    }

    @Test
    fun `mientras no llega el perfil se muestra el marcador y luego el nombre`() = runTest(reglaMain.dispatcher) {
        val perfil = MutableSharedFlow<UserProfile?>()
        val vm = crear(perfil)
        observar(vm)
        assertEquals(EstadoSaludo.Cargando, vm.saludo.value)
        perfil.emit(UserProfile(name = "Ana María"))
        assertEquals(EstadoSaludo.Listo("Hola, Ana"), vm.saludo.value)
    }

    @Test
    fun `si el perfil no llega a tiempo saluda con el correo`() = runTest(reglaMain.dispatcher) {
        val vm = crear(MutableSharedFlow())
        observar(vm)
        advanceTimeBy(4_001)
        assertEquals(EstadoSaludo.Listo("Hola, rodolfo"), vm.saludo.value)
    }

    @Test
    fun `si la lectura falla tambien saluda con el correo`() = runTest(reglaMain.dispatcher) {
        val vm = crear(flow { throw IllegalStateException("Permission denied") })
        observar(vm)
        assertEquals(EstadoSaludo.Listo("Hola, rodolfo"), vm.saludo.value)
    }

    @Test
    fun `perfil sin nombre usa el correo`() = runTest(reglaMain.dispatcher) {
        val vm = crear(flowOf(UserProfile(name = "")))
        observar(vm)
        assertEquals(EstadoSaludo.Listo("Hola, rodolfo"), vm.saludo.value)
    }
}

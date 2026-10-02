package cl.duoc.rulloa.accesiplus.ui.devices

import cl.duoc.rulloa.accesiplus.ReglaDispatcherPrincipal
import cl.duoc.rulloa.accesiplus.data.location.Coordenadas
import cl.duoc.rulloa.accesiplus.data.location.UbicacionProvider
import cl.duoc.rulloa.accesiplus.data.model.Device
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.DeviceRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
import org.mockito.kotlin.whenever

/** Mockito: DeviceViewModel con repositorio y ubicación simulados. */
class DeviceViewModelTest {

    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private val audifono = Device(id = "d1", name = "Audífono derecho", type = "Audífono", createdAt = 1L)
    private lateinit var auth: AuthRepository
    private lateinit var repo: DeviceRepository
    private lateinit var ubicacion: UbicacionProvider
    private lateinit var vm: DeviceViewModel

    @Before
    fun preparar() {
        auth = mock {
            on { estadoSesion() } doReturn flowOf("u1")
            on { uidActual } doReturn "u1"
        }
        repo = mock { on { observarDispositivos("u1") } doReturn flowOf(listOf(audifono)) }
        ubicacion = mock()
        vm = DeviceViewModel(auth, repo, ubicacion)
    }

    @Test
    fun `guardar sin id crea un dispositivo nuevo`() = runTest {
        whenever(repo.crear(any(), any())).thenReturn(Result.success("d2"))
        vm.guardar(null, "  Teléfono  ", "Teléfono", "en la cocina")
        verify(repo).crear(eq("u1"), argThat<Device> { name == "Teléfono" && type == "Teléfono" && notes == "en la cocina" })
        assertEquals("Dispositivo agregado.", vm.ui.value.mensaje)
    }

    @Test
    fun `guardar con id existente actualiza sin perder la ubicacion`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.dispositivos.collect {} }
        whenever(repo.actualizar(any(), any())).thenReturn(Result.success(Unit))
        vm.guardar("d1", "Audífono izquierdo", "Audífono", "")
        verify(repo).actualizar(eq("u1"), argThat<Device> { id == "d1" && name == "Audífono izquierdo" && createdAt == 1L })
        verify(repo, never()).crear(any(), any())
    }

    @Test
    fun `nombre vacio no se guarda`() = runTest {
        vm.guardar(null, "   ", "Otro", "")
        verify(repo, never()).crear(any(), any())
        assertEquals("Escribe un nombre para el dispositivo.", vm.ui.value.error)
    }

    @Test
    fun `registrar ubicacion guarda lat y lon obtenidas`() = runTest {
        whenever(ubicacion.ubicacionActual()).thenReturn(Result.success(Coordenadas(-33.4569, -70.6483)))
        whenever(repo.guardarUbicacion(any(), any(), any(), any())).thenReturn(Result.success(Unit))
        vm.registrarUbicacion(audifono)
        verify(repo).guardarUbicacion("u1", "d1", -33.4569, -70.6483)
        assertEquals("Ubicación guardada para Audífono derecho.", vm.ui.value.mensaje)
        assertNull(vm.ui.value.ubicando)
    }

    @Test
    fun `si no hay ubicacion muestra el motivo y no escribe`() = runTest {
        whenever(ubicacion.ubicacionActual()).thenReturn(
            Result.failure(IllegalStateException("No se pudo obtener la ubicación. Activa el GPS e intenta otra vez."))
        )
        vm.registrarUbicacion(audifono)
        verify(repo, never()).guardarUbicacion(any(), any(), any(), any())
        assertEquals("No se pudo obtener la ubicación. Activa el GPS e intenta otra vez.", vm.ui.value.error)
    }

    @Test
    fun `eliminar llama al repositorio`() = runTest {
        whenever(repo.eliminar(any(), any())).thenReturn(Result.success(Unit))
        vm.eliminar(audifono)
        verify(repo).eliminar("u1", "d1")
        assertEquals("Dispositivo eliminado.", vm.ui.value.mensaje)
    }

    @Test
    fun `permiso denegado muestra explicacion`() {
        vm.permisoDenegado()
        assertEquals(
            "Sin permiso de ubicación no se puede registrar dónde quedó el dispositivo.",
            vm.ui.value.error
        )
        vm.limpiarMensajes()
        assertNull(vm.ui.value.error)
    }
}

package cl.duoc.rulloa.accesiplus.ui.historial

import cl.duoc.rulloa.accesiplus.ReglaDispatcherPrincipal
import cl.duoc.rulloa.accesiplus.VibradorDePrueba
import cl.duoc.rulloa.accesiplus.data.haptica.PatronVibracion
import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.HistorialRepository
import cl.duoc.rulloa.accesiplus.data.tts.Voz
import java.util.TimeZone
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** Mockito: HistorialViewModel con repositorio y voz simulados. */
class HistorialViewModelTest {

    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private val dia = 24 * 60 * 60 * 1000L
    private val lunes = 1_759_708_800_000L // 2025-10-06 00:00 UTC
    private val registros = listOf(
        RegistroHistorial("a", TipoHistorial.ESCRIBIR, "Hola", timestamp = lunes + 1_000),
        RegistroHistorial("b", TipoHistorial.HABLAR, "Gracias", timestamp = lunes + dia),
        RegistroHistorial("c", TipoHistorial.HABLAR, "Ayuda", timestamp = lunes + 5_000)
    )
    private lateinit var auth: AuthRepository
    private lateinit var repo: HistorialRepository
    private lateinit var voz: Voz
    private lateinit var vm: HistorialViewModel
    private val vibrador = VibradorDePrueba()

    @Before
    fun preparar() {
        auth = mock {
            on { estadoSesion() } doReturn flowOf("u1")
            on { uidActual } doReturn "u1"
        }
        repo = mock { on { observar("u1") } doReturn flowOf(registros) }
        voz = mock()
        vm = HistorialViewModel(auth, repo, voz, vibrador.haptica, TimeZone.getTimeZone("UTC"))
    }

    private fun TestScope.observar() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.porDia.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.conteo.collect {} }
    }

    @Test
    fun `sin filtro agrupa por dia del mas nuevo al mas antiguo`() = runTest {
        observar()
        assertEquals(listOf(lunes + dia, lunes), vm.porDia.value.keys.toList())
        assertEquals(listOf("b", "c", "a"), vm.porDia.value.values.flatten().map { it.id })
        assertTrue(vm.ui.value.cargado)
    }

    @Test
    fun `el filtro Hablar deja solo esos registros y Todos los recupera`() = runTest {
        observar()
        vm.filtrar(TipoHistorial.HABLAR)
        assertEquals(listOf("b", "c"), vm.porDia.value.values.flatten().map { it.id })
        vm.filtrar(null)
        assertEquals(3, vm.porDia.value.values.flatten().size)
    }

    @Test
    fun `conteo por tipo para los chips`() = runTest {
        observar()
        assertEquals(1, vm.conteo.value[TipoHistorial.ESCRIBIR])
        assertEquals(2, vm.conteo.value[TipoHistorial.HABLAR])
    }

    @Test
    fun `repetir dice el texto sin crear registros nuevos`() = runTest {
        whenever(voz.hablar(any())).thenReturn(true)
        vm.repetir(registros[1])
        verify(voz).hablar("Gracias")
        verify(repo, never()).agregar(any(), any())
        assertNull(vm.ui.value.error)
    }

    @Test
    fun `eliminar y deshacer restaura el mismo registro`() = runTest {
        whenever(repo.eliminar(any(), any())).thenReturn(Result.success(Unit))
        whenever(repo.restaurar(any(), any())).thenReturn(Result.success(Unit))
        vm.eliminar(registros[0])
        verify(repo).eliminar("u1", "a")
        assertEquals(registros[0], vm.ui.value.eliminado)
        vm.deshacer()
        verify(repo).restaurar("u1", registros[0])
        assertNull(vm.ui.value.eliminado)
        assertEquals("Registro recuperado.", vm.ui.value.mensaje)
        // Borrar no vibra; recuperar el registro es un guardado (doble toque)
        assertEquals(listOf(PatronVibracion.GUARDADO), vibrador.patrones)
    }

    @Test
    fun `si eliminar falla no se ofrece deshacer`() = runTest {
        whenever(repo.eliminar(any(), any())).thenReturn(Result.failure(Exception("Permission denied")))
        vm.eliminar(registros[0])
        assertNull(vm.ui.value.eliminado)
        assertEquals("No tienes permiso para ver o cambiar estos datos.", vm.ui.value.error)
        assertEquals(listOf(PatronVibracion.ERROR), vibrador.patrones)
    }

    @Test
    fun `borrar todo llama al repositorio y avisa`() = runTest {
        whenever(repo.borrarTodo(any())).thenReturn(Result.success(Unit))
        vm.borrarTodo()
        verify(repo).borrarTodo("u1")
        assertEquals("Historial borrado.", vm.ui.value.mensaje)
    }
}

package cl.duoc.rulloa.accesiplus.ui.phrases

import cl.duoc.rulloa.accesiplus.ReglaDispatcherPrincipal
import cl.duoc.rulloa.accesiplus.data.model.OrigenHistorial
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.HistorialRepository
import cl.duoc.rulloa.accesiplus.data.repository.PhraseRepository
import cl.duoc.rulloa.accesiplus.data.tts.EstadoTts
import cl.duoc.rulloa.accesiplus.data.tts.Voz
import cl.duoc.rulloa.accesiplus.domain.FiltroDuplicados
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** Mockito: PhraseViewModel con repositorio y voz simulados. */
class PhraseViewModelTest {

    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private val guardadas = listOf(
        Phrase(id = "f1", text = "Me duele aquí.", category = "Salud", favorite = true),
        Phrase(id = "f2", text = "Texto escrito", category = Phrase.CATEGORIA_ESCRITA)
    )
    private lateinit var auth: AuthRepository
    private lateinit var repo: PhraseRepository
    private lateinit var voz: Voz
    private lateinit var historial: HistorialRepository
    private lateinit var vm: PhraseViewModel
    private var ahora = 0L

    @Before
    fun preparar() {
        auth = mock {
            on { estadoSesion() } doReturn flowOf("u1")
            on { uidActual } doReturn "u1"
        }
        repo = mock { on { observarFrases("u1") } doReturn flowOf(guardadas) }
        voz = mock { on { estado } doReturn MutableStateFlow(EstadoTts.LISTO) }
        historial = mock()
        runBlocking { whenever(historial.agregar(any(), any())).thenReturn(Result.success("h0")) }
        vm =PhraseViewModel(auth, repo, voz, historial, FiltroDuplicados(reloj = { ahora }))
    }

    @Test
    fun `expone las frases del usuario y filtra por categoria`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.frases.collect {} }
        assertEquals(2, vm.frases.value.size)
        assertEquals(listOf("f2"), vm.deCategoria(vm.frases.value, Phrase.CATEGORIA_ESCRITA).map { it.id })
    }

    @Test
    fun `crear guarda la frase recortada en la categoria indicada`() = runTest {
        whenever(repo.crear(any(), any())).thenReturn(Result.success("nuevo"))
        vm.crear("  Necesito ayuda  ", Phrase.CATEGORIA_PROPIA)
        verify(repo).crear(eq("u1"), argThat<Phrase> { text == "Necesito ayuda" && category == Phrase.CATEGORIA_PROPIA })
        assertEquals("Frase guardada.", vm.ui.value.mensaje)
    }

    @Test
    fun `crear una frase vacia no llama al repositorio`() = runTest {
        vm.crear("   ", Phrase.CATEGORIA_PROPIA)
        verify(repo, never()).crear(any(), any())
        assertEquals("La frase debe tener entre 1 y 500 letras.", vm.ui.value.error)
    }

    @Test
    fun `no permite duplicados en la misma categoria`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.frases.collect {} }
        vm.crear("me duele aqui.", "Salud")
        verify(repo, never()).crear(any(), any())
        assertEquals("Esa frase ya está guardada.", vm.ui.value.mensaje)
    }

    @Test
    fun `editar y eliminar llaman al repositorio`() = runTest {
        whenever(repo.actualizar(any(), any())).thenReturn(Result.success(Unit))
        whenever(repo.eliminar(any(), any())).thenReturn(Result.success(Unit))
        vm.editar(guardadas[0], "Me duele la cabeza.")
        verify(repo).actualizar(eq("u1"), argThat<Phrase> { id == "f1" && text == "Me duele la cabeza." })
        vm.eliminar(guardadas[1])
        verify(repo).eliminar("u1", "f2")
        assertEquals("Frase eliminada.", vm.ui.value.mensaje)
    }

    @Test
    fun `alternar favorita invierte el valor`() = runTest {
        whenever(repo.actualizar(any(), any())).thenReturn(Result.success(Unit))
        vm.alternarFavorita(guardadas[0])
        verify(repo).actualizar(eq("u1"), argThat<Phrase> { id == "f1" && !favorite })
    }

    @Test
    fun `hablar usa la voz, muestra la frase y suma un uso`() = runTest {
        whenever(voz.hablar(any())).thenReturn(true)
        whenever(repo.registrarUso(any(), any())).thenReturn(Result.success(Unit))
        vm.hablar("Me duele aquí.", guardadas[0])
        verify(voz).hablar("Me duele aquí.")
        verify(repo).registrarUso("u1", "f1")
        assertEquals("Me duele aquí.", vm.ui.value.enPantalla)
    }

    @Test
    fun `si la voz no esta disponible avisa y deja la frase en pantalla`() = runTest {
        whenever(voz.hablar(any())).thenReturn(false)
        vm.hablar("Hola")
        assertEquals("La voz no está disponible. La frase se muestra en pantalla.", vm.ui.value.error)
        assertEquals("Hola", vm.ui.value.enPantalla)
        verify(repo, never()).registrarUso(any(), any())
    }

    @Test
    fun `hablar guarda automaticamente en el historial con su origen`() = runTest {
        whenever(voz.hablar(any())).thenReturn(true)
        whenever(historial.agregar(any(), any())).thenReturn(Result.success("h1"))
        vm.hablar("  Necesito ayuda ")
        verify(historial).agregar("u1", RegistroHistorial(tipo = TipoHistorial.HABLAR, texto = "Necesito ayuda", origen = OrigenHistorial.TEXTO_LIBRE))
        vm.hablar("Me duele aquí.", guardadas[0])
        verify(historial).agregar(eq("u1"), argThat<RegistroHistorial> { origen == OrigenHistorial.FRASE_GUARDADA })
        vm.hablar("Gracias", origen = OrigenHistorial.WIDGET)
        verify(historial).agregar(eq("u1"), argThat<RegistroHistorial> { texto == "Gracias" && origen == OrigenHistorial.WIDGET })
    }

    @Test
    fun `el resultado final de Escribir se guarda sin origen`() = runTest {
        whenever(historial.agregar(any(), any())).thenReturn(Result.success("h2"))
        vm.registrarEscrito("Buenos días")
        verify(historial).agregar("u1", RegistroHistorial(tipo = TipoHistorial.ESCRIBIR, texto = "Buenos días"))
    }

    @Test
    fun `textos vacios y dobles disparos no se guardan`() = runTest {
        whenever(historial.agregar(any(), any())).thenReturn(Result.success("h3"))
        vm.registrarEscrito("   ")
        vm.registrarEscrito("Hola")
        ahora = 300
        vm.registrarEscrito("Hola") // doble disparo: se ignora
        ahora = 10_000
        vm.registrarEscrito("Hola") // ya pasó la ventana: es una solicitud nueva
        verify(historial, times(2)).agregar(any(), any())
    }

    @Test
    fun `si el historial falla se avisa al usuario`() = runTest {
        whenever(historial.agregar(any(), any())).thenReturn(Result.failure(Exception("Permission denied")))
        vm.registrarEscrito("Hola")
        assertEquals("No se pudo guardar en el historial. No tienes permiso para ver o cambiar estos datos.", vm.ui.value.error)
    }
}

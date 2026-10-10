package cl.duoc.rulloa.accesiplus.data.repository

import cl.duoc.rulloa.accesiplus.data.model.OrigenHistorial
import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import cl.duoc.rulloa.accesiplus.data.model.toMap
import cl.duoc.rulloa.accesiplus.data.remote.RutasFirebase
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** Mockito: FirebaseHistorialRepository con referencias de Realtime Database simuladas. */
class HistorialRepositoryTest {

    private lateinit var nodo: DatabaseReference
    private lateinit var hijo: DatabaseReference
    private lateinit var repo: FirebaseHistorialRepository

    @Before
    fun preparar() {
        // Tarea ya completada: awaitEscritura() termina al instante, sin red
        val lista = Tasks.forResult<Void>(null)
        hijo = mock {
            on { key } doReturn "nuevo"
            on { setValue(any()) } doReturn lista
            on { removeValue() } doReturn lista
        }
        nodo = mock {
            on { push() } doReturn hijo
            on { child(any()) } doReturn hijo
            on { removeValue() } doReturn lista
        }
        val rutas: RutasFirebase = mock { on { historial("u1") } doReturn nodo }
        repo = FirebaseHistorialRepository(rutas)
    }

    @Test
    fun `agregar usa push y la hora del servidor`() = runTest {
        val res = repo.agregar("u1", RegistroHistorial(tipo = TipoHistorial.HABLAR, texto = "Hola", origen = OrigenHistorial.TEXTO_LIBRE))
        assertEquals("nuevo", res.getOrNull())
        verify(hijo).setValue(argThat<Map<String, Any?>> {
            this["timestamp"] == ServerValue.TIMESTAMP && this["tipo"] == "HABLAR" && this["texto"] == "Hola"
        })
    }

    @Test
    fun `eliminar borra solo ese registro y borrarTodo el nodo completo`() = runTest {
        assertTrue(repo.eliminar("u1", "h1").isSuccess)
        verify(nodo).child("h1")
        verify(hijo).removeValue()
        assertTrue(repo.borrarTodo("u1").isSuccess)
        verify(nodo).removeValue()
    }

    @Test
    fun `eliminar sin id falla sin tocar Firebase`() = runTest {
        assertTrue(repo.eliminar("u1", "").isFailure)
    }

    @Test
    fun `restaurar vuelve a escribir con el mismo id y la hora original`() = runTest {
        val r = RegistroHistorial("h9", TipoHistorial.ESCRIBIR, "Buenos días", timestamp = 1234L)
        assertTrue(repo.restaurar("u1", r).isSuccess)
        verify(nodo).child("h9")
        verify(hijo).setValue(r.toMap())
    }

    @Test
    fun `observar pide los ultimos 200 por timestamp y los entrega del mas nuevo al mas antiguo`() = runTest {
        val viejo = snapshot("a", mapOf("tipo" to "ESCRIBIR", "texto" to "Hola", "timestamp" to 10L))
        val nuevo = snapshot("b", mapOf("tipo" to "HABLAR", "texto" to "Chao", "timestamp" to 20L))
        val malo = snapshot("c", mapOf("tipo" to "HABLAR")) // sin texto: se descarta
        val raiz: DataSnapshot = mock { on { children } doReturn listOf(viejo, nuevo, malo) }
        val consulta: Query = mock {
            on { addValueEventListener(any()) } doAnswer { inv ->
                inv.getArgument<ValueEventListener>(0).also { it.onDataChange(raiz) }
            }
        }
        val ordenada: Query = mock { on { limitToLast(200) } doReturn consulta }
        whenever(nodo.orderByChild("timestamp")).thenReturn(ordenada)

        val lista = repo.observar("u1").first()

        assertEquals(listOf("b", "a"), lista.map { it.id })
        verify(ordenada).limitToLast(200)
    }

    private fun snapshot(id: String, valor: Map<String, Any?>): DataSnapshot = mock {
        on { key } doReturn id
        on { value } doReturn valor
    }
}

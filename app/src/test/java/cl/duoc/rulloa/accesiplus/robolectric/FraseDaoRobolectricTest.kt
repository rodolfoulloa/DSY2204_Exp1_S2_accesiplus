package cl.duoc.rulloa.accesiplus.robolectric

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.rulloa.accesiplus.data.local.BaseLocal
import cl.duoc.rulloa.accesiplus.data.local.FraseLocal
import cl.duoc.rulloa.accesiplus.data.local.aLocal
import cl.duoc.rulloa.accesiplus.data.local.aPhrase
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Robolectric: DAO de Room con una base SQLite en memoria. */
@RunWith(AndroidJUnit4::class)
class FraseDaoRobolectricTest {

    private lateinit var db: BaseLocal

    private fun frase(id: String, uid: String = "u1", favorita: Boolean = false, t: Long = 0) =
        FraseLocal(id, uid, "Frase $id", "Salud", favorita, usos = 0, actualizada = t)

    @Before
    fun abrir() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), BaseLocal::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun `insertar y observar ordena por fecha descendente`() = runTest {
        db.frases().insertar(listOf(frase("a", t = 1), frase("b", t = 3), frase("c", t = 2)))
        assertEquals(listOf("b", "c", "a"), db.frases().observar("u1").first().map { it.id })
    }

    @Test
    fun `reemplazar borra solo las frases de ese usuario`() = runTest {
        db.frases().insertar(listOf(frase("a"), frase("x", uid = "otro")))
        db.frases().reemplazar("u1", listOf(frase("b"), frase("c")))
        assertEquals(setOf("b", "c"), db.frases().todas("u1").map { it.id }.toSet())
        assertEquals(listOf("x"), db.frases().todas("otro").map { it.id })
    }

    @Test
    fun `cursor de favoritas expone las columnas del proveedor`() = runTest {
        db.frases().insertar(listOf(frase("a", favorita = true, t = 1), frase("b"), frase("c", favorita = true, t = 5)))
        db.frases().cursorFavoritas().use { c ->
            assertEquals(2, c.count)
            c.moveToFirst()
            assertEquals("c", c.getString(c.getColumnIndexOrThrow("id")))
            assertEquals("Frase c", c.getString(c.getColumnIndexOrThrow("texto")))
            assertEquals(1, c.getInt(c.getColumnIndexOrThrow("favorita")))
        }
    }

    @Test
    fun `borrarTodo deja la cache vacia al cerrar sesion`() = runTest {
        db.frases().insertar(listOf(frase("a"), frase("b", uid = "otro")))
        db.frases().borrarTodo()
        db.frases().cursorTodas().use { assertEquals(0, it.count) }
    }

    @Test
    fun `conversion entre Phrase y FraseLocal`() {
        val p = Phrase("id1", "Hola", "Compras", favorite = true, uses = 4, createdAt = 10, updatedAt = 20)
        val local = p.aLocal("u1")
        assertEquals(20L, local.actualizada)
        assertEquals(p.copy(createdAt = 20), local.aPhrase())
    }
}

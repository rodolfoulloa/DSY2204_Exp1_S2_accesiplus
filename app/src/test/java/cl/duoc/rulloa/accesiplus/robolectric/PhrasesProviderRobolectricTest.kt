package cl.duoc.rulloa.accesiplus.robolectric

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.content.contentValuesOf
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.rulloa.accesiplus.data.local.BaseLocal
import cl.duoc.rulloa.accesiplus.data.local.FraseLocal
import cl.duoc.rulloa.accesiplus.provider.PhrasesProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

/** Robolectric: ContentProvider de frases respaldado por Room (base en memoria). */
@RunWith(AndroidJUnit4::class)
class PhrasesProviderRobolectricTest {

    private lateinit var db: BaseLocal
    private lateinit var resolver: ContentResolver

    @Before
    fun preparar() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BaseLocal::class.java).allowMainThreadQueries().build()
        BaseLocal.reemplazarParaPruebas(db)
        db.frases().insertar(
            listOf(
                FraseLocal("1", "u1", "Me duele aquí.", "Salud", favorita = true, usos = 2, actualizada = 10),
                FraseLocal("2", "u1", "¿Cuánto cuesta?", "Compras", favorita = false, usos = 0, actualizada = 20),
                FraseLocal("3", "u1", "Hola", "Propias", favorita = true, usos = 1, actualizada = 30)
            )
        )
        // Registra el proveedor en Robolectric con su autoridad real
        Robolectric.buildContentProvider(PhrasesProvider::class.java).create(PhrasesProvider.AUTORIDAD)
        resolver = context.contentResolver
    }

    @After
    fun cerrar() {
        BaseLocal.reemplazarParaPruebas(null)
        db.close()
    }

    @Test
    fun `query de todas las frases`() {
        resolver.query(PhrasesProvider.URI_FRASES, null, null, null, null)!!.use { c ->
            assertEquals(3, c.count)
            c.moveToFirst()
            assertEquals("Hola", c.getString(c.getColumnIndexOrThrow(PhrasesProvider.COL_TEXTO)))
        }
    }

    @Test
    fun `query de favoritas para el widget`() {
        resolver.query(PhrasesProvider.URI_FAVORITAS, null, null, null, null)!!.use { c ->
            assertEquals(2, c.count)
            c.moveToFirst()
            assertEquals("Hola", c.getString(c.getColumnIndexOrThrow(PhrasesProvider.COL_TEXTO)))
            assertEquals("Propias", c.getString(c.getColumnIndexOrThrow(PhrasesProvider.COL_CATEGORIA)))
        }
    }

    @Test
    fun `getType devuelve un tipo de directorio`() {
        assertEquals(
            "vnd.android.cursor.dir/vnd.${PhrasesProvider.AUTORIDAD}.frase",
            resolver.getType(PhrasesProvider.URI_FRASES)
        )
    }

    @Test
    fun `uri desconocida lanza error`() {
        assertThrows(IllegalArgumentException::class.java) {
            resolver.query(Uri.parse("content://${PhrasesProvider.AUTORIDAD}/otra"), null, null, null, null)
        }
    }

    @Test
    fun `el proveedor es de solo lectura`() {
        // contentValuesOf (core-ktx) arma los valores en una línea
        val valores = contentValuesOf(PhrasesProvider.COL_TEXTO to "Intento", PhrasesProvider.COL_CATEGORIA to "Salud")
        assertThrows(UnsupportedOperationException::class.java) { resolver.insert(PhrasesProvider.URI_FRASES, valores) }
        assertThrows(UnsupportedOperationException::class.java) { resolver.delete(PhrasesProvider.URI_FRASES, null, null) }
    }
}

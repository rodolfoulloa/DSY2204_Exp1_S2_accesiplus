package cl.duoc.rulloa.accesiplus.data.local

import android.content.Context
import android.database.Cursor
import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import kotlinx.coroutines.flow.Flow

/**
 * Copia local de las frases del usuario. La fuente de verdad sigue siendo Firebase:
 * PhraseSync reemplaza estas filas cada vez que cambian en users/{uid}/phrases.
 * La usa el ContentProvider (y por él, el widget) sin depender de la red.
 */
@Entity(tableName = "frases", indices = [Index("uid")])
data class FraseLocal(
    @PrimaryKey val id: String,
    val uid: String,
    val texto: String,
    val categoria: String,
    val favorita: Boolean,
    val usos: Long,
    val actualizada: Long
)

fun Phrase.aLocal(uid: String) = FraseLocal(
    id = id, uid = uid, texto = text, categoria = category,
    favorita = favorite, usos = uses, actualizada = maxOf(updatedAt, createdAt)
)

fun FraseLocal.aPhrase() = Phrase(
    id = id, text = texto, category = categoria, favorite = favorita,
    uses = usos, createdAt = actualizada, updatedAt = actualizada
)

@Dao
interface FraseDao {
    @Query("SELECT * FROM frases WHERE uid = :uid ORDER BY actualizada DESC")
    fun observar(uid: String): Flow<List<FraseLocal>>

    @Query("SELECT * FROM frases WHERE uid = :uid ORDER BY actualizada DESC")
    suspend fun todas(uid: String): List<FraseLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(frases: List<FraseLocal>)

    @Query("DELETE FROM frases WHERE uid = :uid")
    suspend fun borrarDe(uid: String)

    @Query("DELETE FROM frases")
    suspend fun borrarTodo()

    /** Reemplaza todas las frases del usuario en una sola transacción. */
    @Transaction
    suspend fun reemplazar(uid: String, frases: List<FraseLocal>) {
        borrarDe(uid)
        insertar(frases)
    }

    // --- Consultas con Cursor para el ContentProvider (se ejecutan fuera del hilo principal) ---

    @Query(
        "SELECT rowid AS _id, id, texto, categoria, favorita, usos, actualizada FROM frases " +
            "ORDER BY actualizada DESC"
    )
    fun cursorTodas(): Cursor

    @Query(
        "SELECT rowid AS _id, id, texto, categoria, favorita, usos, actualizada FROM frases " +
            "WHERE favorita = 1 ORDER BY actualizada DESC"
    )
    fun cursorFavoritas(): Cursor
}

@Database(entities = [FraseLocal::class], version = 1, exportSchema = false)
abstract class BaseLocal : RoomDatabase() {
    abstract fun frases(): FraseDao

    companion object {
        @Volatile private var instancia: BaseLocal? = null

        fun obtener(context: Context): BaseLocal = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(context.applicationContext, BaseLocal::class.java, "accesiplus.db")
                .build().also { instancia = it }
        }

        /** Solo para pruebas: permite inyectar una base en memoria. */
        @VisibleForTesting
        fun reemplazarParaPruebas(db: BaseLocal?) {
            instancia = db
        }
    }
}

package cl.duoc.rulloa.accesiplus.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import cl.duoc.rulloa.accesiplus.data.local.BaseLocal

/**
 * ContentProvider de solo lectura con las frases guardadas (caché Room).
 *  - content://cl.duoc.rulloa.accesiplus.frases/frases
 *  - content://cl.duoc.rulloa.accesiplus.frases/frases/favoritas
 * Lo consume el widget "Frase rápida". En release no se exporta a otras apps.
 */
class PhrasesProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val dao = BaseLocal.obtener(requireNotNull(context)).frases()
        val cursor = when (MATCHER.match(uri)) {
            TODAS -> dao.cursorTodas()
            FAVORITAS -> dao.cursorFavoritas()
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
        cursor.setNotificationUri(requireNotNull(context).contentResolver, URI_FRASES)
        return cursor
    }

    override fun getType(uri: Uri): String = when (MATCHER.match(uri)) {
        TODAS, FAVORITAS -> "vnd.android.cursor.dir/vnd.$AUTORIDAD.frase"
        else -> throw IllegalArgumentException("URI desconocida: $uri")
    }

    // Las frases se crean y editan en Firebase; aquí solo se exponen para lectura
    override fun insert(uri: Uri, values: ContentValues?): Uri =
        throw UnsupportedOperationException("Proveedor de solo lectura")

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Proveedor de solo lectura")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Proveedor de solo lectura")

    companion object {
        const val AUTORIDAD = "cl.duoc.rulloa.accesiplus.frases"
        val URI_FRASES: Uri = "content://$AUTORIDAD/frases".toUri()
        val URI_FAVORITAS: Uri = "content://$AUTORIDAD/frases/favoritas".toUri()

        // Columnas expuestas
        const val COL_ID = "id"
        const val COL_TEXTO = "texto"
        const val COL_CATEGORIA = "categoria"
        const val COL_FAVORITA = "favorita"
        const val COL_USOS = "usos"

        private const val TODAS = 1
        private const val FAVORITAS = 2
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTORIDAD, "frases", TODAS)
            addURI(AUTORIDAD, "frases/favoritas", FAVORITAS)
        }
    }
}

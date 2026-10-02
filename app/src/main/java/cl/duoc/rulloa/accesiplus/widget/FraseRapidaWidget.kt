package cl.duoc.rulloa.accesiplus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import cl.duoc.rulloa.accesiplus.MainActivity
import cl.duoc.rulloa.accesiplus.R
import cl.duoc.rulloa.accesiplus.provider.PhrasesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Widget "Frase rápida": muestra la última frase favorita (leída desde el ContentProvider)
 * y, al tocar "Decir", abre Hablar y la reproduce con texto a voz.
 */
class FraseRapidaWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        // La consulta al ContentProvider no puede ir en el hilo principal: goAsync + corrutina
        val pendiente = goAsync()
        alcance.launch {
            try {
                val frase = ultimaFavorita(context)
                ids.forEach { id -> manager.updateAppWidget(id, vistas(context, frase)) }
            } finally {
                pendiente.finish()
            }
        }
    }

    companion object {
        const val EXTRA_FRASE = "cl.duoc.rulloa.accesiplus.EXTRA_FRASE"
        const val EXTRA_ABRIR_HABLAR = "cl.duoc.rulloa.accesiplus.EXTRA_ABRIR_HABLAR"
        private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** Pide a todos los widgets instalados que se redibujen (lo llama PhraseSync). */
        fun actualizarTodos(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, FraseRapidaWidget::class.java))
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, FraseRapidaWidget::class.java)
                    .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            )
        }

        fun ultimaFavorita(context: Context): String? =
            context.contentResolver.query(PhrasesProvider.URI_FAVORITAS, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(c.getColumnIndexOrThrow(PhrasesProvider.COL_TEXTO)) else null
            }

        private fun vistas(context: Context, frase: String?): RemoteViews =
            RemoteViews(context.packageName, R.layout.widget_frase_rapida).apply {
                setTextViewText(
                    R.id.widget_texto,
                    frase ?: context.getString(R.string.widget_sin_favorita)
                )
                val abrir = Intent(context, MainActivity::class.java)
                    .putExtra(EXTRA_ABRIR_HABLAR, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                setOnClickPendingIntent(R.id.widget_raiz, pendingIntent(context, 0, abrir))
                if (frase != null) {
                    val decir = Intent(abrir).putExtra(EXTRA_FRASE, frase)
                    setOnClickPendingIntent(R.id.widget_boton_decir, pendingIntent(context, 1, decir))
                } else {
                    setOnClickPendingIntent(R.id.widget_boton_decir, pendingIntent(context, 0, abrir))
                }
            }

        private fun pendingIntent(context: Context, codigo: Int, intent: Intent): PendingIntent =
            PendingIntent.getActivity(
                context, codigo, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }
}

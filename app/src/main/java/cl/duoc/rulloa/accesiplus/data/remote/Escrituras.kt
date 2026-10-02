package cl.duoc.rulloa.accesiplus.data.remote

import android.util.Log
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "Escrituras"
private const val ESPERA_MS = 6_000L

/**
 * Espera la confirmación del servidor con await() (kotlinx-coroutines-play-services).
 * Con la persistencia offline activada, la escritura ya quedó aplicada en la caché local:
 * si no hay red, la tarea no termina hasta reconectar. Por eso se espera un máximo de
 * [ESPERA_MS]; pasado ese tiempo se considera "en cola" y Firebase la enviará solo.
 */
suspend fun Task<Void>.awaitEscritura() {
    // Task<Void>.await() devuelve null: se devuelve true para distinguirlo del timeout
    val confirmada = withTimeoutOrNull(ESPERA_MS) { await(); true }
    if (confirmada == null) Log.i(TAG, "Escritura en cola: se sincronizará al volver la conexión")
}

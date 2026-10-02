package cl.duoc.rulloa.accesiplus.data.local

import android.util.Log
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.PhraseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Mantiene Room sincronizado con users/{uid}/phrases mientras haya sesión.
 * Al cerrar sesión borra la caché para que el widget no muestre frases de otra cuenta.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PhraseSync(
    private val auth: AuthRepository,
    private val remoto: PhraseRepository,
    private val dao: FraseDao,
    private val alCambiar: () -> Unit
) {
    fun iniciar(scope: CoroutineScope) {
        scope.launch {
            auth.estadoSesion()
                .onEach { uid -> if (uid == null) { dao.borrarTodo(); alCambiar() } }
                .flatMapLatest { uid ->
                    if (uid == null) emptyFlow()
                    else remoto.observarFrases(uid).map { lista -> uid to lista }
                }
                .catch { e -> Log.w(TAG, "Sincronización detenida", e) }
                .collectLatest { (uid, lista) ->
                    dao.reemplazar(uid, lista.map { it.aLocal(uid) })
                    alCambiar()
                }
        }
    }

    private companion object { const val TAG = "PhraseSync" }
}

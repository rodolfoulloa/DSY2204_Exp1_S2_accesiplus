package cl.duoc.rulloa.accesiplus.ui.tutorial

import androidx.lifecycle.ViewModel
import cl.duoc.rulloa.accesiplus.data.local.Preferencias
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository

/** Decide si se muestra el tutorial de primer inicio y lo marca como visto para el usuario actual. */
class TutorialViewModel(
    private val auth: AuthRepository,
    private val prefs: Preferencias
) : ViewModel() {

    /** Solo con sesión: el tutorial se recuerda por usuario (uid). */
    fun debeMostrar(): Boolean {
        val uid = auth.uidActual ?: return false
        return !prefs.tutorialVisto(uid)
    }

    /** Se llama al tocar Comenzar u Omitir (o Atrás): no vuelve a aparecer solo. */
    fun completar() {
        auth.uidActual?.let(prefs::marcarTutorialVisto)
    }
}

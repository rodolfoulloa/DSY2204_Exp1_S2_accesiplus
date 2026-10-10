package cl.duoc.rulloa.accesiplus.data.local

import android.content.Context
import androidx.core.content.edit

/**
 * Preferencias locales del dispositivo (no viajan a Firebase). Las claves llevan el uid:
 * si dos personas usan el mismo teléfono, cada una ve el tutorial la primera vez.
 */
interface Preferencias {
    fun tutorialVisto(uid: String): Boolean
    fun marcarTutorialVisto(uid: String)
    fun reiniciarTutorial(uid: String)
}

class PreferenciasApp(context: Context) : Preferencias {

    private val prefs = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    override fun tutorialVisto(uid: String): Boolean = prefs.getBoolean(claveTutorial(uid), false)

    // edit { } (core-ktx) abre el editor, aplica los cambios y llama a apply() al terminar
    override fun marcarTutorialVisto(uid: String) = prefs.edit { putBoolean(claveTutorial(uid), true) }

    override fun reiniciarTutorial(uid: String) = prefs.edit { remove(claveTutorial(uid)) }

    companion object {
        const val ARCHIVO = "accesiplus_preferencias"
        fun claveTutorial(uid: String) = "tutorial_visto_$uid"
    }
}

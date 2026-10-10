package cl.duoc.rulloa.accesiplus.data.local

import android.content.Context
import androidx.core.content.edit

/**
 * Preferencias locales del dispositivo (no viajan a Firebase). Las claves del tutorial llevan
 * el uid: si dos personas usan el mismo teléfono, cada una ve el tutorial la primera vez.
 */
interface Preferencias {
    fun tutorialVisto(uid: String): Boolean
    fun marcarTutorialVisto(uid: String)
    fun reiniciarTutorial(uid: String)

    /** Interruptor "Vibración" del perfil. Es del teléfono, no de la cuenta: activada por defecto. */
    fun vibracionActiva(): Boolean
    fun cambiarVibracion(activa: Boolean)
}

class PreferenciasApp(context: Context) : Preferencias {

    private val prefs = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    override fun tutorialVisto(uid: String): Boolean = prefs.getBoolean(claveTutorial(uid), false)

    // edit { } (core-ktx) abre el editor, aplica los cambios y llama a apply() al terminar
    override fun marcarTutorialVisto(uid: String) = prefs.edit { putBoolean(claveTutorial(uid), true) }

    override fun reiniciarTutorial(uid: String) = prefs.edit { remove(claveTutorial(uid)) }

    override fun vibracionActiva(): Boolean = prefs.getBoolean(CLAVE_VIBRACION, true)

    override fun cambiarVibracion(activa: Boolean) = prefs.edit { putBoolean(CLAVE_VIBRACION, activa) }

    companion object {
        const val ARCHIVO = "accesiplus_preferencias"
        const val CLAVE_VIBRACION = "vibracion_activa"
        fun claveTutorial(uid: String) = "tutorial_visto_$uid"
    }
}

package cl.duoc.rulloa.accesiplus

import android.app.Application
import cl.duoc.rulloa.accesiplus.di.AppContainer
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AccesiPlusApp : Application() {

    lateinit var container: AppContainer
        private set

    /** Alcance que vive mientras vive el proceso (sincronización Firebase → Room). */
    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Debe llamarse antes de cualquier otra operación con la base de datos:
        // guarda una copia local para seguir funcionando con cortes de red.
        Firebase.database.setPersistenceEnabled(true)
        container = AppContainer(this)
        container.phraseSync.iniciar(alcanceApp)
    }
}

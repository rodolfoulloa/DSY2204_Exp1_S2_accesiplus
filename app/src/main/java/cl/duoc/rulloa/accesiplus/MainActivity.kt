package cl.duoc.rulloa.accesiplus

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import cl.duoc.rulloa.accesiplus.ui.components.BannerSinConexion
import cl.duoc.rulloa.accesiplus.ui.home.TamanoVentana
import cl.duoc.rulloa.accesiplus.ui.navigation.AccesiPlusNavHost
import cl.duoc.rulloa.accesiplus.ui.navigation.SolicitudWidget
import cl.duoc.rulloa.accesiplus.ui.theme.AccesiPlusTheme
import cl.duoc.rulloa.accesiplus.widget.FraseRapidaWidget
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FragmentActivity (y no solo ComponentActivity) porque BuscarDispositivo integra
 * un Fragment clásico dentro de Compose con AndroidFragment.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : FragmentActivity() {

    /** Último toque en el widget pendiente de atender por la navegación. */
    private val solicitudWidget = MutableStateFlow<SolicitudWidget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) leerWidget(intent)
        val conexion = (application as AccesiPlusApp).container.connectivityObserver
        setContent {
            AccesiPlusTheme {
                val navController = rememberNavController()
                // WindowSizeClass: se recalcula al girar la pantalla o cambiar de dispositivo
                val clase = calculateWindowSizeClass(this)
                val tamano = TamanoVentana(clase.widthSizeClass, clase.heightSizeClass)
                val flujoConexion = remember { conexion.observarConexion() }
                val hayInternet by flujoConexion.collectAsStateWithLifecycle(initialValue = true)
                val solicitud by solicitudWidget.collectAsStateWithLifecycle()
                Surface(color = MaterialTheme.colorScheme.background) {
                    // testTagsAsResourceId expone los testTag a UI Automator (capturas automatizadas)
                    Column(Modifier.fillMaxSize().safeDrawingPadding().semantics { testTagsAsResourceId = true }) {
                        if (!hayInternet) BannerSinConexion()
                        Column(Modifier.weight(1f)) {
                            AccesiPlusNavHost(
                                navController = navController,
                                tamano = tamano,
                                solicitudWidget = solicitud,
                                onSolicitudWidgetAtendida = { solicitudWidget.value = null }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerWidget(intent)
    }

    private fun leerWidget(intent: Intent?) {
        if (intent?.getBooleanExtra(FraseRapidaWidget.EXTRA_ABRIR_HABLAR, false) == true) {
            solicitudWidget.value = SolicitudWidget(intent.getStringExtra(FraseRapidaWidget.EXTRA_FRASE))
        }
    }
}

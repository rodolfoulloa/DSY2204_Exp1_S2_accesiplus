package cl.duoc.rulloa.accesiplus

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import cl.duoc.rulloa.accesiplus.ui.components.BannerSinConexion
import cl.duoc.rulloa.accesiplus.ui.home.TamanoVentana
import cl.duoc.rulloa.accesiplus.ui.navigation.AccesiPlusNavHost
import cl.duoc.rulloa.accesiplus.ui.theme.AccesiPlusTheme

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val conexion = (application as AccesiPlusApp).container.connectivityObserver
        setContent {
            AccesiPlusTheme {
                val navController = rememberNavController()
                // WindowSizeClass: se recalcula al girar la pantalla o cambiar de dispositivo
                val clase = calculateWindowSizeClass(this)
                val tamano = TamanoVentana(clase.widthSizeClass, clase.heightSizeClass)
                val flujoConexion = remember { conexion.observarConexion() }
                val hayInternet by flujoConexion.collectAsStateWithLifecycle(initialValue = true)
                Surface(color = MaterialTheme.colorScheme.background) {
                    // testTagsAsResourceId expone los testTag a UI Automator (capturas automatizadas)
                    Column(Modifier.fillMaxSize().safeDrawingPadding().semantics { testTagsAsResourceId = true }) {
                        if (!hayInternet) BannerSinConexion()
                        Column(Modifier.weight(1f)) {
                            AccesiPlusNavHost(navController = navController, tamano = tamano)
                        }
                    }
                }
            }
        }
    }
}

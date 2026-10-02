package cl.duoc.rulloa.accesiplus

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import cl.duoc.rulloa.accesiplus.ui.components.BannerSinConexion
import cl.duoc.rulloa.accesiplus.ui.navigation.AccesiPlusNavHost
import cl.duoc.rulloa.accesiplus.ui.theme.AccesiPlusTheme
import java.util.Locale

val LocalTextToSpeech = staticCompositionLocalOf<TextToSpeech?> { null }

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        enableEdgeToEdge()
        val conexion = (application as AccesiPlusApp).container.connectivityObserver
        setContent {
            AccesiPlusTheme {
                val navController = rememberNavController()
                val flujoConexion = remember { conexion.observarConexion() }
                val hayInternet by flujoConexion.collectAsStateWithLifecycle(initialValue = true)
                CompositionLocalProvider(LocalTextToSpeech provides tts) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        // testTagsAsResourceId expone los testTag a UI Automator (capturas automatizadas)
                        Column(Modifier.fillMaxSize().safeDrawingPadding().semantics { testTagsAsResourceId = true }) {
                            if (!hayInternet) BannerSinConexion()
                            Column(Modifier.weight(1f)) {
                                AccesiPlusNavHost(navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.forLanguageTag("es-CL")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

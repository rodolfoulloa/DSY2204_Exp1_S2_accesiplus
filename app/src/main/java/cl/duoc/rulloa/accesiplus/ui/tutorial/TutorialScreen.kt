package cl.duoc.rulloa.accesiplus.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import kotlinx.coroutines.launch

data class PaginaTutorial(val titulo: String, val texto: String, val icono: ImageVector)

/** Contenido y reglas de avance del tutorial (la lógica se prueba con JUnit). */
object PasosTutorial {
    val paginas = listOf(
        PaginaTutorial(
            "Te damos la bienvenida a AccesiPlus",
            "Esta app te ayuda a comunicarte: muestra en texto lo que te dicen y dice en voz alta lo que tú escribes.",
            Icons.Filled.RecordVoiceOver
        ),
        PaginaTutorial(
            "Escribir: lee lo que te dicen",
            "Toca «Escuchar» y acerca el teléfono a quien te habla. Sus palabras aparecerán en letra grande.",
            Icons.Filled.EditNote
        ),
        PaginaTutorial(
            "Hablar: el teléfono habla por ti",
            "Escribe lo que quieres decir o elige una frase rápida y toca «Decir en voz alta».",
            Icons.AutoMirrored.Filled.VolumeUp
        ),
        PaginaTutorial(
            "Historial y frases",
            "Todo lo que escuchas y dices se guarda solo en el Historial. Marca tus frases favoritas con la estrella.",
            Icons.Filled.History
        )
    )

    fun esUltima(pagina: Int, total: Int = paginas.size) = pagina >= total - 1
    fun textoBoton(pagina: Int, total: Int = paginas.size) = if (esUltima(pagina, total)) "Comenzar" else "Siguiente"
    fun descripcionIndicador(pagina: Int, total: Int = paginas.size) = "Página ${pagina + 1} de $total"
}

/**
 * Tutorial de primer inicio. [onTerminar] se llama con Comenzar, Omitir o el botón Atrás:
 * en los tres casos se marca como visto para no volver a interrumpir al usuario.
 */
@Composable
fun TutorialScreen(onTerminar: () -> Unit) {
    val paginas = PasosTutorial.paginas
    val estado = rememberPagerState { paginas.size }
    val scope = rememberCoroutineScope()
    val actual = estado.currentPage
    BackHandler(onBack = onTerminar)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp).testTag("tutorial"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (!PasosTutorial.esUltima(actual)) {
                TextButton(
                    onClick = onTerminar,
                    modifier = Modifier.heightIn(min = ALTO_TACTIL).testTag("boton_tutorial_omitir")
                ) { Text("Omitir", style = MaterialTheme.typography.labelLarge) }
            }
        }

        HorizontalPager(state = estado, modifier = Modifier.weight(1f).fillMaxWidth()) { i ->
            val p = paginas[i]
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
            ) {
                Icon(p.icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(120.dp))
                Text(
                    p.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Text(p.texto, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
        }

        // Indicador de página: puntos para la vista y un texto único para TalkBack
        val descripcion = PasosTutorial.descripcionIndicador(actual)
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clearAndSetSemantics {
                    contentDescription = descripcion
                    liveRegion = LiveRegionMode.Polite
                }
                .testTag("indicador_tutorial"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            paginas.indices.forEach { i ->
                Box(
                    Modifier
                        .size(if (i == actual) 16.dp else 12.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == actual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        BotonGrande(
            texto = PasosTutorial.textoBoton(actual),
            icono = if (PasosTutorial.esUltima(actual)) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                if (PasosTutorial.esUltima(actual)) onTerminar()
                else scope.launch { estado.animateScrollToPage(actual + 1) }
            },
            modifier = Modifier.widthIn(max = 560.dp).testTag("boton_tutorial_siguiente")
        )
    }
}

package cl.duoc.rulloa.accesiplus.ui.ayuda

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.LocalAbrirAyuda
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase

data class SeccionAyuda(val titulo: String, val icono: ImageVector, val pasos: List<String>)

/** Texto de la ayuda en un solo lugar: lenguaje simple y pasos cortos. */
object ContenidoAyuda {
    val secciones = listOf(
        SeccionAyuda(
            "Cómo usar Escribir", Icons.Filled.EditNote, listOf(
                "En el menú, toca «Escribir».",
                "Toca «Escuchar». La primera vez, permite el uso del micrófono.",
                "Acerca el teléfono a la persona que te habla.",
                "Sus palabras aparecen en letra grande. Toca «Ver grande» para mostrarlas en toda la pantalla.",
                "Lo escuchado se guarda solo en el Historial. Toca «Volver al menú» cuando termines."
            )
        ),
        SeccionAyuda(
            "Cómo usar Hablar", Icons.AutoMirrored.Filled.VolumeUp, listOf(
                "En el menú, toca «Hablar».",
                "Escribe lo que quieres decir, o elige una categoría y toca el parlante de una frase rápida.",
                "Toca «Decir en voz alta». El teléfono lo dice y lo muestra en grande.",
                "Toca la estrella para guardar una frase como favorita.",
                "Todo lo que dices queda en el Historial. Toca «Volver al menú» cuando termines."
            )
        ),
        SeccionAyuda(
            "Historial", Icons.Filled.History, listOf(
                "Muestra lo que escuchaste en Escribir y lo que dijiste en Hablar, separado por día.",
                "Usa los botones Todos, Escribir y Hablar para filtrar.",
                "Toca el parlante para repetir una frase en voz alta.",
                "Toca el basurero para borrar un registro. Si te equivocas, toca «Deshacer»."
            )
        ),
        SeccionAyuda(
            "Buscar dispositivo", Icons.Filled.LocationOn, listOf(
                "Toca «Agregar dispositivo» y registra tu audífono, implante o teléfono.",
                "Toca «Registrar ubicación» en el lugar donde lo dejaste.",
                "Después toca «Ver en mapa» para encontrarlo."
            )
        ),
        SeccionAyuda(
            "Consejos para el micrófono", Icons.Filled.Mic, listOf(
                "Acerca el teléfono a unos 20 a 30 cm de la boca de quien habla.",
                "Busca un lugar con poco ruido y pide que hablen claro y sin apuro.",
                "No tapes el micrófono con la mano ni con la funda.",
                "Si dice que falta el permiso, ve a Ajustes del teléfono > Apps > AccesiPlus > Permisos y activa el micrófono.",
                "El reconocimiento de voz necesita internet. Si no funciona, escribe con el teclado."
            )
        ),
        SeccionAyuda(
            "Si no hay conexión", Icons.Filled.WifiOff, listOf(
                "Verás un aviso amarillo arriba que dice «Sin conexión a internet».",
                "Hablar sigue funcionando si tu teléfono tiene la voz en español instalada.",
                "Tus frases y tu historial se guardan en el teléfono y se suben solos cuando vuelve la señal.",
                "Escribir con el micrófono necesita internet: mientras tanto, usa el teclado.",
                "Revisa que el Wi-Fi o los datos móviles estén activados y que el modo avión esté apagado."
            )
        )
    )
}

@Composable
fun AyudaScreen(onVolver: () -> Unit, onVerTutorial: () -> Unit) {
    // Una sección abierta a la vez (acordeón): menos texto a la vista, más fácil de seguir
    var abierta by rememberSaveable { mutableStateOf<Int?>(null) }

    // Ya estamos en Ayuda: no se muestra el ícono de ayuda en la barra superior
    CompositionLocalProvider(LocalAbrirAyuda provides null) {
        PantallaBase(titulo = "Ayuda", onVolver = onVolver) {
            Text("Toca un tema para ver el paso a paso.", style = MaterialTheme.typography.bodyLarge)
            ContenidoAyuda.secciones.forEachIndexed { i, seccion ->
                TarjetaSeccion(
                    seccion = seccion,
                    abierta = abierta == i,
                    onAlternar = { abierta = if (abierta == i) null else i },
                    tag = "ayuda_seccion_$i"
                )
            }
            BotonGrande(
                texto = "Ver tutorial nuevamente",
                icono = Icons.Filled.PlayCircle,
                onClick = onVerTutorial,
                secundario = true,
                modifier = Modifier.testTag("boton_ver_tutorial")
            )
        }
    }
}

@Composable
private fun TarjetaSeccion(seccion: SeccionAyuda, abierta: Boolean, onAlternar: () -> Unit, tag: String) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize().testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ALTO_TACTIL)
                .clickable(role = Role.Button, onClick = onAlternar)
                // TalkBack lee "Cómo usar Escribir, abierta/cerrada, botón"
                .semantics { stateDescription = if (abierta) "Abierta" else "Cerrada" }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(seccion.icono, contentDescription = null, modifier = Modifier.size(32.dp))
            Text(
                seccion.titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp).semantics { heading() }
            )
            Icon(if (abierta) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
        }
        if (abierta) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                seccion.pasos.forEachIndexed { n, paso ->
                    Text("${n + 1}. $paso", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

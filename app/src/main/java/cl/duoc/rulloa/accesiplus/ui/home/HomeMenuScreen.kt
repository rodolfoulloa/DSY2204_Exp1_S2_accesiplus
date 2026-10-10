package cl.duoc.rulloa.accesiplus.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.profile.ProfileViewModel

/** Clases de tamaño de ventana calculadas en MainActivity (WindowSizeClass de Material 3). */
data class TamanoVentana(val ancho: WindowWidthSizeClass, val alto: WindowHeightSizeClass)

val LocalTamanoVentana = compositionLocalOf {
    TamanoVentana(WindowWidthSizeClass.Compact, WindowHeightSizeClass.Medium)
}

/** Teléfono vertical: 1 columna. Tablet u horizontal: 2 columnas. */
fun columnasMenu(t: TamanoVentana): Int =
    if (t.ancho != WindowWidthSizeClass.Compact || t.alto == WindowHeightSizeClass.Compact) 2 else 1

private data class OpcionMenu(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val tag: String,
    val color: @Composable () -> Color,
    val accion: () -> Unit
)

@Composable
fun HomeMenuScreen(
    perfilViewModel: ProfileViewModel,
    tamano: TamanoVentana,
    onEscribir: () -> Unit,
    onHablar: () -> Unit,
    onBuscar: () -> Unit,
    onHistorial: () -> Unit,
    onPerfil: () -> Unit
) {
    val perfil by perfilViewModel.perfil.collectAsStateWithLifecycle()
    val opciones = listOf(
        OpcionMenu("Escribir", "Convierte la voz en texto grande", Icons.Filled.EditNote, "menu_escribir",
            { MaterialTheme.colorScheme.primaryContainer }, onEscribir),
        OpcionMenu("Hablar", "El teléfono dice tus frases", Icons.AutoMirrored.Filled.VolumeUp, "menu_hablar",
            { MaterialTheme.colorScheme.secondaryContainer }, onHablar),
        OpcionMenu("Historial", "Lo que escuchaste y dijiste", Icons.Filled.History, "menu_historial",
            { MaterialTheme.colorScheme.surfaceVariant }, onHistorial),
        OpcionMenu("Buscar dispositivo", "Dónde dejaste tu audífono o teléfono", Icons.Filled.LocationOn, "menu_buscar",
            { MaterialTheme.colorScheme.tertiaryContainer }, onBuscar),
        OpcionMenu("Mi perfil", "Tus datos y cerrar sesión", Icons.Filled.AccountCircle, "menu_perfil",
            { MaterialTheme.colorScheme.surfaceVariant }, onPerfil)
    )
    val columnas = columnasMenu(tamano)

    PantallaBase(titulo = "AccesiPlus", onVolver = null) {
        Text(
            "Hola${perfil?.name?.takeIf { it.isNotBlank() }?.let { ", ${it.substringBefore(' ')}" } ?: ""}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("saludo_home")
        )
        Text("¿Qué quieres hacer?", style = MaterialTheme.typography.titleMedium)
        opciones.chunked(columnas).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { BotonMenu(it, Modifier.weight(1f)) }
                // Completa la fila para que todas las tarjetas tengan el mismo ancho
                repeat(columnas - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BotonMenu(op: OpcionMenu, modifier: Modifier) {
    Card(
        onClick = op.accion,
        colors = CardDefaults.cardColors(containerColor = op.color()),
        modifier = modifier
            .heightIn(min = 120.dp)
            .semantics { role = Role.Button }
            .testTag(op.tag)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(op.icono, contentDescription = null, modifier = Modifier.size(56.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Text(op.titulo, style = MaterialTheme.typography.headlineSmall)
                Text(op.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

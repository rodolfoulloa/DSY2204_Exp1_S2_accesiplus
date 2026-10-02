package cl.duoc.rulloa.accesiplus.ui.phrases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL

/** Tarjeta de una frase guardada con acciones grandes y descritas para TalkBack. */
@Composable
fun TarjetaFrase(
    frase: Phrase,
    onTocar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onFavorita: (() -> Unit)? = null,
    iconoTocar: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Decir en voz alta")
    }
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frase_${frase.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)) {
            Text(
                frase.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onTocar).padding(end = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (frase.uses > 0) {
                    Text(
                        "Usada ${frase.uses} ${if (frase.uses == 1L) "vez" else "veces"}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(onClick = onTocar, modifier = Modifier.size(ALTO_TACTIL)) { iconoTocar() }
                if (onFavorita != null) {
                    IconButton(onClick = onFavorita, modifier = Modifier.size(ALTO_TACTIL)) {
                        Icon(
                            if (frase.favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (frase.favorite) "Quitar de favoritas" else "Marcar como favorita",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                IconButton(onClick = onEditar, modifier = Modifier.size(ALTO_TACTIL)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar frase")
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(ALTO_TACTIL)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar frase", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Diálogo para crear o editar el texto de una frase. */
@Composable
fun DialogoFrase(
    titulo: String,
    textoInicial: String,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(textoInicial) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { if (it.length <= Validaciones.LARGO_MAX_FRASE) texto = it },
                label = { Text("Frase") },
                minLines = 2,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().testTag("dialogo_frase_texto")
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(texto) },
                enabled = Validaciones.isPhraseValid(texto),
                modifier = Modifier.testTag("dialogo_frase_guardar")
            ) { Text("Guardar", style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", style = MaterialTheme.typography.labelLarge) }
        }
    )
}

/** Confirmación antes de borrar (evita eliminar por un toque accidental). */
@Composable
fun DialogoConfirmarEliminar(texto: String, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Eliminar?") },
        text = { Text(texto, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = onConfirmar, modifier = Modifier.testTag("confirmar_eliminar")) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", style = MaterialTheme.typography.labelLarge) }
        }
    )
}

/**
 * Muestra una frase a pantalla completa con letra muy grande y alto contraste,
 * para que la otra persona la lea (por ejemplo, girando el teléfono hacia ella).
 */
@Composable
fun PantallaGrande(texto: String, onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable(onClick = onCerrar)
                .testTag("pantalla_grande")
        ) {
            Text(
                texto,
                color = MaterialTheme.colorScheme.background,
                fontSize = 44.sp,
                lineHeight = 54.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp)
            )
            IconButton(
                onClick = onCerrar,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(ALTO_TACTIL)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.background)
            }
        }
    }
}

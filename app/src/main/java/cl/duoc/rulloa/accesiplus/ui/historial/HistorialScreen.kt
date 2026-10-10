package cl.duoc.rulloa.accesiplus.ui.historial

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.data.model.RegistroHistorial
import cl.duoc.rulloa.accesiplus.data.model.TipoHistorial
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
import cl.duoc.rulloa.accesiplus.ui.components.MostrarMensaje
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.components.TextoError
import cl.duoc.rulloa.accesiplus.ui.phrases.DialogoConfirmarEliminar

/**
 * Historial: todo lo que se escuchó en Escribir y lo que se dijo en Hablar,
 * guardado automáticamente en users/{uid}/history.
 */
@Composable
fun HistorialScreen(viewModel: HistorialViewModel, onVolver: () -> Unit) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val porDia by viewModel.porDia.collectAsStateWithLifecycle()
    val conteo by viewModel.conteo.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var eliminando by remember { mutableStateOf<RegistroHistorial?>(null) }
    var borrandoTodo by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    MostrarMensaje(snackbar, ui.mensaje) { viewModel.limpiarMensajes() }

    // Borrado con opción de deshacer: el Snackbar dura más para dar tiempo a reaccionar
    LaunchedEffect(ui.eliminado) {
        if (ui.eliminado != null) {
            val r = snackbar.showSnackbar("Registro eliminado.", actionLabel = "Deshacer", duration = SnackbarDuration.Long)
            if (r == SnackbarResult.ActionPerformed) viewModel.deshacer() else viewModel.limpiarEliminado()
        }
    }

    val visibles = porDia.values.sumOf { it.size }

    PantallaBase(
        titulo = "Historial",
        onVolver = onVolver,
        snackbar = snackbar,
        desplazable = false,
        acciones = {
            if (registros.isNotEmpty()) {
                IconButton(onClick = { borrandoTodo = true }, modifier = Modifier.size(ALTO_TACTIL).testTag("boton_borrar_historial")) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Borrar todo el historial")
                }
            }
        }
    ) {
        Text(
            "Aquí se guarda solo lo que escuchaste en Escribir y lo que dijiste en Hablar.",
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipFiltro("Todos (${registros.size})", filtro == null, "filtro_todos") { viewModel.filtrar(null) }
            TipoHistorial.entries.forEach { tipo ->
                ChipFiltro("${tipo.etiqueta} (${conteo[tipo] ?: 0})", filtro == tipo, "filtro_${tipo.name}") {
                    viewModel.filtrar(tipo)
                }
            }
        }

        // TalkBack anuncia el total cada vez que cambia el filtro o se borra algo
        Text(
            if (visibles == 1) "1 registro" else "$visibles registros",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("total_historial")
        )
        TextoError(ui.error)

        if (ui.cargado && visibles == 0) {
            EstadoVacio(filtro)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).testTag("lista_historial"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                porDia.forEach { (dia, lista) ->
                    item(key = "dia_$dia") {
                        Text(
                            FormatoHistorial.etiquetaDia(dia),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp).semantics { heading() }
                        )
                    }
                    items(lista, key = { it.id }) { registro ->
                        TarjetaHistorial(
                            registro = registro,
                            onRepetir = { viewModel.repetir(registro) },
                            onEliminar = { eliminando = registro }
                        )
                    }
                }
            }
        }
    }

    eliminando?.let { r ->
        DialogoConfirmarEliminar(
            "Se eliminará del historial: \"${r.texto}\"",
            onConfirmar = { viewModel.eliminar(r); eliminando = null },
            onCancelar = { eliminando = null }
        )
    }
    if (borrandoTodo) {
        AlertDialog(
            onDismissRequest = { borrandoTodo = false },
            title = { Text("¿Borrar todo el historial?") },
            text = { Text("Se borrarán los ${registros.size} registros. No se puede deshacer.", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { viewModel.borrarTodo(); borrandoTodo = false }, modifier = Modifier.testTag("confirmar_borrar_todo")) {
                    Text("Borrar todo", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { borrandoTodo = false }) { Text("Cancelar", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }
}

@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, tag: String, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.heightIn(min = ALTO_TACTIL).testTag(tag)
    )
}

@Composable
private fun EstadoVacio(filtro: TipoHistorial?) {
    Card(modifier = Modifier.fillMaxWidth().testTag("historial_vacio")) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(56.dp))
            Text(
                when (filtro) {
                    null -> "Todavía no hay registros. Usa Escribir o Hablar y aparecerán aquí."
                    TipoHistorial.ESCRIBIR -> "No hay nada escuchado en Escribir."
                    TipoHistorial.HABLAR -> "No hay frases dichas en Hablar."
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun TarjetaHistorial(registro: RegistroHistorial, onRepetir: () -> Unit, onEliminar: () -> Unit) {
    val (icono, color) = estiloTipo(registro.tipo)
    val detalle = listOfNotNull(registro.tipo.etiqueta, FormatoHistorial.hora(registro.timestamp), registro.origen?.etiqueta)
    Card(
        modifier = Modifier.fillMaxWidth().testTag("registro_${registro.id}"),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // El ícono es decorativo: el tipo ya se lee en el texto de al lado
                Icon(icono, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(
                    detalle.joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                registro.texto,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 12.dp)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRepetir, modifier = Modifier.size(ALTO_TACTIL).testTag("repetir_${registro.id}")) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repetir en voz alta")
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(ALTO_TACTIL).testTag("eliminar_${registro.id}")) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar del historial", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Mismos colores que las tarjetas Escribir y Hablar del menú principal. */
@Composable
private fun estiloTipo(tipo: TipoHistorial): Pair<ImageVector, Color> = when (tipo) {
    TipoHistorial.ESCRIBIR -> Icons.Filled.EditNote to MaterialTheme.colorScheme.primaryContainer
    TipoHistorial.HABLAR -> Icons.AutoMirrored.Filled.VolumeUp to MaterialTheme.colorScheme.secondaryContainer
}

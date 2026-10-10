package cl.duoc.rulloa.accesiplus.ui.escribir

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.MostrarMensaje
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.components.TextoError
import cl.duoc.rulloa.accesiplus.ui.phrases.DialogoConfirmarEliminar
import cl.duoc.rulloa.accesiplus.ui.phrases.DialogoFrase
import cl.duoc.rulloa.accesiplus.ui.phrases.PantallaGrande
import cl.duoc.rulloa.accesiplus.ui.phrases.PhraseViewModel
import cl.duoc.rulloa.accesiplus.ui.phrases.TarjetaFrase

/**
 * Escribir: lo que dice la otra persona se convierte en texto grande (voz a texto),
 * o el usuario lo escribe con el teclado. Los textos se pueden guardar (CRUD en users/{uid}/phrases).
 */
@Composable
fun EscribirScreen(viewModel: PhraseViewModel, onVolver: () -> Unit) {
    val context = LocalContext.current
    val reconocedor = remember { ReconocedorVoz(context) }
    DisposableEffect(Unit) { onDispose { reconocedor.liberar() } }

    var texto by rememberSaveable { mutableStateOf("") }
    var editando by remember { mutableStateOf<Phrase?>(null) }
    var eliminando by remember { mutableStateOf<Phrase?>(null) }
    val frases by viewModel.frases.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val guardadas = viewModel.deCategoria(frases, Phrase.CATEGORIA_ESCRITA)
    val snackbar = remember { SnackbarHostState() }
    MostrarMensaje(snackbar, ui.mensaje) { viewModel.limpiarMensajes() }

    // ReconocedorVoz solo llama aquí con el resultado final (los parciales no pasan por esta lambda)
    val agregar: (String) -> Unit = { nuevo ->
        texto = if (texto.isBlank()) nuevo else "$texto $nuevo"
        viewModel.registrarEscrito(nuevo)
    }
    val pedirMicrofono = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) reconocedor.iniciar(agregar)
    }

    PantallaBase(titulo = "Escribir", onVolver = onVolver, snackbar = snackbar) {
        Text(
            "Toca \"Escuchar\" y acerca el teléfono a quien te habla. Verás sus palabras aquí.",
            style = MaterialTheme.typography.bodyLarge
        )

        // Texto transcrito en letra grande
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                when {
                    reconocedor.escuchando -> Text(
                        "Escuchando…", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    texto.isEmpty() -> Text("Aquí aparecerá el texto.", style = MaterialTheme.typography.bodyLarge)
                }
                val mostrado = listOf(texto, reconocedor.parcial).filter { it.isNotBlank() }.joinToString(" ")
                if (mostrado.isNotEmpty()) {
                    Text(mostrado, fontSize = 30.sp, lineHeight = 38.sp, modifier = Modifier.testTag("texto_transcrito"))
                }
            }
        }

        if (reconocedor.escuchando) {
            BotonGrande("Detener", onClick = reconocedor::detener, icono = Icons.Filled.MicOff)
        } else {
            BotonGrande(
                texto = if (reconocedor.disponible) "Escuchar (voz a texto)" else "Voz a texto no disponible",
                icono = Icons.Filled.Mic,
                habilitado = reconocedor.disponible,
                onClick = {
                    val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    if (concedido) reconocedor.iniciar(agregar) else pedirMicrofono.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.testTag("boton_escuchar")
            )
        }
        TextoError(reconocedor.error ?: ui.error)

        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it; reconocedor.limpiarError() },
            label = { Text("O escribe aquí con el teclado") },
            minLines = 2,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("campo_escribir")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonGrande(
                "Ver grande", onClick = { viewModel.mostrar(texto) }, icono = Icons.Filled.Fullscreen,
                habilitado = texto.isNotBlank(), modifier = Modifier.weight(1f)
            )
            BotonGrande(
                "Borrar", onClick = { texto = "" }, icono = Icons.Filled.Clear,
                habilitado = texto.isNotBlank(), secundario = true, modifier = Modifier.weight(1f)
            )
        }
        BotonGrande(
            "Guardar texto", icono = Icons.Filled.Save, habilitado = texto.isNotBlank(),
            onClick = { viewModel.crear(texto, Phrase.CATEGORIA_ESCRITA); texto = "" },
            modifier = Modifier.testTag("boton_guardar_escrito")
        )

        Text("Textos guardados (${guardadas.size})", style = MaterialTheme.typography.titleLarge)
        if (guardadas.isEmpty()) {
            Text("Todavía no guardas textos.", style = MaterialTheme.typography.bodyLarge)
        }
        guardadas.sortedByDescending { it.createdAt }.forEach { frase ->
            TarjetaFrase(
                frase = frase,
                onTocar = { viewModel.mostrar(frase.text) },
                onEditar = { editando = frase },
                onEliminar = { eliminando = frase },
                iconoTocar = { Icon(Icons.Filled.Fullscreen, contentDescription = "Ver en grande") }
            )
        }
    }

    editando?.let { f ->
        DialogoFrase("Editar texto", f.text, onGuardar = { viewModel.editar(f, it); editando = null }, onCancelar = { editando = null })
    }
    eliminando?.let { f ->
        DialogoConfirmarEliminar(
            "Se eliminará: \"${f.text}\"",
            onConfirmar = { viewModel.eliminar(f); eliminando = null },
            onCancelar = { eliminando = null }
        )
    }
    ui.enPantalla?.let { PantallaGrande(it, onCerrar = viewModel::ocultarPantalla) }
}

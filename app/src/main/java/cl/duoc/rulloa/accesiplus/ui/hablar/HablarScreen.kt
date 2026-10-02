package cl.duoc.rulloa.accesiplus.ui.hablar

import android.content.ActivityNotFoundException
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.data.model.FrasesRapidas
import cl.duoc.rulloa.accesiplus.data.model.Phrase
import cl.duoc.rulloa.accesiplus.data.tts.EstadoTts
import cl.duoc.rulloa.accesiplus.data.tts.TtsService
import cl.duoc.rulloa.accesiplus.domain.FiltrosFrases
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
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
 * Hablar: el teléfono dice en voz alta lo que el usuario escribe o elige (texto a voz).
 * @param fraseInicial frase que llega desde el widget para decirla al abrir.
 * @param colorCategoria color de cada categoría (lo calcula Palette en la fase de componentes).
 */
@Composable
fun HablarScreen(
    viewModel: PhraseViewModel,
    onVolver: () -> Unit,
    fraseInicial: String? = null,
    colorCategoria: (String) -> Color? = { null }
) {
    val context = LocalContext.current
    val frases by viewModel.frases.collectAsStateWithLifecycle()
    val estado by viewModel.estadoVoz.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf(FrasesRapidas.nombres.first()) }
    var creando by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Phrase?>(null) }
    var eliminando by remember { mutableStateOf<Phrase?>(null) }
    var inicialDicha by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    MostrarMensaje(snackbar, ui.mensaje) { viewModel.limpiarMensajes() }

    // Frase que viene del widget: se dice cuando el motor de voz terminó de iniciar
    LaunchedEffect(fraseInicial, estado) {
        if (fraseInicial != null && !inicialDicha && estado != EstadoTts.INICIANDO) {
            inicialDicha = true
            viewModel.hablar(fraseInicial, frases.firstOrNull { it.text == fraseInicial })
        }
    }

    val propias = frases.filter { it.category != Phrase.CATEGORIA_ESCRITA }

    PantallaBase(titulo = "Hablar", onVolver = onVolver, snackbar = snackbar) {
        EstadoVoz(estado, onInstalar = {
            // Algunos fabricantes no traen pantalla de instalación de voces
            try { context.startActivity(TtsService.intentInstalarVoces()) } catch (_: ActivityNotFoundException) { }
        })

        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Escribe lo que quieres decir") },
            minLines = 2,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("campo_hablar")
        )
        BotonGrande(
            "Decir en voz alta", icono = Icons.AutoMirrored.Filled.VolumeUp,
            habilitado = texto.isNotBlank(), onClick = { viewModel.hablar(texto) },
            modifier = Modifier.testTag("boton_hablar")
        )
        TextoError(ui.error)

        Text("Frases rápidas", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrasesRapidas.nombres.forEach { nombre ->
                val color = colorCategoria(nombre)
                FilterChip(
                    selected = categoria == nombre,
                    onClick = { categoria = nombre },
                    label = { Text(nombre, style = MaterialTheme.typography.bodyLarge) },
                    colors = if (color != null) FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color,
                        selectedLabelColor = Color.White
                    ) else FilterChipDefaults.filterChipColors(),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("categoria_$nombre")
                )
            }
        }
        FrasesRapidas.de(categoria).forEach { rapida ->
            val guardada = propias.firstOrNull { it.category == categoria && it.text == rapida }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorCategoria(categoria)?.copy(alpha = 0.16f)
                        ?: MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Text(rapida, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                    IconButton(onClick = { viewModel.hablar(rapida, guardada) }, modifier = Modifier.size(ALTO_TACTIL)) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Decir: $rapida")
                    }
                    IconButton(
                        onClick = {
                            if (guardada == null) viewModel.crear(rapida, categoria, favorita = true)
                            else viewModel.alternarFavorita(guardada)
                        },
                        modifier = Modifier.size(ALTO_TACTIL)
                    ) {
                        val fav = guardada?.favorite == true
                        Icon(
                            if (fav) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (fav) "Quitar de favoritas" else "Guardar como favorita",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        Text("Mis frases (${propias.size})", style = MaterialTheme.typography.titleLarge)
        BotonGrande(
            "Nueva frase", icono = Icons.Filled.Add, secundario = true,
            onClick = { creando = true }, modifier = Modifier.testTag("boton_nueva_frase")
        )
        if (propias.isEmpty()) Text("Crea frases propias o marca frases rápidas con la estrella.", style = MaterialTheme.typography.bodyLarge)
        propias.sortedWith(compareByDescending<Phrase> { it.favorite }.thenByDescending { it.createdAt }).forEach { frase ->
            Column {
                Text(frase.category, style = MaterialTheme.typography.labelMedium)
                TarjetaFrase(
                    frase = frase,
                    onTocar = { viewModel.hablar(frase.text, frase) },
                    onEditar = { editando = frase },
                    onEliminar = { eliminando = frase },
                    onFavorita = { viewModel.alternarFavorita(frase) }
                )
            }
        }
        FiltrosFrases.ultimaFavorita(propias)?.let {
            Text("Favorita del widget: \"${it.text}\"", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (creando) {
        DialogoFrase("Nueva frase", "", onGuardar = { viewModel.crear(it, Phrase.CATEGORIA_PROPIA); creando = false }, onCancelar = { creando = false })
    }
    editando?.let { f ->
        DialogoFrase("Editar frase", f.text, onGuardar = { viewModel.editar(f, it); editando = null }, onCancelar = { editando = null })
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

@Composable
private fun EstadoVoz(estado: EstadoTts, onInstalar: () -> Unit) {
    val (texto, aviso) = when (estado) {
        EstadoTts.INICIANDO -> "Preparando la voz…" to false
        EstadoTts.LISTO -> "Voz lista en español." to false
        EstadoTts.SIN_ESPANOL -> "No hay voz en español: se usará el idioma del teléfono." to true
        EstadoTts.SIN_MOTOR -> "Este teléfono no tiene voz instalada. Las frases se mostrarán en pantalla." to true
    }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("estado_voz"),
        colors = CardDefaults.cardColors(
            containerColor = if (aviso) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(texto, style = MaterialTheme.typography.bodyLarge)
            if (estado == EstadoTts.SIN_MOTOR || estado == EstadoTts.SIN_ESPANOL) {
                BotonGrande("Instalar voces", onClick = onInstalar, secundario = true)
            }
        }
    }
}

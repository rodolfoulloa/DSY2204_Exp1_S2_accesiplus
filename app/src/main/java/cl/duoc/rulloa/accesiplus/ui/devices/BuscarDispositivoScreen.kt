package cl.duoc.rulloa.accesiplus.ui.devices

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.fragment.compose.AndroidFragment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.data.model.Device
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.MostrarMensaje
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.components.TextoError
import cl.duoc.rulloa.accesiplus.ui.phrases.DialogoConfirmarEliminar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * BuscarDispositivo: el usuario registra sus dispositivos (audífono, implante, teléfono…)
 * y guarda la ubicación donde los dejó, para encontrarlos después en el mapa.
 * Al final muestra ConsejosFragment (vistas clásicas) integrado con AndroidFragment.
 * @param contenidoExtra contenido adicional opcional al final de la pantalla.
 */
@Composable
fun BuscarDispositivoScreen(
    viewModel: DeviceViewModel,
    onVolver: () -> Unit,
    contenidoExtra: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val dispositivos by viewModel.dispositivos.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Device?>(null) }
    var creando by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf<Device?>(null) }
    var pendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    MostrarMensaje(snackbar, ui.mensaje) { viewModel.limpiarMensajes() }

    // Permiso en tiempo de ejecución: basta con ubicación aproximada o precisa
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        val dispositivo = dispositivos.firstOrNull { it.id == pendiente }
        if (res.values.any { it } && dispositivo != null) viewModel.registrarUbicacion(dispositivo)
        else viewModel.permisoDenegado()
        pendiente = null
    }
    val registrar: (Device) -> Unit = { d ->
        if (tienePermiso(context)) viewModel.registrarUbicacion(d)
        else { pendiente = d.id; pedirPermiso.launch(PERMISOS_UBICACION) }
    }

    PantallaBase(titulo = "Buscar dispositivo", onVolver = onVolver, snackbar = snackbar) {
        Text(
            "Guarda dónde dejaste tu audífono, implante o teléfono. Toca \"Registrar ubicación\" cuando lo dejes en un lugar.",
            style = MaterialTheme.typography.bodyLarge
        )
        BotonGrande(
            "Agregar dispositivo", icono = Icons.Filled.Add,
            onClick = { creando = true }, modifier = Modifier.testTag("boton_agregar_dispositivo")
        )
        TextoError(ui.error)

        if (dispositivos.isEmpty()) {
            Text("Todavía no agregas dispositivos.", style = MaterialTheme.typography.bodyLarge)
        }
        dispositivos.forEach { d ->
            TarjetaDispositivo(
                dispositivo = d,
                ubicando = ui.ubicando == d.id,
                onRegistrar = { registrar(d) },
                onMapa = { if (!abrirMapa(context, d)) viewModel.sinAppMapas() },
                onEditar = { editando = d },
                onEliminar = { eliminando = d }
            )
        }
        // Fragment clásico dentro de Compose: los consejos dependen del tipo del primer dispositivo
        val tipo = dispositivos.firstOrNull()?.type ?: "Otro"
        key(tipo) {
            AndroidFragment<ConsejosFragment>(
                arguments = ConsejosFragment.argumentos(tipo),
                modifier = Modifier.fillMaxWidth().testTag("fragment_consejos")
            )
        }
        contenidoExtra()
    }

    if (creando || editando != null) {
        DialogoDispositivo(
            inicial = editando,
            onGuardar = { nombre, tipo, notas ->
                viewModel.guardar(editando?.id, nombre, tipo, notas)
                creando = false; editando = null
            },
            onCancelar = { creando = false; editando = null }
        )
    }
    eliminando?.let { d ->
        DialogoConfirmarEliminar(
            "Se eliminará \"${d.name}\" y su ubicación guardada.",
            onConfirmar = { viewModel.eliminar(d); eliminando = null },
            onCancelar = { eliminando = null }
        )
    }
}

private fun tienePermiso(context: Context) = PERMISOS_UBICACION.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

/** Abre la app de mapas con un intent geo: construido con la extensión toUri() de core-ktx. */
private fun abrirMapa(context: Context, d: Device): Boolean {
    val lat = d.lat ?: return false
    val lon = d.lon ?: return false
    val uri = "geo:$lat,$lon?q=$lat,$lon(${Uri.encode(d.name)})".toUri()
    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

private fun iconoTipo(tipo: String): ImageVector = when (tipo) {
    "Audífono", "Implante coclear" -> Icons.Filled.Hearing
    "Teléfono" -> Icons.Filled.PhoneAndroid
    "Tablet" -> Icons.Filled.Tablet
    else -> Icons.Filled.Devices
}

private val FORMATO_FECHA = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

@Composable
private fun TarjetaDispositivo(
    dispositivo: Device,
    ubicando: Boolean,
    onRegistrar: () -> Unit,
    onMapa: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("dispositivo_${dispositivo.name}")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(iconoTipo(dispositivo.type), contentDescription = null, modifier = Modifier.size(36.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(dispositivo.name, style = MaterialTheme.typography.titleMedium)
                    Text(dispositivo.type, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onEditar, modifier = Modifier.size(ALTO_TACTIL)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar ${dispositivo.name}")
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(ALTO_TACTIL)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${dispositivo.name}", tint = MaterialTheme.colorScheme.error)
                }
            }
            if (dispositivo.notes.isNotBlank()) Text(dispositivo.notes, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (dispositivo.tieneUbicacion) {
                    "Última ubicación: %.5f, %.5f\n%s".format(
                        Locale.US, dispositivo.lat, dispositivo.lon,
                        dispositivo.locationAt?.let { FORMATO_FECHA.format(Date(it)) }.orEmpty()
                    )
                } else "Sin ubicación registrada.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("ubicacion_${dispositivo.name}")
            )
            BotonGrande(
                if (ubicando) "Buscando ubicación…" else "Registrar ubicación",
                icono = Icons.Filled.MyLocation, onClick = onRegistrar, cargando = ubicando
            )
            BotonGrande(
                "Ver en mapa", icono = Icons.Filled.Map, onClick = onMapa,
                habilitado = dispositivo.tieneUbicacion, secundario = true
            )
        }
    }
}

@Composable
private fun DialogoDispositivo(
    inicial: Device?,
    onGuardar: (String, String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(inicial?.name.orEmpty()) }
    var tipo by remember { mutableStateOf(inicial?.type ?: Device.TIPOS.first()) }
    var notas by remember { mutableStateOf(inicial?.notes.orEmpty()) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (inicial == null) "Nuevo dispositivo" else "Editar dispositivo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre, onValueChange = { if (it.length <= 60) nombre = it },
                    label = { Text("Nombre (ej: Audífono derecho)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dispositivo_nombre")
                )
                Column(Modifier.selectableGroup()) {
                    Device.TIPOS.forEach { t ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(selected = t == tipo, onClick = { tipo = t }, role = Role.RadioButton),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = t == tipo, onClick = null)
                            Text(t, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = notas, onValueChange = { if (it.length <= 300) notas = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth().testTag("dispositivo_notas")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(nombre, tipo, notas) }, enabled = nombre.isNotBlank(),
                modifier = Modifier.testTag("dispositivo_guardar")
            ) { Text("Guardar", style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar", style = MaterialTheme.typography.labelLarge) } }
    )
}

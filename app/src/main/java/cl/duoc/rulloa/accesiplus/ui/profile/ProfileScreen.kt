package cl.duoc.rulloa.accesiplus.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.MostrarMensaje
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.components.TextoError

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onVolver: () -> Unit) {
    val perfil by viewModel.perfil.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var nombre by rememberSaveable { mutableStateOf("") }
    var pedirClave by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    MostrarMensaje(snackbar, ui.mensaje) { viewModel.limpiarMensajes() }

    // Carga el nombre guardado una vez que llega desde Firebase
    LaunchedEffect(perfil?.name) { if (nombre.isEmpty()) nombre = perfil?.name.orEmpty() }

    PantallaBase(titulo = "Mi perfil", onVolver = onVolver, snackbar = snackbar) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Correo", style = MaterialTheme.typography.labelMedium)
                Text(viewModel.correo, style = MaterialTheme.typography.bodyLarge)
                Text("Tipo de perfil", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                Text(perfil?.role ?: "—", style = MaterialTheme.typography.bodyLarge)
                Text("Género", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                Text(perfil?.gender ?: "—", style = MaterialTheme.typography.bodyLarge)
            }
        }
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("perfil_nombre")
        )
        TextoError(ui.error)
        BotonGrande(
            texto = "Guardar nombre",
            icono = Icons.Filled.Save,
            onClick = { viewModel.guardarNombre(nombre) },
            habilitado = nombre.isNotBlank() && nombre != perfil?.name,
            cargando = ui.cargando
        )
        BotonGrande(
            texto = "Cerrar sesión",
            icono = Icons.AutoMirrored.Filled.Logout,
            onClick = viewModel::cerrarSesion,
            secundario = true,
            modifier = Modifier.testTag("boton_cerrar_sesion")
        )
        BotonGrande(
            texto = "Eliminar mi cuenta",
            icono = Icons.Filled.DeleteForever,
            onClick = { pedirClave = true },
            secundario = true
        )
    }

    if (pedirClave) {
        var clave by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { pedirClave = false },
            title = { Text("¿Eliminar tu cuenta?") },
            text = {
                Column {
                    Text(
                        "Se borrarán tu perfil, tus frases y tus dispositivos. No se puede deshacer. " +
                            "Escribe tu contraseña para confirmar.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { pedirClave = false; viewModel.eliminarCuenta(clave) },
                    enabled = clave.isNotEmpty(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { pedirClave = false }) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

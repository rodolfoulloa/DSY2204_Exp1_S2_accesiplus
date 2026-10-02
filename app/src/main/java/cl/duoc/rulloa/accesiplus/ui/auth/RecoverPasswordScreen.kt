package cl.duoc.rulloa.accesiplus.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.components.TextoError

@Composable
fun RecoverPasswordScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val correoMal = correo.isNotEmpty() && !viewModel.isEmailValid(correo)
    val volver = { viewModel.limpiarMensajes(); onBackToLogin() }

    PantallaBase(titulo = "Recuperar contraseña", onVolver = volver) {
        Text(
            "Escribe el correo de tu cuenta. Te enviaremos un enlace para crear una nueva contraseña.",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; viewModel.limpiarMensajes() },
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = correoMal,
            supportingText = { if (correoMal) Text("Revisa el formato: nombre@correo.cl") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("campo_correo_recuperar")
        )
        TextoError(ui.error)
        ui.mensaje?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
            ) {
                Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
            }
        }
        BotonGrande(
            texto = "Enviar enlace",
            icono = Icons.Filled.Email,
            onClick = { viewModel.recuperar(correo) },
            habilitado = viewModel.isEmailValid(correo),
            cargando = ui.cargando
        )
        BotonGrande(texto = "Volver a iniciar sesión", onClick = volver, secundario = true)
    }
}

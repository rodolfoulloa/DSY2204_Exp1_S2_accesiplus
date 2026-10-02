package cl.duoc.rulloa.accesiplus.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.MostrarMensaje
import cl.duoc.rulloa.accesiplus.ui.components.TextoError

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onRegisterClick: () -> Unit,
    onRecoverPasswordClick: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    MostrarMensaje(snackbar, ui.mensaje) { viewModel.limpiarMensajes() }

    val correoMal = correo.isNotEmpty() && !viewModel.isEmailValid(correo)

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Filled.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    "AccesiPlus",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Escribe y habla en tu día a día",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; viewModel.limpiarMensajes() },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    isError = correoMal || ui.error != null,
                    supportingText = { if (correoMal) Text("Revisa el formato: nombre@correo.cl") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().testTag("campo_correo")
                )
                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it; viewModel.limpiarMensajes() },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = ui.error != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.login(correo, clave) }),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().testTag("campo_clave")
                )

                TextoError(ui.error)

                BotonGrande(
                    texto = "Iniciar sesión",
                    icono = Icons.AutoMirrored.Filled.Login,
                    onClick = { viewModel.login(correo, clave) },
                    habilitado = correo.isNotBlank() && clave.isNotEmpty() && !correoMal,
                    cargando = ui.cargando,
                    modifier = Modifier.testTag("boton_ingresar")
                )
                TextButton(
                    onClick = { viewModel.limpiarMensajes(); onRecoverPasswordClick() },
                    modifier = Modifier.heightIn(min = ALTO_TACTIL)
                ) { Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.bodyLarge) }
                TextButton(
                    onClick = { viewModel.limpiarMensajes(); onRegisterClick() },
                    modifier = Modifier.heightIn(min = ALTO_TACTIL).testTag("ir_registro")
                ) { Text("¿No tienes cuenta? Regístrate", style = MaterialTheme.typography.bodyLarge) }
            }
        }
    }
}

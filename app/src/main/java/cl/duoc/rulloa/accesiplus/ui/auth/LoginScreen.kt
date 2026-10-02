package cl.duoc.rulloa.accesiplus.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecoverPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsState()
    val successMessage by viewModel.authSuccessMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginError, successMessage) {
        loginError?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
        }
        successMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Accesibilidad Auditiva",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Entorno Cotidiano",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                isError = (loginError != null) || (email.isNotEmpty() && !viewModel.isEmailValid(email)),
                supportingText = {
                    if (email.isNotEmpty() && !viewModel.isEmailValid(email)) {
                        Text("Formato de correo inválido")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = loginError != null
            )

            if (loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (viewModel.login(email, password)) {
                        onLoginSuccess()
                    }
                },
                enabled = email.isNotEmpty() && password.isNotEmpty() && viewModel.isEmailValid(email),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Iniciar Sesión AccesipPlus")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRecoverPasswordClick) {
                Text("¿Olvidaste tu contraseña?")
            }

            TextButton(onClick = onRegisterClick) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}

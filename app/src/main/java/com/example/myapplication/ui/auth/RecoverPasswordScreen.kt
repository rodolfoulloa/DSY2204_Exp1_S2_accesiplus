package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun RecoverPasswordScreen(
    viewModel: AuthViewModel,
    onEmailSent: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val isEmailValid = viewModel.isEmailValid(email)
    val loginError by viewModel.loginError.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                text = "Recuperar Acceso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingresa tu correo institucional o personal para restablecer tu clave.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                isError = (email.isNotEmpty() && !isEmailValid) || loginError != null,
                supportingText = {
                    if (email.isNotEmpty() && !isEmailValid) {
                        Text("Formato de correo inválido")
                    } else if (loginError != null) {
                        Text(loginError ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (viewModel.recoverPassword(email)) {
                        onEmailSent()
                    } else {
                        // El error se actualizará en loginError
                    }
                },
                enabled = isEmailValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Instrucciones")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                viewModel.clearMessages()
                onBackToLogin()
            }) {
                Text("Volver al Login")
            }
        }
    }
}


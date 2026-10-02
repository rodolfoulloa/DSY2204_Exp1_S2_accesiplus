package cl.duoc.rulloa.accesiplus.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.rulloa.accesiplus.data.MockData
import cl.duoc.rulloa.accesiplus.data.CommunicationCategory
import cl.duoc.rulloa.accesiplus.data.AccessInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    var showDetailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Accesibilidad") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Categorías de Comunicación",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid de Categorías (Entorno Cotidiano)
            Box(modifier = Modifier.height(300.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(MockData.communicationCategories) { category ->
                        CategoryCard(category)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Estado de Herramientas de Apoyo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Requerimiento: LazyColumn para la tabla de estado
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
            ) {
                Column {
                    // Header Tabla
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp)
                    ) {
                        Text("Función", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("Estado", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }
                    
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(MockData.accessibilityStatus) { info ->
                            AccessibilityRow(info)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // REQUERIMIENTO S5: HERRAMIENTA INTERACTIVA DE COMUNICACIÓN (ESCRIBIR Y HABLAR)
            Text(
                text = "Herramienta de Comunicación Rápida (TTS)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var textToSpeak by remember { mutableStateOf("") }
                    var spokenTextDisplay by remember { mutableStateOf("") }
                    val tts = cl.duoc.rulloa.accesiplus.LocalTextToSpeech.current

                    OutlinedTextField(
                        value = textToSpeak,
                        onValueChange = { textToSpeak = it },
                        label = { Text("Escribe un mensaje para reproducir / comunicar") },
                        placeholder = { Text("Ej: Hola, necesito ayuda para encontrar la parada de bus.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (textToSpeak.isNotBlank()) {
                                try {
                                    spokenTextDisplay = textToSpeak
                                    tts?.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                    textToSpeak = ""
                                } catch (e: Exception) {
                                    android.util.Log.e("TTS_ERROR", "Fallo al reproducir audio", e)
                                }
                            }
                        },
                        enabled = textToSpeak.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reproducir Mensaje (Voz / Texto Grande)")
                    }



                    if (spokenTextDisplay.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "📢 COMUNICANDO EN PANTALLA:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = spokenTextDisplay,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    lineHeight = 28.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            
            // Requerimiento Semana 2: Link ACTIVO que ejecuta acción real
            TextButton(
                onClick = { showDetailDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Ver informe detallado completo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            confirmButton = {
                Button(onClick = { showDetailDialog = false }) {
                    Text("Entendido")
                }
            },
            title = { Text("Informe de Accesibilidad") },
            text = { 
                Text("Este informe contiene el estado detallado de todos los sensores y motores de síntesis de voz del dispositivo. Actualmente, el sistema está optimizado para un entorno cotidiano (Hogar y Trabajo).")
            },
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
    }
}

@Composable
fun CategoryCard(category: CommunicationCategory) {
    val icon = when (category.iconName) {
        "Medical" -> Icons.Default.Info
        "Shopping" -> Icons.Default.CheckCircle
        "Transport" -> Icons.Default.Info
        "Home" -> Icons.Default.Accessibility
        else -> Icons.Default.Accessibility
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = category.title, fontWeight = FontWeight.Bold)
            Text(text = category.description, fontSize = 12.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Prioridad: ${category.importance}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}



@Composable
fun AccessibilityRow(info: AccessInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(info.feature, modifier = Modifier.weight(1.5f))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (info.status == "Activo") Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (info.status == "Activo") Color(0xFF008000) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = info.status, fontSize = 14.sp)
        }
    }
}

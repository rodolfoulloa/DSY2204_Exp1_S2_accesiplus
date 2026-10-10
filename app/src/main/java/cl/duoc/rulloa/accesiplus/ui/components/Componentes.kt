package cl.duoc.rulloa.accesiplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.accesiplus.domain.EtapaAccion

/** Alto mínimo de los controles táctiles: 56dp (supera los 48dp que pide Material). */
val ALTO_TACTIL = 56.dp

/**
 * Acción para abrir la Ayuda desde la barra superior. La entrega el NavGraph solo con sesión
 * activa: así las pantallas no necesitan un parámetro extra y Login/Registro no la muestran.
 */
val LocalAbrirAyuda = staticCompositionLocalOf<(() -> Unit)?> { null }

/** Botón principal grande, de ancho completo. */
@Composable
fun BotonGrande(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    secundario: Boolean = false
) {
    val contenido: @Composable () -> Unit = {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        } else {
            if (icono != null) {
                // El texto del botón ya describe la acción: el ícono es decorativo
                Icon(icono, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.size(12.dp))
            }
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
    val mod = modifier.fillMaxWidth().heightIn(min = ALTO_TACTIL)
    if (secundario) {
        OutlinedButton(onClick = onClick, enabled = habilitado && !cargando, modifier = mod) {
            Row(verticalAlignment = Alignment.CenterVertically) { contenido() }
        }
    } else {
        Button(onClick = onClick, enabled = habilitado && !cargando, modifier = mod) {
            Row(verticalAlignment = Alignment.CenterVertically) { contenido() }
        }
    }
}

/** Aviso fijo cuando no hay internet. liveRegion hace que TalkBack lo anuncie. */
@Composable
fun BannerSinConexion(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag("banner_sin_conexion"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
        Text(
            "Sin conexión a internet. Los cambios se guardarán cuando vuelva la señal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

/**
 * Estructura común de las pantallas internas: barra superior con volver,
 * contenido desplazable y centrado con ancho máximo (se ve bien en tablet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBase(
    titulo: String,
    onVolver: (() -> Unit)?,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    acciones: @Composable () -> Unit = {},
    desplazable: Boolean = true,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver, modifier = Modifier.size(ALTO_TACTIL)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    acciones()
                    LocalAbrirAyuda.current?.let { abrirAyuda ->
                        IconButton(onClick = abrirAyuda, modifier = Modifier.size(ALTO_TACTIL).testTag("boton_ayuda")) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Ayuda")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val base = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
            Column(
                modifier = if (desplazable) base.verticalScroll(rememberScrollState()) else base.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = contenido
            )
        }
    }
}

/**
 * Indicación del siguiente paso (acción guiada). liveRegion hace que TalkBack la lea
 * cada vez que cambia la etapa; el ícono refuerza el estado sin depender del color.
 */
@Composable
fun GuiaPaso(etapa: EtapaAccion, mensaje: String, modifier: Modifier = Modifier) {
    val (icono, fondo) = when (etapa) {
        EtapaAccion.INICIO -> Icons.Filled.TouchApp to MaterialTheme.colorScheme.surfaceVariant
        EtapaAccion.ESCUCHANDO -> Icons.Filled.Hearing to MaterialTheme.colorScheme.primaryContainer
        EtapaAccion.PROCESANDO -> Icons.Filled.HourglassTop to MaterialTheme.colorScheme.primaryContainer
        EtapaAccion.LISTO -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.secondaryContainer
        EtapaAccion.ERROR -> Icons.Filled.ErrorOutline to MaterialTheme.colorScheme.tertiaryContainer
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(fondo, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
            .testTag("guia_paso"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // contentColorFor elige el color "on" del tema para ese fondo: mantiene el contraste en modo oscuro
        val texto = contentColorFor(fondo)
        Icon(icono, contentDescription = null, tint = texto, modifier = Modifier.size(32.dp))
        Text(mensaje, style = MaterialTheme.typography.bodyLarge, color = texto)
    }
}

/** Botón explícito para terminar y volver al menú principal (paso 8 del flujo). */
@Composable
fun BotonVolverMenu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    BotonGrande(
        texto = "Volver al menú",
        icono = Icons.Filled.Home,
        onClick = onClick,
        secundario = true,
        modifier = modifier.testTag("boton_volver_menu")
    )
}

/** Muestra un mensaje en el snackbar y lo marca como leído. */
@Composable
fun MostrarMensaje(snackbar: SnackbarHostState, mensaje: String?, onMostrado: () -> Unit) {
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            onMostrado()
        }
    }
}

/** Texto de error visible bajo un formulario (además del snackbar). */
@Composable
fun TextoError(texto: String?, modifier: Modifier = Modifier) {
    if (texto != null) {
        Text(
            texto,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Assertive }
                .testTag("texto_error")
        )
    }
}

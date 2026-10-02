package cl.duoc.rulloa.accesiplus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Sin "dynamic color": los colores del fondo de pantalla podrían bajar el contraste.
private val EsquemaClaro = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Papel,
    primaryContainer = AzulClaro,
    onPrimaryContainer = Tinta,
    secondary = VerdeAzulado,
    onSecondary = Papel,
    secondaryContainer = VerdeAzuladoClaro,
    onSecondaryContainer = Tinta,
    tertiary = AmbarAviso,
    tertiaryContainer = AmbarAvisoClaro,
    onTertiaryContainer = Tinta,
    background = Papel,
    onBackground = Tinta,
    surface = Papel,
    onSurface = Tinta,
    surfaceVariant = GrisSuperficie,
    onSurfaceVariant = Tinta,
    error = RojoError,
    onError = Papel
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulNoche,
    onPrimary = FondoNoche,
    primaryContainer = AzulNocheContenedor,
    onPrimaryContainer = TextoNoche,
    secondary = VerdeNoche,
    onSecondary = FondoNoche,
    tertiary = AmbarAvisoClaro,
    tertiaryContainer = AmbarAviso,
    onTertiaryContainer = TextoNoche,
    background = FondoNoche,
    onBackground = TextoNoche,
    surface = FondoNoche,
    onSurface = TextoNoche,
    surfaceVariant = SuperficieNoche,
    onSurfaceVariant = TextoNoche,
    error = RojoNoche,
    onError = FondoNoche
)

@Composable
fun AccesiPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}

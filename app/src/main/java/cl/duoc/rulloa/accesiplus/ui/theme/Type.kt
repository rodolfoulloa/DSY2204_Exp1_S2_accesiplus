package cl.duoc.rulloa.accesiplus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografía grande: el texto base es 20sp (Material usa 16sp) y escala con la
// configuración de tamaño de fuente del sistema porque se expresa en sp.
private val Base = Typography()

val Typography = Typography(
    displaySmall = Base.displaySmall.copy(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
    headlineLarge = Base.headlineLarge.copy(fontSize = 34.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold),
    headlineMedium = Base.headlineMedium.copy(fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineSmall = Base.headlineSmall.copy(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = Base.titleLarge.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    titleMedium = Base.titleMedium.copy(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
    bodySmall = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = Base.labelLarge.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    labelMedium = Base.labelMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
    labelSmall = Base.labelSmall.copy(fontSize = 14.sp, lineHeight = 20.sp)
)

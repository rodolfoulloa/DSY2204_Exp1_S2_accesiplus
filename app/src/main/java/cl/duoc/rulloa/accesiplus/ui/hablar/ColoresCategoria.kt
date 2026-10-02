package cl.duoc.rulloa.accesiplus.ui.hablar

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Target
import androidx.palette.graphics.get
import cl.duoc.rulloa.accesiplus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Colores de las categorías de frases rápidas obtenidos con Palette desde la imagen
 * de cada categoría. Se garantiza contraste suficiente con texto blanco (WCAG >= 4.5:1).
 */
object ColoresCategoria {

    @DrawableRes
    fun imagen(categoria: String): Int = when (categoria) {
        "Salud" -> R.drawable.categoria_salud
        "Compras" -> R.drawable.categoria_compras
        "Transporte" -> R.drawable.categoria_transporte
        "Hogar" -> R.drawable.categoria_hogar
        else -> R.drawable.categoria_trabajo
    }

    /** Calcula los colores fuera del hilo principal (Palette procesa el bitmap completo). */
    suspend fun calcular(context: Context, categorias: List<String>): Map<String, Color> =
        withContext(Dispatchers.Default) {
            categorias.associateWith { categoria ->
                val drawable = requireNotNull(ContextCompat.getDrawable(context, imagen(categoria)))
                // toBitmap() de core-ktx convierte el vector en bitmap
                val paleta = Palette.from(drawable.toBitmap(96, 96)).generate()
                // palette[Target] es el operador de palette-ktx
                val muestra = paleta[Target.VIBRANT] ?: paleta[Target.DARK_VIBRANT] ?: paleta.dominantSwatch
                Color(conContraste(muestra?.rgb ?: android.graphics.Color.DKGRAY))
            }
        }

    /** Oscurece el color hasta que el texto blanco tenga contraste 4.5:1. */
    fun conContraste(argb: Int, minimo: Double = 4.5): Int {
        var color = argb
        var paso = 0
        while (ColorUtils.calculateContrast(android.graphics.Color.WHITE, color) < minimo && paso < 10) {
            color = ColorUtils.blendARGB(color, android.graphics.Color.BLACK, 0.15f)
            paso++
        }
        return color
    }
}

package cl.duoc.rulloa.accesiplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Paso 8 del flujo: después de Hablar se vuelve al menú sin dejar destinos duplicados. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class VolverAlMenuTest {

    @get:Rule(order = 0) val sesion = ReglaSesion(iniciada = true)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun saludoGuiaYVolverAlMenuDespuesDeHablar() {
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_hablar"), 15_000)
        // Paso 2: el marcador de carga se reemplaza por el saludo con nombre
        compose.waitUntilDoesNotExist(hasTestTag("saludo_cargando"), 10_000)
        compose.onNodeWithTag("saludo_home").assertIsDisplayed()

        compose.onNodeWithTag("menu_hablar").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("guia_paso"), 10_000)
        // Antes de completar la acción no se ofrece volver al menú
        compose.onNodeWithTag("boton_volver_menu").assertDoesNotExist()

        compose.onNodeWithTag("campo_hablar").performScrollTo().performTextInput("Prueba volver al menú")
        compose.onNodeWithTag("boton_hablar").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("pantalla_grande"), 5_000)
        compose.onNodeWithContentDescription("Cerrar").performClick()

        compose.waitUntilAtLeastOneExists(hasTestTag("boton_volver_menu"), 10_000)
        compose.onNodeWithTag("boton_volver_menu").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_escribir"), 5_000)

        // Pila sin duplicados: desde el menú, "Atrás" cierra la app (no aparece otro menú ni Hablar)
        Espresso.pressBackUnconditionally()
        compose.waitForIdle()
        assertTrue(
            compose.activityRule.scenario.state == Lifecycle.State.DESTROYED ||
                compose.activity.isFinishing
        )
    }
}

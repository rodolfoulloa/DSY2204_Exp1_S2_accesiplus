package cl.duoc.rulloa.accesiplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Compose UI test en el emulador: login real con Firebase → HomeMenú → Hablar. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class LoginHablarTest {

    @get:Rule(order = 0) val sesion = ReglaSesion(iniciada = false)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginLlegaAlHomeYAbreHablar() {
        compose.waitUntilAtLeastOneExists(hasTestTag("campo_correo"), 15_000)
        compose.onNodeWithTag("campo_correo").performTextInput(ReglaSesion.correo)
        compose.onNodeWithTag("campo_clave").performTextInput(ReglaSesion.clave)
        compose.onNodeWithTag("boton_ingresar").performClick()

        // Firebase responde y la guardia de sesión navega al HomeMenú
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_hablar"), 20_000)
        compose.onNodeWithTag("saludo_home").assertIsDisplayed()
        compose.onNodeWithTag("menu_hablar").performClick()

        compose.waitUntilAtLeastOneExists(hasTestTag("estado_voz"), 10_000)
        compose.onNodeWithText("Hablar").assertIsDisplayed()
        compose.onNodeWithTag("campo_hablar").performScrollTo().performTextInput("Prueba automatizada")
        compose.onNodeWithTag("boton_hablar").performScrollTo().performClick()

        // La frase se muestra en pantalla grande (con o sin voz disponible)
        compose.waitUntilAtLeastOneExists(hasTestTag("pantalla_grande"), 5_000)
        compose.onNode(
            hasText("Prueba automatizada") and hasAnyAncestor(hasTestTag("pantalla_grande")),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    @Test
    fun loginConClaveIncorrectaMuestraError() {
        compose.waitUntilAtLeastOneExists(hasTestTag("campo_correo"), 15_000)
        compose.onNodeWithTag("campo_correo").performTextInput(ReglaSesion.correo)
        compose.onNodeWithTag("campo_clave").performTextInput("ClaveIncorrecta1")
        compose.onNodeWithTag("boton_ingresar").performClick()

        compose.waitUntilAtLeastOneExists(hasTestTag("texto_error"), 20_000)
        compose.onNodeWithText("Correo o contraseña incorrectos.").assertIsDisplayed()
    }
}

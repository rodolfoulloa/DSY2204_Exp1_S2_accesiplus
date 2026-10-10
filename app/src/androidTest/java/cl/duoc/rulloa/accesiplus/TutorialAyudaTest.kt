package cl.duoc.rulloa.accesiplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.rulloa.accesiplus.data.local.PreferenciasApp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Compose UI test: tutorial de primer inicio, Ayuda y "Ver tutorial nuevamente". */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class TutorialAyudaTest {

    @get:Rule(order = 0) val sesion = ReglaSesion(iniciada = true, tutorialVisto = false)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun primerInicioMuestraTutorialYComenzarLlegaAlMenu() {
        compose.waitUntilAtLeastOneExists(hasTestTag("tutorial"), 15_000)
        compose.onNodeWithContentDescription("Página 1 de 4").assertIsDisplayed()

        repeat(3) {
            compose.onNodeWithTag("boton_tutorial_siguiente").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithContentDescription("Página 4 de 4").assertIsDisplayed()
        compose.onNodeWithText("Comenzar").assertIsDisplayed()
        compose.onNodeWithTag("boton_tutorial_omitir").assertDoesNotExist()
        compose.onNodeWithTag("boton_tutorial_siguiente").performClick()

        compose.waitUntilAtLeastOneExists(hasTestTag("menu_escribir"), 10_000)
        compose.onNodeWithTag("tutorial").assertDoesNotExist()
        // La bandera quedó guardada para este usuario
        val uid = Firebase.auth.currentUser!!.uid
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(PreferenciasApp(contexto).tutorialVisto(uid))
    }

    @Test
    fun ayudaDesdeElMenuYVerTutorialNuevamente() {
        compose.waitUntilAtLeastOneExists(hasTestTag("tutorial"), 15_000)
        compose.onNodeWithTag("boton_tutorial_omitir").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_ayuda"), 10_000)

        compose.onNodeWithTag("menu_ayuda").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("ayuda_seccion_0"), 10_000)

        // Sección expandible: al tocarla aparece el paso a paso
        compose.onNodeWithText("Cómo usar Escribir").performClick()
        compose.onNodeWithText("1. En el menú, toca «Escribir».").assertIsDisplayed()

        compose.onNodeWithTag("boton_ver_tutorial").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("tutorial"), 5_000)
        compose.onNodeWithTag("boton_tutorial_omitir").performClick()

        // Omitir vuelve a la Ayuda, no al menú
        compose.waitUntilAtLeastOneExists(hasTestTag("boton_ver_tutorial"), 5_000)
    }

    @Test
    fun iconoDeAyudaEnLaBarraSuperior() {
        compose.waitUntilAtLeastOneExists(hasTestTag("tutorial"), 15_000)
        compose.onNodeWithTag("boton_tutorial_omitir").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_hablar"), 10_000)
        compose.onNodeWithTag("menu_hablar").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("boton_ayuda"), 10_000)
        compose.onNodeWithTag("boton_ayuda").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("ayuda_seccion_0"), 5_000)
        // Dentro de Ayuda no se repite el ícono de ayuda
        compose.onNodeWithTag("boton_ayuda").assertDoesNotExist()
    }
}

package cl.duoc.rulloa.accesiplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Sin sesión solo se ven Login/Registro/Recuperar; al cerrar sesión no se puede volver atrás. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NavegacionProtegidaTest {

    @get:Rule(order = 0) val sesion = ReglaSesion(iniciada = true)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun conSesionAbreElHome() {
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_escribir"), 15_000)
        compose.onNodeWithTag("menu_escribir").assertIsDisplayed()
        compose.onNodeWithTag("campo_correo").assertDoesNotExist()
    }

    @Test
    fun alCerrarSesionVuelveAlLoginYAtrasNoRegresaAlHome() {
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_escribir"), 15_000)

        // Se cierra la sesión desde fuera de la UI: la guardia debe reaccionar sola
        compose.runOnUiThread { Firebase.auth.signOut() }
        compose.waitUntilAtLeastOneExists(hasTestTag("boton_ingresar"), 10_000)
        compose.waitUntilDoesNotExist(hasTestTag("menu_escribir"), 5_000)

        // La pila quedó solo con Login: "Atrás" cierra la app en vez de mostrar el Home
        Espresso.pressBackUnconditionally()
        compose.waitForIdle()
        assertTrue(
            compose.activityRule.scenario.state == Lifecycle.State.DESTROYED ||
                compose.activity.isFinishing
        )
    }
}

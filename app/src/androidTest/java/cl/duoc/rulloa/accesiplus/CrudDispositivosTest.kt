package cl.duoc.rulloa.accesiplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CRUD de dispositivos de punta a punta (crear, editar, eliminar) contra Firebase real. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class CrudDispositivosTest {

    @get:Rule(order = 0) val sesion = ReglaSesion(iniciada = true)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    private val nombre = "Dispositivo UI Test"
    private val editado = "Dispositivo UI Editado"

    @Test
    fun crearEditarYEliminarDispositivo() {
        compose.waitUntilAtLeastOneExists(hasTestTag("menu_buscar"), 15_000)
        compose.onNodeWithTag("menu_buscar").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("boton_agregar_dispositivo"), 10_000)

        // Crear
        compose.onNodeWithTag("boton_agregar_dispositivo").performClick()
        compose.onNodeWithTag("dispositivo_nombre").performTextInput(nombre)
        compose.onNodeWithText("Tablet").performClick()
        compose.onNodeWithTag("dispositivo_guardar").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("dispositivo_$nombre"), 10_000)
        compose.onNodeWithTag("dispositivo_$nombre").performScrollTo().assertIsDisplayed()

        // Editar
        compose.onNodeWithContentDescription("Editar $nombre").performScrollTo().performClick()
        compose.onNodeWithTag("dispositivo_nombre").performTextClearance()
        compose.onNodeWithTag("dispositivo_nombre").performTextInput(editado)
        compose.onNodeWithTag("dispositivo_guardar").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag("dispositivo_$editado"), 10_000)
        compose.waitUntilDoesNotExist(hasTestTag("dispositivo_$nombre"), 5_000)

        // Eliminar (con confirmación)
        compose.onNodeWithContentDescription("Eliminar $editado").performScrollTo().performClick()
        compose.onNodeWithTag("confirmar_eliminar").performClick()
        compose.waitUntilDoesNotExist(hasTestTag("dispositivo_$editado"), 10_000)
        compose.waitUntilDoesNotExist(hasContentDescription("Eliminar $editado"), 5_000)
    }
}

package cl.duoc.rulloa.accesiplus.ui.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.rulloa.accesiplus.domain.Validaciones
import cl.duoc.rulloa.accesiplus.ui.components.ALTO_TACTIL
import cl.duoc.rulloa.accesiplus.ui.components.BotonGrande
import cl.duoc.rulloa.accesiplus.ui.components.PantallaBase
import cl.duoc.rulloa.accesiplus.ui.components.TextoError

private val ROLES = listOf("Usuario Final", "Cuidador", "Profesional de salud")
private val GENEROS = listOf("Femenino", "Masculino", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf(ROLES.first()) }
    var genero by rememberSaveable { mutableStateOf("Otro") }
    var acepta by rememberSaveable { mutableStateOf(false) }
    var menuAbierto by rememberSaveable { mutableStateOf(false) }
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    val correoMal = correo.isNotEmpty() && !viewModel.isEmailValid(correo)
    val claveMal = clave.isNotEmpty() && !viewModel.isPasswordValid(clave)
    val noCoinciden = confirmar.isNotEmpty() && confirmar != clave
    val formularioOk = Validaciones.isNameValid(nombre) && viewModel.isEmailValid(correo) &&
        viewModel.isPasswordValid(clave) && clave == confirmar && acepta
    val volver = { viewModel.limpiarMensajes(); onBackToLogin() }

    PantallaBase(titulo = "Crear cuenta", onVolver = volver) {
        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre completo") }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("registro_nombre")
        )
        OutlinedTextField(
            value = correo, onValueChange = { correo = it; viewModel.limpiarMensajes() },
            label = { Text("Correo electrónico") }, singleLine = true, isError = correoMal,
            supportingText = { if (correoMal) Text("Revisa el formato: nombre@correo.cl") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("registro_correo")
        )
        OutlinedTextField(
            value = clave, onValueChange = { clave = it },
            label = { Text("Contraseña (mínimo 6)") }, singleLine = true, isError = claveMal,
            supportingText = { if (claveMal) Text("Usa al menos ${Validaciones.LARGO_MIN_CLAVE} caracteres") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("registro_clave")
        )
        OutlinedTextField(
            value = confirmar, onValueChange = { confirmar = it },
            label = { Text("Repite la contraseña") }, singleLine = true, isError = noCoinciden,
            supportingText = { if (noCoinciden) Text("Las contraseñas no coinciden") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().testTag("registro_confirmar")
        )

        // Combo box: tipo de perfil
        ExposedDropdownMenuBox(expanded = menuAbierto, onExpandedChange = { menuAbierto = it }) {
            OutlinedTextField(
                value = rol, onValueChange = {}, readOnly = true,
                label = { Text("Tipo de perfil") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                ROLES.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, style = MaterialTheme.typography.bodyLarge) },
                        onClick = { rol = opcion; menuAbierto = false },
                        modifier = Modifier.heightIn(min = ALTO_TACTIL)
                    )
                }
            }
        }

        // Radio buttons: género. Toda la fila es tocable (no solo el círculo).
        Text("Género", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            GENEROS.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = ALTO_TACTIL)
                        .selectable(selected = opcion == genero, onClick = { genero = opcion }, role = Role.RadioButton),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = opcion == genero, onClick = null)
                    Text(opcion, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }

        // Checkbox: términos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ALTO_TACTIL)
                .toggleable(value = acepta, onValueChange = { acepta = it }, role = Role.Checkbox)
                .testTag("registro_terminos"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = acepta, onCheckedChange = null)
            Text(
                "Acepto que mis datos se guarden en mi cuenta",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        TextoError(ui.error)
        BotonGrande(
            texto = "Crear cuenta",
            icono = Icons.Filled.PersonAdd,
            onClick = { viewModel.registrar(nombre, correo, clave, rol, genero) },
            habilitado = formularioOk,
            cargando = ui.cargando,
            modifier = Modifier.testTag("boton_registrar")
        )
        BotonGrande(texto = "Ya tengo cuenta", onClick = volver, secundario = true)
    }
}

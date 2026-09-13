package com.example.myapplication.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage

    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun login(email: String, password: String): Boolean {
        val user = MockData.registeredUsers.find { it.email == email && it.password == password }
        return if (user != null) {
            _loginError.value = null
            true
        } else {
            _loginError.value = "Credenciales incorrectas. Verifique correo y contraseña."
            false
        }
    }

    fun registerUser(name: String, email: String, password: String, role: String, gender: String): Boolean {
        return try {
            if (MockData.registeredUsers.any { it.email == email }) {
                _loginError.value = "El correo ya está registrado."
                return false
            }
            val newId = (MockData.registeredUsers.maxOfOrNull { it.id } ?: 0) + 1
            val newUser = com.example.myapplication.data.User(newId, email, password, name, role, gender)
            MockData.registeredUsers.add(newUser)
            _authSuccessMessage.value = "¡Cuenta creada con éxito! Ya puedes iniciar sesión."
            true
        } catch (e: Exception) {
            _loginError.value = "Error inesperado al registrar el usuario: ${e.localizedMessage}"
            false
        }
    }


    fun recoverPassword(email: String): Boolean {
        val user = MockData.registeredUsers.find { it.email == email }
        return if (user != null) {
            _authSuccessMessage.value = "Instrucciones enviadas al correo $email"
            true
        } else {
            _loginError.value = "El correo ingresado no pertenece a ningún usuario."
            false
        }
    }

    fun clearMessages() {
        _loginError.value = null
        _authSuccessMessage.value = null
    }
}

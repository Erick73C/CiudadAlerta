package com.example.ciudadalerta.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *
 * ViewModel encargado de gestionar la lógica de inicio de sesión.
 * Conecta la UI con el repositorio de usuarios.
 * @author YO
 */
class LoginViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    // Estado del correo electrónico
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    // Estado de la contraseña
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Estado general de la pantalla (Loading, Success, Error)
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun onEmailChange(nuevoEmail: String) {
        _email.value = nuevoEmail
    }

    fun onPasswordChange(nuevaContrasena: String) {
        _password.value = nuevaContrasena
    }

    fun login() {
        // Validación básica
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _loginState.value = LoginState.Error("Por favor, llena todos los campos.")
            return
        }

        // Pasamos al estado de carga
        _loginState.value = LoginState.Loading

        // Lanzamos una corrutina para no bloquear la interfaz gráfica
        viewModelScope.launch {
            try {
                // Consultamos la base de datos a través del repositorio
                val usuario = repository.login(_email.value, _password.value)

                if (usuario != null) {
                    // Si el usuario existe, el login es exitoso
                    _loginState.value = LoginState.Success(usuario)
                } else {
                    // Si es null, las credenciales son incorrectas
                    _loginState.value = LoginState.Error("Correo o contraseña incorrectos.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Ocurrió un error inesperado.")
            }
        }
    }

    // Para limpiar el estado después de mostrar un error
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
package com.example.ciudadalerta.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.domain.models.ResultadoAuth
import com.example.ciudadalerta.domain.repository.AuthRepository
import com.example.ciudadalerta.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de inicio de sesión.
 * Conecta la UI con el [AuthRepository] (Supabase Auth) y restaura la sesión guardada.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Estado general de la pantalla (Idle, Loading, Success, Error)
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        // CLAVE: si ya hay una sesión guardada (se lee sin internet), entra directo sin pedir login
        authRepository.restaurarSesion()?.let { _loginState.value = LoginState.Success(it) }
    }

    fun onEmailChange(nuevoEmail: String) {
        _email.value = nuevoEmail
    }

    fun onPasswordChange(nuevaContrasena: String) {
        _password.value = nuevaContrasena
    }

    fun login() {
        val emailLimpio = _email.value.trim().lowercase()
        val passwordLimpia = _password.value.trim()

        if (emailLimpio.isBlank() || passwordLimpia.isBlank()) {
            _loginState.value = LoginState.Error("Por favor, llena todos los campos.")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            _loginState.value = when (val resultado = authRepository.iniciarSesion(emailLimpio, passwordLimpia)) {
                is ResultadoAuth.Exito -> LoginState.Success(resultado.dato)
                is ResultadoAuth.Error -> LoginState.Error(resultado.mensaje)
            }
        }
    }

    // Cierra la sesión: borra la sesión guardada y limpia el formulario
    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _email.value = ""
        _password.value = ""
        _loginState.value = LoginState.Idle
    }

    // Para limpiar el estado después de mostrar un error
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
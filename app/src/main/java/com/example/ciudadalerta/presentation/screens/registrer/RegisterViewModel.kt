package com.example.ciudadalerta.presentation.screens.registrer

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.domain.models.ResultadoAuth
import com.example.ciudadalerta.domain.repository.AuthRepository
import com.example.ciudadalerta.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de registro: valida los datos y crea la cuenta en Supabase
 * a través de [AuthRepository]. El rol siempre lo asigna el servidor (Ciudadano).
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChanged(nuevoNombre: String) {
        _uiState.update { it.copy(nombre = nuevoNombre, errorMessage = null) }
    }

    fun onEmailChanged(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail, errorMessage = null) }
    }

    fun onPasswordChanged(nuevaPassword: String) {
        _uiState.update { it.copy(password = nuevaPassword, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(nuevaConfirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = nuevaConfirmPassword, errorMessage = null) }
    }

    fun registrarUsuario() {
        val currentState = _uiState.value
        val emailLimpio = currentState.email.trim().lowercase()
        val passwordLimpia = currentState.password.trim()

        // 1. Validaciones de la UI
        when {
            currentState.nombre.isBlank() || emailLimpio.isBlank() || passwordLimpia.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos") }
                return
            }
            passwordLimpia != currentState.confirmPassword.trim() -> {
                _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
                return
            }
        }

        // 2. Registro en Supabase (los errores llegan ya traducidos a español)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val resultado = authRepository.registrar(currentState.nombre.trim(), emailLimpio, passwordLimpia)) {
                is ResultadoAuth.Exito ->
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is ResultadoAuth.Error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = resultado.mensaje) }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
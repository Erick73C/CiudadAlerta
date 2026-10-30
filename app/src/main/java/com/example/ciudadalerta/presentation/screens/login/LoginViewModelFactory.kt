package com.example.ciudadalerta.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ciudadalerta.domain.repository.AuthRepository
import com.example.ciudadalerta.domain.repository.UsuarioRepository

/**
 * Permite la inyección manual de dependencias inyectando el [UsuarioRepository]
 * dentro del constructor del ViewModel, sobreescribiendo la creación predeterminada.
 *
 */
class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
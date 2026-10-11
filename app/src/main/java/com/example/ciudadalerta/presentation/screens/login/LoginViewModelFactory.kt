package com.example.ciudadalerta.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ciudadalerta.domain.repository.UsuarioRepository

/**
 * Permite la inyección manual de dependencias inyectando el [UsuarioRepository]
 * dentro del constructor del ViewModel, sobreescribiendo la creación predeterminada.
 *
 * @property usuarioRepository Instancia del repositorio de usuarios para la capa de datos.
 * @author YO
 */
class LoginViewModelFactory(
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(usuarioRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
package com.example.ciudadalerta.presentation.screens.login

import com.example.ciudadalerta.data.local.entity.UsuarioEntity

/**
 *
 * Representa los diferentes estados visuales por los que puede pasar la pantalla de Login.
 * @author YO
 */
sealed class LoginState {
    object Idle : LoginState() // Estado inicial (inactivo)(mostramos un spinner)
    object Loading : LoginState() // Cargando
    data class Success(val usuario: UsuarioEntity) : LoginState() // Éxito (navegamos a la siguiente pantalla)
    data class Error(val message: String) : LoginState() // Error (mostramos un mensaje)
}
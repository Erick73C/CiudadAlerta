package com.example.ciudadalerta.presentation.screens.registrer


/**
 *
 * Representa los diferentes estados visuales por los que puede pasar la pantalla de Registro
 * @author YO
 */
data class RegisterUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rol: String = "usuario", // Rol asignado por defecto
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
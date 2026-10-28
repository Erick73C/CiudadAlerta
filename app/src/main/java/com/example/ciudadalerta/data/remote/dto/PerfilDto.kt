package com.example.ciudadalerta.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Fila de la tabla `perfiles` en Supabase.
 * El campo `rol` ("Ciudadano" o "Administrador") es lo que decide a qué pantalla entra el usuario.
 */
@Serializable
data class PerfilDto(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String
)
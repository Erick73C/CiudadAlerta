package com.example.ciudadalerta.domain.models


/**
 * Usuario que tiene la sesión iniciada. Reemplaza a UsuarioEntity en la navegación.
 * El `id` es el UUID de Supabase (el mismo de perfiles.id y reportes.usuario_id).
 */
data class UsuarioSesion(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String // "Ciudadano" o "Administrador"
)
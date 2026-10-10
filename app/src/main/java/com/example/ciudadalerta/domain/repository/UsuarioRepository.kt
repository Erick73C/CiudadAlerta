package com.example.ciudadalerta.domain.repository

import com.example.ciudadalerta.data.local.entity.UsuarioEntity


/**
 *
 * Interfaaz que define los casos de uso disponibles para la gestión de usuarios.
 * @author YO
 */
interface UsuarioRepository {
    suspend fun registrarUsuario(usuario: UsuarioEntity)
    suspend fun login(correo: String, contrasena: String): UsuarioEntity?
}
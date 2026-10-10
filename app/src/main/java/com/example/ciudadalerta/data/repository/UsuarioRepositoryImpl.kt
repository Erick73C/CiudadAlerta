package com.example.ciudadalerta.data.repository

import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import com.example.ciudadalerta.domain.repository.UsuarioRepository


/**
 *
 * Implementación del repositorio de usuarios utilizando Room (UsuarioDao) como fuente de datos.
 * @author YO
 */
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {

    //LLama a la  funcion de registrar usuario en la capa de datos
    override suspend fun registrarUsuario(usuario: UsuarioEntity) {
        usuarioDao.registrarUsuario(usuario)
    }

    override suspend fun login(correo: String, contrasena: String): UsuarioEntity? {
        return usuarioDao.login(correo, contrasena)
    }
}
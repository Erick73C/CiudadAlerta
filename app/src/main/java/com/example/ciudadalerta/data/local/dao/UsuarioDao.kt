package com.example.ciudadalerta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow


/**
 *
 * Define las operaciones de base de datos para la tabla de Usuarios.
 * @author Erick Omar Perez Gonzalez
 */
@Dao
interface UsuarioDao {
    // Inserta un nuevo usuario (Registro)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarUsuario(usuario: UsuarioEntity)

    // Consulta para el Login: Busca un usuario que coincida con correo y contraseña
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun login(correo: String, contrasena: String): UsuarioEntity?
}
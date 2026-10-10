package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * Representa la tabla de 'usuarios' en la base de datos local.
 * Contiene las credenciales y el rol (Ciudadano o Administrador).
 * @author YO
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val correo: String,
    val contrasena: String,
    val rol: String // "Ciudadano" o "Administrador"
)

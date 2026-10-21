package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *
 * Representa la tabla de 'usuarios' en la base de datos local.
 * Contiene las credenciales y el rol (Ciudadano o Administrador).
 * No acepta correos duplicados en la base de datos.
 * @author YO
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String // "Ciudadano" o "Administrador"
)

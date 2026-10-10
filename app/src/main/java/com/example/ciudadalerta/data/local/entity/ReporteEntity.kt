package com.example.ciudadalerta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * Representa la tabla de 'reportes' en la base de datos local.
 * Prepara el terreno para el modo Offline-First.
 * @author YO
 */
@Entity(tableName = "reportes")
data class ReporteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val hora: String,
    val requiereAtencionInmediata: Boolean,
    val estatus: String = "Pendiente",
    val estatusSincronizacion: String = "LOCAL",
    val usuarioId: Int
)
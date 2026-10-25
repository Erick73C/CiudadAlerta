package com.example.ciudadalerta.data.local.entity


import androidx.room.Embedded

/**
 * Resultado de unir un reporte con el nombre del ciudadano que lo creó.
 * NO ES UNA TABLA: Room la llena con el JOIN de ReporteDao.
 */
data class ReporteConUsuario(
    @Embedded val reporte: ReporteEntity,
    val nombreUsuario: String?
)

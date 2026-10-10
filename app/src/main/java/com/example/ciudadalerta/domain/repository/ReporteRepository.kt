package com.example.ciudadalerta.domain.repository

import com.example.ciudadalerta.data.local.entity.ReporteEntity
import kotlinx.coroutines.flow.Flow

/**
 *
 * Interfaz que define las operaciones de lectura y escritura para los reportes ciudadanos.
 * @author YO
 */
interface ReporteRepository {
    suspend fun insertarReporte(reporte: ReporteEntity)
    fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>>
    fun obtenerReportesPorUsuario(usuarioId: Int): Flow<List<ReporteEntity>>
}
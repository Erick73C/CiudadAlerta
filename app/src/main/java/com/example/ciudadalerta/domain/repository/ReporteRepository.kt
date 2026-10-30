package com.example.ciudadalerta.domain.repository

import com.example.ciudadalerta.data.local.entity.ReporteConUsuario
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
    fun obtenerReportesPorUsuario(usuarioId: String): Flow<List<ReporteEntity>>
    fun obtenerReportePorId(id: String): Flow<ReporteEntity?>
    suspend fun actualizarEstatus(id: String, estatus: String)
}
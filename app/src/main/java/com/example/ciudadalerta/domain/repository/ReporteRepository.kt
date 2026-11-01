package com.example.ciudadalerta.domain.repository

import com.example.ciudadalerta.data.local.entity.ReporteConUsuario
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.domain.models.ResultadoSync
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

    /** Sube a Supabase los reportes en estado LOCAL (primero la foto, luego el reporte). */
    suspend fun sincronizarPendientes(): ResultadoSync
    /** Envía los cambios de estatus hechos por el administrador. */
    suspend fun sincronizarEstatus(): ResultadoSync
    /** Baja los reportes del servidor (RLS decide cuáles: los propios o todos) y los guarda en Room. */
    suspend fun descargarReportes(): ResultadoSync
    /** Ejecuta las tres en orden: subir reportes, enviar estatus y descargar. Lo usa el Worker. */
    suspend fun sincronizarTodo(): ResultadoSync
}
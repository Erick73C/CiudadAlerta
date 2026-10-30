package com.example.ciudadalerta.data.repository

import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.entity.ReporteConUsuario
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.domain.repository.ReporteRepository
import kotlinx.coroutines.flow.Flow

/**
 *
 * Implementación del repositorio de reportes.
 * Aquí va ir  la lógica para decidir si enviar los datos
 * con Retrofit o guardarlos localmente en Room si no hay internet.
 * @author YO
 */
class ReporteRepositoryImpl(
    private val reporteDao: ReporteDao
) : ReporteRepository {

    override suspend fun insertarReporte(reporte: ReporteEntity) {
        // Por ahora solo guarda en local.
        reporteDao.insertarReporte(reporte)
    }

    override fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>> =
        reporteDao.obtenerTodosLosReportes()

    override fun obtenerReportesPorUsuario(usuarioId: String): Flow<List<ReporteEntity>> =
        reporteDao.obtenerReportesPorUsuario(usuarioId)

    override fun obtenerReportePorId(id: String): Flow<ReporteEntity?> =
        reporteDao.obtenerReportePorId(id)

    override suspend fun actualizarEstatus(id: String, estatus: String) {
        reporteDao.actualizarEstatus(id, estatus)
    }
}
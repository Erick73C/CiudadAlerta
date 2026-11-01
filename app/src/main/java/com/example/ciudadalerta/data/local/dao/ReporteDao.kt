package com.example.ciudadalerta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ciudadalerta.data.local.entity.ReporteConUsuario
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import kotlinx.coroutines.flow.Flow

/**
 *
 * Define las operaciones de base de datos para la tabla de Reportes.
 * Usa Flow para que la UI se actualice automáticamente cuando cambien los datos.
 * @author YO
 */
@Dao
interface ReporteDao {
    // Insertar un nuevo reporte
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReporte(reporte: ReporteEntity)

    //se ordena por fecha porque los UUID no tienen orden cronológico
    @Query("SELECT * FROM reportes ORDER BY fechaCreacion DESC")
    fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerReportesPorUsuario(usuarioId: String): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE id = :id LIMIT 1")
    fun obtenerReportePorId(id: String): Flow<ReporteEntity?>

    // El admin cambia el estatus: queda marcado para enviarse (PATCH).
    // Si el reporte sigue en LOCAL se conserva LOCAL: el POST ya lleva el estatus actual.
    @Query("""
        UPDATE reportes SET estatus = :estatus,
        estatusSincronizacion = CASE WHEN estatusSincronizacion = 'LOCAL'
                                     THEN 'LOCAL' ELSE 'ESTATUS_PENDIENTE' END
        WHERE id = :id
    """)
    suspend fun actualizarEstatus(id: String, estatus: String)

    // Sincronización: subida
    @Query("SELECT * FROM reportes WHERE estatusSincronizacion = 'LOCAL' ORDER BY fechaCreacion ASC")
    suspend fun obtenerPendientesDeEnvio(): List<ReporteEntity>

    @Query("SELECT * FROM reportes WHERE estatusSincronizacion = 'ESTATUS_PENDIENTE'")
    suspend fun obtenerEstatusPendientes(): List<ReporteEntity>

    @Query("UPDATE reportes SET estatusSincronizacion = :estado WHERE id = :id")
    suspend fun actualizarEstadoSync(id: String, estado: String)


    //Sincronización: descarga (no pisa lo local)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(reportes: List<ReporteEntity>)

    // Solo actualiza el estatus de filas ya sincronizadas; nunca pisa un cambio local pendiente.
    @Query("UPDATE reportes SET estatus = :estatus WHERE id = :id AND estatusSincronizacion = 'ENVIADO'")
    suspend fun actualizarEstatusDesdeServidor(id: String, estatus: String)
}
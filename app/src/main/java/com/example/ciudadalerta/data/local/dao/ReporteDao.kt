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

    @Query("UPDATE reportes SET estatus = :estatus WHERE id = :id")
    suspend fun actualizarEstatus(id: String, estatus: String)
}
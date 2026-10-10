package com.example.ciudadalerta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    // Obtener todos los reportes (Para el panel del Administrador)
    @Query("SELECT * FROM reportes ORDER BY id DESC")
    fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>>

    // Obtener reportes de un usuario en específico (Para la lista del Ciudadano)
    @Query("SELECT * FROM reportes WHERE usuarioId = :usuarioId ORDER BY id DESC")
    fun obtenerReportesPorUsuario(usuarioId: Int): Flow<List<ReporteEntity>>
}
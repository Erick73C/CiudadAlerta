package com.example.ciudadalerta.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.local.entity.UsuarioEntity

/**
 *
 * Configuración principal de la base de datos Room.
 * Integra las entidades y provee acceso a los DAOs.
 * Implementa el patrón Singleton para evitar múltiples instancias de la BD.
 * @author YO
 */
@Database(
    entities = [UsuarioEntity::class, ReporteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CiudadAlertaDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun reporteDao(): ReporteDao

    companion object {
        @Volatile
        private var INSTANCE: CiudadAlertaDatabase? = null

        fun getDatabase(context: Context): CiudadAlertaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CiudadAlertaDatabase::class.java,
                    "ciudad_alerta_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
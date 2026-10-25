package com.example.ciudadalerta.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.dao.UsuarioDao
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * Configuración principal de la base de datos Room.
 * Integra las entidades y provee acceso a los DAOs.
 * Implementa el patrón Singleton para evitar múltiples instancias de la BD.
 * @author YO
 */
@Database(
    entities = [UsuarioEntity::class, ReporteEntity::class],
    version = 5,
    exportSchema = false
)
abstract class CiudadAlertaDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun reporteDao(): ReporteDao

    companion object {
        @Volatile
        private var INSTANCE: CiudadAlertaDatabase? = null

        private val ADMIN_PREDETERMINADO = UsuarioEntity(
            nombre = "Administrador",
            correo = "admin@gmail.com",
            contrasena = "admin123",
            rol = "Administrador"
        )
        fun getDatabase(context: Context): CiudadAlertaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CiudadAlertaDatabase::class.java,
                    "ciudad_alerta_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance

                // Garantiza que siempre exista el admin (no duplica si ya está)
                CoroutineScope(Dispatchers.IO).launch {
                    instance.usuarioDao().insertarSiNoExiste(ADMIN_PREDETERMINADO)
                }
                instance
            }
        }
    }
}
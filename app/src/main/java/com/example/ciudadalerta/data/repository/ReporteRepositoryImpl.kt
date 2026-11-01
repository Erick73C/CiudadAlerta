package com.example.ciudadalerta.data.repository

import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.entity.EstadoSync
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.remote.SupabaseApi
import com.example.ciudadalerta.data.remote.SupabaseConfig
import com.example.ciudadalerta.data.remote.dto.EstatusUpdate
import com.example.ciudadalerta.data.remote.dto.ReporteDto
import com.example.ciudadalerta.domain.models.ResultadoSync
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.domain.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Implementación del repositorio de reportes (offline-first).
 * La UI siempre lee y escribe en Room; la sincronización con Supabase ocurre aparte
 * (la dispara el SyncWorker) y deja el resultado también en Room.
 */
class ReporteRepositoryImpl(
    private val reporteDao: ReporteDao,
    private val api: SupabaseApi,
    private val syncScheduler: SyncScheduler
) : ReporteRepository {

    // Local (lo que ya tenías)

    override suspend fun insertarReporte(reporte: ReporteEntity) {
        reporteDao.insertarReporte(reporte) // nace con estatusSincronizacion = LOCAL
        syncScheduler.solicitarSincronizacion()
    }

    override fun obtenerTodosLosReportes(): Flow<List<ReporteEntity>> =
        reporteDao.obtenerTodosLosReportes()

    override fun obtenerReportesPorUsuario(usuarioId: String): Flow<List<ReporteEntity>> =
        reporteDao.obtenerReportesPorUsuario(usuarioId)

    override fun obtenerReportePorId(id: String): Flow<ReporteEntity?> =
        reporteDao.obtenerReportePorId(id)

    override suspend fun actualizarEstatus(id: String, estatus: String) {
        reporteDao.actualizarEstatus(id, estatus) // el DAO lo marca ESTATUS_PENDIENTE
        syncScheduler.solicitarSincronizacion()
    }

    //  Sincronización

    override suspend fun sincronizarTodo(): ResultadoSync {
        var resultado = sincronizarPendientes()
        // sin internet no tiene caso seguir con las demás
        if (resultado == ResultadoSync.REINTENTAR) return resultado
        resultado = resultado.peor(sincronizarEstatus())
        if (resultado == ResultadoSync.REINTENTAR) return resultado
        // la descarga va al final para que lo local ya esté subido antes de comparar
        return resultado.peor(descargarReportes())
    }

    override suspend fun sincronizarPendientes(): ResultadoSync {
        var resultado = ResultadoSync.EXITO
        for (reporte in reporteDao.obtenerPendientesDeEnvio()) {
            val actual = subirReporte(reporte)
            if (actual == ResultadoSync.REINTENTAR) return actual
            resultado = resultado.peor(actual) // un FALLO no detiene a los demás reportes
        }
        return resultado
    }

    /** Sube la foto (si existe el archivo) y después el reporte. */
    private suspend fun subirReporte(reporte: ReporteEntity): ResultadoSync {
        try {
            var rutaRemota: String? = null
            val archivo = reporte.fotoPath?.let { File(it) }?.takeIf { it.exists() }
            if (archivo != null) {
                // la ruta es determinista (usuario/idReporte.jpg): reintentar no duplica fotos
                val ruta = "${reporte.usuarioId}/${reporte.id}.jpg"
                val respuestaFoto = api.subirFoto(
                    SupabaseConfig.BUCKET_FOTOS,
                    ruta,
                    archivo.asRequestBody("image/jpeg".toMediaType())
                )
                val resultadoFoto = respuestaFoto.aResultado()
                if (resultadoFoto != ResultadoSync.EXITO) return resultadoFoto
                rutaRemota = ruta
            }
            val resultado = api.crearReporte(reporte.aDto(rutaRemota)).aResultado()
            if (resultado == ResultadoSync.EXITO) {
                reporteDao.actualizarEstadoSync(reporte.id, EstadoSync.ENVIADO)
            }
            return resultado
        } catch (e: IOException) {
            return ResultadoSync.REINTENTAR
        }
    }

    override suspend fun sincronizarEstatus(): ResultadoSync {
        var resultado = ResultadoSync.EXITO
        try {
            for (reporte in reporteDao.obtenerEstatusPendientes()) {
                val respuesta = api.actualizarEstatus("eq.${reporte.id}",
                    EstatusUpdate(reporte.estatus)
                )
                if (respuesta.isSuccessful) {
                    // lista vacía = RLS bloqueó el cambio en silencio (no es administrador)
                    if (respuesta.body().isNullOrEmpty()) {
                        resultado = resultado.peor(ResultadoSync.FALLO)
                    } else {
                        reporteDao.marcarEstatusEnviado(reporte.id, reporte.estatus)
                    }
                } else {
                    val actual = respuesta.aResultado()
                    if (actual == ResultadoSync.REINTENTAR) return actual
                    resultado = resultado.peor(actual)
                }
            }
        } catch (e: IOException) {
            return ResultadoSync.REINTENTAR
        }
        return resultado
    }

    override suspend fun descargarReportes(): ResultadoSync {
        try {
            val respuesta = api.obtenerReportes()
            val remotos = respuesta.body()
            if (!respuesta.isSuccessful) return respuesta.aResultado()
            if (remotos == null) return ResultadoSync.FALLO

            // 1) los que no existen en Room se insertan como ENVIADO; los existentes no se tocan
            reporteDao.insertarSiNoExiste(remotos.map { it.aEntidad() })
            // 2) los ya sincronizados refrescan su estatus (lo pendiente del admin no se pisa)
            remotos.forEach { reporteDao.actualizarEstatusDesdeServidor(it.id, it.estatus) }
            return ResultadoSync.EXITO
        } catch (e: IOException) {
            return ResultadoSync.REINTENTAR
        }
    }

    // Auxiliares
    /**
     * Traduce la respuesta HTTP al resultado de sincronización.
     */
    private fun Response<*>.aResultado(): ResultadoSync {
        if (isSuccessful) return ResultadoSync.EXITO
        val cuerpo = errorBody()?.string().orEmpty()
        return when {
            "23505" in cuerpo || "Duplicate" in cuerpo -> ResultadoSync.EXITO
            code() == 401 || code() == 429 || code() >= 500 -> ResultadoSync.REINTENTAR
            else -> ResultadoSync.FALLO
        }
    }

    private fun ReporteEntity.aDto(rutaFoto: String?) = ReporteDto(
        id = id,
        usuarioId = usuarioId,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        horaIncidente = hora,
        requiereAtencionInmediata = requiereAtencionInmediata,
        estatus = estatus,
        latitud = latitud,
        longitud = longitud,
        fotoPath = rutaFoto,
        fechaCreacion = Instant.ofEpochMilli(fechaCreacion).toString()
    )

    private fun ReporteDto.aEntidad() = ReporteEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        hora = horaIncidente,
        requiereAtencionInmediata = requiereAtencionInmediata,
        estatus = estatus,
        estatusSincronizacion = EstadoSync.ENVIADO,
        usuarioId = usuarioId,
        nombreUsuario = perfiles?.nombre,
        latitud = latitud,
        longitud = longitud,
        fotoPath = fotoPath, // ruta del bucket, no un archivo local
        fechaCreacion = fechaCreacion
            ?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }
            ?: System.currentTimeMillis()
    )
}
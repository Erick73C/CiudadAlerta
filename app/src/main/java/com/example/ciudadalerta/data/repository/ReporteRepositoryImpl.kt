package com.example.ciudadalerta.data.repository

import com.example.ciudadalerta.data.local.dao.ReporteDao
import com.example.ciudadalerta.data.local.entity.EstadoSync
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.data.remote.SupabaseApi
import com.example.ciudadalerta.data.remote.SupabaseConfig
import com.example.ciudadalerta.data.remote.dto.EstatusUpdate
import com.example.ciudadalerta.data.remote.dto.ReporteDto
import com.example.ciudadalerta.domain.models.EstadoRed
import com.example.ciudadalerta.domain.models.ResultadoFoto
import com.example.ciudadalerta.domain.models.ResultadoSync
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.domain.sync.ConexionObserver
import com.example.ciudadalerta.domain.sync.SyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
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
    private val syncScheduler: SyncScheduler,
    private val conexionObserver: ConexionObserver,
    private val carpetaCache: File,
    private val renovarSesion: suspend () -> Boolean
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


    override val estadoRed: Flow<EstadoRed> = combine(
        conexionObserver.conectado,
        syncScheduler.sincronizando.mantenerVisible()
    ) { conectado, sincronizando ->
        when {
            !conectado -> EstadoRed.SIN_CONEXION
            sincronizando -> EstadoRed.SINCRONIZANDO
            else -> EstadoRed.EN_LINEA
        }
    }

    override fun sincronizarAhora() = syncScheduler.solicitarSincronizacion()

    /** Una sincronización dura milisegundos: se mantiene 1.2 s el "true" para que el usuario alcance a verlo. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<Boolean>.mantenerVisible(): Flow<Boolean> = transformLatest { activo ->
        if (!activo) delay(1_200)
        emit(activo)
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

    override suspend fun obtenerFoto(fotoPath: String): ResultadoFoto = withContext(Dispatchers.IO) {
        // 1) Ruta absoluta = archivo de este mismo teléfono
        if (fotoPath.startsWith("/")) {
            val archivo = File(fotoPath)
            return@withContext if (archivo.exists()) ResultadoFoto.Lista(archivo.absolutePath)
            else ResultadoFoto.NoDisponible
        }

        // 2) Ruta del bucket: si ya se descargó antes, se reutiliza sin red
        val destino = File(carpetaCache, fotoPath.replace('/', '_'))
        if (destino.exists() && destino.length() > 0) {
            return@withContext ResultadoFoto.Lista(destino.absolutePath)
        }

        try {
            renovarSesion() // el bucket es privado: hace falta un token vigente
            val respuesta = api.descargarFoto(SupabaseConfig.BUCKET_FOTOS, fotoPath)
            val cuerpo = respuesta.body()
            if (!respuesta.isSuccessful || cuerpo == null) return@withContext ResultadoFoto.NoDisponible

            carpetaCache.mkdirs()
            // CLAVE: se escribe a un .tmp y luego se renombra, para no dejar una foto a medias si se corta la red
            val temporal = File(carpetaCache, destino.name + ".tmp")
            cuerpo.use { c -> temporal.outputStream().use { salida -> c.byteStream().copyTo(salida) } }
            temporal.renameTo(destino)
            ResultadoFoto.Lista(destino.absolutePath)
        } catch (e: IOException) {
            ResultadoFoto.SinConexion
        }
    }
}
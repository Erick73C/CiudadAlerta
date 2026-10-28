package com.example.ciudadalerta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fila de la tabla `reportes` en Supabase.
 * Se usa tanto para enviar un reporte (POST) como para leerlo (GET).
 *
 * - `id`: UUID generado por la app, para que reintentar una subida no duplique el reporte.
 * - `fotoPath`: ruta dentro del bucket (ej. "uid/idReporte.jpg"), no una URL.
 * - `fechaCreacion`: texto ISO-8601. Al enviar: Instant.ofEpochMilli(ms).toString().
 *   Al leer: OffsetDateTime.parse(texto).toInstant().toEpochMilli().
 */
@Serializable
data class ReporteDto(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    @SerialName("hora_incidente") val horaIncidente: String,
    @SerialName("requiere_atencion_inmediata") val requiereAtencionInmediata: Boolean,
    val estatus: String = "Pendiente",
    val latitud: Double? = null,
    val longitud: Double? = null,
    @SerialName("foto_path") val fotoPath: String? = null,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null,
    // CLAVE: solo llega en lecturas con select=*,perfiles(nombre). Al enviar queda null y se omite.
    val perfiles: PerfilNombreDto? = null
)

/** Nombre de quien creó el reporte (resultado del JOIN con `perfiles`). */
@Serializable
data class PerfilNombreDto(
    val nombre: String
)

/** Cuerpo del PATCH que cambia únicamente el estatus de un reporte (RF-08). */
@Serializable
data class EstatusUpdate(
    val estatus: String
)

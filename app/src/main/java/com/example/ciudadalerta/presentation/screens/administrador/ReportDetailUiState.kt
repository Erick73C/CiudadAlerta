package com.example.ciudadalerta.presentation.screens.administrador

/** Estatus que el administrador puede asignar (los filtros menos "Todos"). */
val estatusReporte = filtrosEstatus.filter { it != FILTRO_TODOS }

/** Datos del reporte tal como los muestra la pantalla de detalle. */
data class ReportDetailUi(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val estatus: String,
    val fecha: String,
    val horaIncidente: String,
    val nombreUsuario: String,
    val urgente: Boolean,
    val fotoPath: String?,
    val latitud: Double?,
    val longitud: Double?
)

data class ReportDetailUiState(
    val reporte: ReportDetailUi? = null,
    val estatusSeleccionado: String? = null, // lo que el admin eligió, aún sin guardar
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null
)
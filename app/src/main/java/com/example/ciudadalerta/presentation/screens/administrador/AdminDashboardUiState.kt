package com.example.ciudadalerta.presentation.screens.administrador

const val FILTRO_TODOS = "Todos"
val filtrosEstatus = listOf(FILTRO_TODOS, "Pendiente", "En Revisión", "Resuelto")

/** Modelo de un reporte tal como lo muestra el panel del administrador. */
data class AdminReporteUi(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val estatus: String,
    val fecha: String,
    val nombreUsuario: String,
    val urgente: Boolean
)

data class AdminDashboardUiState(
    val reportes: List<AdminReporteUi> = emptyList(), // ya filtrados
    val filtro: String = FILTRO_TODOS,
    val conteos: Map<String, Int> = emptyMap(),       // cantidad por estatus
    val total: Int = 0,
    val cargando: Boolean = true
)
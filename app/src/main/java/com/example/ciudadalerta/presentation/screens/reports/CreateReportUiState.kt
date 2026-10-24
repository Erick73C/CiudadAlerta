package com.example.ciudadalerta.presentation.screens.reports

import com.example.ciudadalerta.domain.models.Ubicacion


val categoriasReporte = listOf("Bache", "Alumbrado", "Fuga de Agua", "Basura", "Otro")

data class CreateReportUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = categoriasReporte[0],
    val requiereAtencionInmediata: Boolean = false,
    val hora: String? = null,
    val fotoPath: String? = null,
    val ubicacion: Ubicacion? = null,
    val obteniendoUbicacion: Boolean = false,
    val guardando: Boolean = false,
    val errorMessage: String? = null,
    val guardadoExitoso: Boolean = false
)

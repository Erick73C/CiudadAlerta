package com.example.ciudadalerta.domain.models

data class reporte(
    var id: String,
    var informacion: String,
    var region: String,
    val descripcion: String,
    val categoria: String,
    val hora: String,
    val requiereAtencionInmediata: Boolean,
    val estatus: String = "Pendiente",
    val estatusSincronizacion: String = "LOCAL",
    val usuarioId: Int
)

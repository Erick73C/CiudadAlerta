package com.example.ciudadalerta.presentation.util

import androidx.compose.ui.graphics.Color

/** Color asociado a cada estatus de un reporte. */
fun colorPorEstatus(estatus: String): Color = when (estatus) {
    "Pendiente" -> Color(0xFFE53935)   // Rojo
    "En Revisión" -> Color(0xFFFDD835) // Amarillo
    "Resuelto" -> Color(0xFF43A047)    // Verde
    else -> Color.Gray
}
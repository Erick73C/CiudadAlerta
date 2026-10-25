package com.example.ciudadalerta.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/** Convierte un timestamp en milisegundos a "25/10/2026 13:35" en la zona horaria del dispositivo. */
fun Long.aFechaHora(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(formatoFechaHora)
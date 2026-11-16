package com.example.ciudadalerta.domain.models

/** Resultado de pedir la foto de un reporte. */
sealed interface ResultadoFoto {
    /** La foto ya está en un archivo local listo para mostrarse. */
    data class Lista(val ruta: String) : ResultadoFoto
    /** No se pudo descargar por falta de internet: se puede reintentar. */
    data object SinConexion : ResultadoFoto
    /** El archivo no existe o el servidor no lo entregó. */
    data object NoDisponible : ResultadoFoto
}
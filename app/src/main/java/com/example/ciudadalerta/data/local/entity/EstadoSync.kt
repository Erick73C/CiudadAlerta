package com.example.ciudadalerta.data.local.entity

/** Valores de ReporteEntity.estatusSincronizacion. */
object EstadoSync {
    const val LOCAL = "LOCAL"                       // creado en el teléfono, aún no está en Supabase
    const val ENVIADO = "ENVIADO"                   // ya existe en Supabase
    const val ESTATUS_PENDIENTE = "ESTATUS_PENDIENTE" // el admin cambió el estatus y falta el PATCH
}

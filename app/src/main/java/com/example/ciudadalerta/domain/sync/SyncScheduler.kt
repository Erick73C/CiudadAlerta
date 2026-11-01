package com.example.ciudadalerta.domain.sync

/**
 * Contrato para pedir sincronizaciones en segundo plano.
 * Vive en el dominio para que el repositorio no dependa de WorkManager.
 */
interface SyncScheduler {
    /** Encola una sincronización que se ejecuta en cuanto haya internet. */
    fun solicitarSincronizacion()

    /** Programa la sincronización periódica (cada 15 min con internet). Es seguro llamarla muchas veces. */
    fun programarPeriodica()
}
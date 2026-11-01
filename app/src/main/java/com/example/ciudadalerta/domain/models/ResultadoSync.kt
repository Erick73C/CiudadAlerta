package com.example.ciudadalerta.domain.models

/**
 * Resultado de una operación de sincronización. El Worker lo traduce a su propio resultado:
 * EXITO -> success, REINTENTAR -> retry (con backoff), FALLO -> failure (no se reintenta solo).
 */
enum class ResultadoSync {
    EXITO,
    REINTENTAR, // sin internet, token vencido (401), 429 o error 5xx: puede funcionar más tarde
    FALLO;      // error 4xx permanente: reintentar igual no lo arregla

    /** Devuelve el peor de dos resultados (el orden del enum es la gravedad). */
    fun peor(otro: ResultadoSync): ResultadoSync = if (otro.ordinal > ordinal) otro else this
}
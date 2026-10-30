package com.example.ciudadalerta.domain.models

/**
 * Resultado de una operación de autenticación: salió bien (con su dato) o falló (con un mensaje
 * listo para mostrar al usuario).
 */
sealed class ResultadoAuth<out T> {
    data class Exito<out T>(val dato: T) : ResultadoAuth<T>()
    data class Error(val mensaje: String) : ResultadoAuth<Nothing>()
}
package com.example.ciudadalerta.domain.repository

import com.example.ciudadalerta.domain.models.ResultadoAuth
import com.example.ciudadalerta.domain.models.UsuarioSesion

/** Casos de uso de autenticación contra la nube y manejo de la sesión del dispositivo. */
interface AuthRepository {

    /** Crea una cuenta de ciudadano. No deja sesión iniciada: después hay que iniciar sesión. */
    suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth<Unit>

    /** Inicia sesión, guarda la sesión en el dispositivo y devuelve el usuario con su rol. */
    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth<UsuarioSesion>

    /** Usuario guardado en el dispositivo, o null si no hay sesión. NO usa internet (offline-first). */
    fun restaurarSesion(): UsuarioSesion?

    /**
     * Renueva el token si está por vencer. Devuelve true si queda un token vigente.
     * Sin internet devuelve false pero CONSERVA la sesión, para reintentar después.
     */
    suspend fun renovarSesionSiExpiro(): Boolean

    /** Borra la sesión del dispositivo. */
    fun cerrarSesion()
}
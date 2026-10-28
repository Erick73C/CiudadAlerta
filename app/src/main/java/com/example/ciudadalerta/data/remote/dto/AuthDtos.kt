package com.example.ciudadalerta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Datos extra que se guardan junto al usuario al registrarse.
 * Supabase los deja en raw_user_meta_data, y el trigger crear_perfil() los lee para llenar `perfiles`.
 */
@Serializable
data class UserMetadata(
    val nombre: String //El trigger busca exactamente esta llave ("nombre")
)

/**
 * Cuerpo de POST /auth/v1/signup (registro de un ciudadano).
 * El rol NO se envía: el servidor siempre crea el perfil como "Ciudadano".
 */
@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: UserMetadata //Supabase llama "data" al bloque de metadatos
)

/** Cuerpo de POST /auth/v1/token?grant_type=password (inicio de sesión). */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/** Cuerpo de POST /auth/v1/token?grant_type=refresh_token (renovar sesión sin pedir contraseña). */
@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String // @SerialName traduce snake_case del JSON a camelCase de Kotlin
)

/**
 * Respuesta de registro, inicio y renovación de sesión.
 * Requiere que "Confirm email" esté apagado: si estuviera encendido, el registro no
 * devolvería tokens y la lectura fallaría.
 */
@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,   //JWT que se envía en cada petición; dura ~1 hora
    @SerialName("refresh_token") val refreshToken: String, //sirve para obtener un access_token nuevo
    @SerialName("expires_in") val expiresIn: Long,         // segundos de vida del access_token
    val user: AuthUser
)

/** Usuario autenticado. Su `id` (UUID) es el mismo que `perfiles.id` y `reportes.usuario_id`. */
@Serializable
data class AuthUser(
    val id: String,
    val email: String? = null
)

/**
 * Forma de los errores que devuelve Supabase (Auth y PostgREST usan campos distintos).
 * Solo se declaran los campos útiles; el resto se ignora (ignoreUnknownKeys).
 * Ejemplo de login incorrecto: error_code = "invalid_credentials".
 */
@Serializable
data class SupabaseErrorDto(
    @SerialName("error_code") val errorCode: String? = null,
    val msg: String? = null,
    val message: String? = null,
    @SerialName("error_description") val errorDescription: String? = null
) {
    /** Primer mensaje disponible, para mostrarlo o registrarlo. */
    fun mensajeLegible(): String =
        msg ?: message ?: errorDescription ?: "Error desconocido"
}
package com.example.ciudadalerta.data.repository

import com.example.ciudadalerta.data.local.SessionManager
import com.example.ciudadalerta.data.remote.RetrofitClient
import com.example.ciudadalerta.data.remote.SupabaseApi
import com.example.ciudadalerta.data.remote.dto.LoginRequest
import com.example.ciudadalerta.data.remote.dto.RefreshRequest
import com.example.ciudadalerta.data.remote.dto.SignUpRequest
import com.example.ciudadalerta.data.remote.dto.SupabaseErrorDto
import com.example.ciudadalerta.data.remote.dto.UserMetadata
import com.example.ciudadalerta.domain.models.ResultadoAuth
import com.example.ciudadalerta.domain.models.UsuarioSesion
import com.example.ciudadalerta.domain.repository.AuthRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException

/**
 * Implementación de [AuthRepository] con Supabase Auth (vía Retrofit) y [SessionManager].
 * Traduce errores técnicos (códigos HTTP, sin internet) a mensajes en español para la UI.
 */
class AuthRepositoryImpl(
    private val api: SupabaseApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    // El refresh_token solo se puede usar una vez; este candado evita dos renovaciones a la vez
    private val candadoRenovacion = Mutex()

    override suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String
    ): ResultadoAuth<Unit> = ejecutar {
        val respuesta = api.registrar(SignUpRequest(correo, contrasena, UserMetadata(nombre)))
        if (respuesta.isSuccessful) ResultadoAuth.Exito(Unit)
        else ResultadoAuth.Error(respuesta.mensajeDeError())
    }

    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): ResultadoAuth<UsuarioSesion> = ejecutar {
        val respuesta = api.iniciarSesion(LoginRequest(correo, contrasena))
        val auth = respuesta.body()
        if (!respuesta.isSuccessful || auth == null) {
            return@ejecutar ResultadoAuth.Error(respuesta.mensajeDeError())
        }

        // los tokens se guardan ANTES de pedir el perfil, porque esa petición
        // necesita el encabezado Authorization que el interceptor toma de SessionManager
        sessionManager.guardarTokens(auth.accessToken, auth.refreshToken, auth.expiresIn)

        val perfil = try {
            api.obtenerPerfil("eq.${auth.user.id}").body()?.firstOrNull()
        } catch (e: Exception) {
            sessionManager.limpiar() // no dejar una sesión a medias si falla la lectura del perfil
            throw e
        }
        if (perfil == null) {
            sessionManager.limpiar()
            return@ejecutar ResultadoAuth.Error("No se encontró el perfil del usuario.")
        }

        // el rol se lee de la base de datos (perfiles), nunca de lo que envíe la app
        val usuario = UsuarioSesion(perfil.id, perfil.nombre, perfil.correo, perfil.rol)
        sessionManager.guardarUsuario(usuario)
        ResultadoAuth.Exito(usuario)
    }

    override fun restaurarSesion(): UsuarioSesion? =
        if (sessionManager.haySesion()) sessionManager.obtenerUsuario() else null

    override suspend fun renovarSesionSiExpiro(): Boolean = candadoRenovacion.withLock {
        if (!sessionManager.haySesion()) return@withLock false
        if (!sessionManager.tokenPorExpirar()) return@withLock true // todavía sirve

        val refreshToken = sessionManager.obtenerRefreshToken() ?: return@withLock false
        try {
            val respuesta = api.renovarSesion(RefreshRequest(refreshToken))
            val auth = respuesta.body()
            when {
                respuesta.isSuccessful && auth != null -> {
                    sessionManager.guardarTokens(auth.accessToken, auth.refreshToken, auth.expiresIn)
                    true
                }
                //un 4xx (menos 429) significa que el refresh_token ya no sirve:
                // la sesión es inválida y hay que volver a iniciar sesión
                respuesta.code() in 400..499 && respuesta.code() != 429 -> {
                    sessionManager.limpiar()
                    false
                }
                else -> false
            }
        } catch (e: IOException) {
            false // sin internet: se conserva la sesión y se reintenta después
        }
    }

    override fun cerrarSesion() {
        sessionManager.limpiar()
    }

    //auxiliares

    /** Ejecuta una operación de red y convierte las fallas técnicas en un ResultadoAuth.Error. */
    private suspend fun <T> ejecutar(bloque: suspend () -> ResultadoAuth<T>): ResultadoAuth<T> =
        try {
            bloque()
        } catch (e: IOException) {
            ResultadoAuth.Error("Sin conexión a internet. Intenta de nuevo.")
        } catch (e: SerializationException) {
            ResultadoAuth.Error("Respuesta inesperada del servidor.")
        }

    /** Lee el cuerpo de error de Supabase y devuelve un mensaje en español. */
    private fun <T> Response<T>.mensajeDeError(): String {
        val error = errorBody()?.string()?.let { texto ->
            runCatching { RetrofitClient.json.decodeFromString<SupabaseErrorDto>(texto) }.getOrNull()
        }
        return when (error?.errorCode) {
            "invalid_credentials" -> "Correo o contraseña incorrectos."
            "user_already_exists", "email_exists" -> "Este correo ya está registrado."
            "weak_password" -> "La contraseña es muy débil (mínimo 6 caracteres)."
            "email_address_invalid" -> "El correo no es válido."
            "over_email_send_rate_limit", "over_request_rate_limit" ->
                "Demasiados intentos. Espera unos minutos."
            else -> error?.mensajeLegible() ?: "Error del servidor (código ${code()})."
        }
    }
}
package com.example.ciudadalerta.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ciudadalerta.domain.models.UsuarioSesion

/**
 * Guarda en el dispositivo los tokens de Supabase y los datos del usuario con sesión iniciada.
 * Usa SharedPreferences privadas de la app (nadie más puede leerlas) y un archivo excluido del
 * respaldo en la nube (ver backup_rules.xml). Es suficiente para un prototipo.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)

    /** Guarda los tokens y calcula en qué momento vence el access_token. */
    fun guardarTokens(accessToken: String, refreshToken: String, expiraEnSegundos: Long) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            // CLAVE: se guarda la hora de vencimiento absoluta, no la duración
            .putLong(KEY_EXPIRA_EN, System.currentTimeMillis() + expiraEnSegundos * 1000)
            .apply()
    }

    fun guardarUsuario(usuario: UsuarioSesion) {
        prefs.edit()
            .putString(KEY_ID, usuario.id)
            .putString(KEY_NOMBRE, usuario.nombre)
            .putString(KEY_CORREO, usuario.correo)
            .putString(KEY_ROL, usuario.rol)
            .apply()
    }

    /** Lo usa el interceptor de Retrofit en cada petición. */
    fun obtenerAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun obtenerRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun obtenerUsuario(): UsuarioSesion? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        return UsuarioSesion(
            id = id,
            nombre = prefs.getString(KEY_NOMBRE, "") ?: "",
            correo = prefs.getString(KEY_CORREO, "") ?: "",
            rol = prefs.getString(KEY_ROL, "") ?: ""
        )
    }

    /** Hay sesión si existen el refresh_token y el usuario guardados. */
    fun haySesion(): Boolean = obtenerRefreshToken() != null && obtenerUsuario() != null

    /** true si el access_token ya venció o vence en menos de [margenMs] (por defecto 1 minuto). */
    fun tokenPorExpirar(margenMs: Long = 60_000L): Boolean =
        System.currentTimeMillis() >= prefs.getLong(KEY_EXPIRA_EN, 0L) - margenMs

    fun limpiar() {
        prefs.edit().clear().apply()
    }

    companion object {
        // CLAVE: este nombre debe coincidir con path="sesion_prefs.xml" de las reglas de respaldo
        private const val NOMBRE_PREFS = "sesion_prefs"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRA_EN = "expira_en"
        private const val KEY_ID = "usuario_id"
        private const val KEY_NOMBRE = "usuario_nombre"
        private const val KEY_CORREO = "usuario_correo"
        private const val KEY_ROL = "usuario_rol"
    }
}
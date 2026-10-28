package com.example.ciudadalerta.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construye el cliente Retrofit configurado para Supabase.
 * Se encarga de dos cosas: agregar a cada petición los encabezados de autenticación
 * y convertir el JSON a las clases de datos con kotlinx.serialization.
 */
object RetrofitClient {

    /**
     * Configuración de JSON compartida (también sirve para leer los cuerpos de error).
     * - ignoreUnknownKeys: no falla si el servidor manda campos que no declaramos.
     * - explicitNulls = false: los null NO se envían. si se enviara "fecha_creacion": null,
     *   PostgREST lo guardaría como NULL e ignoraría el valor por defecto now() de la tabla.
     * - encodeDefaults: envía también las propiedades que conservan su valor por defecto.
     */
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    /**
     * Crea la API lista para usar.
     * @param tokenProvider devuelve el access_token de la sesión actual, o null si no hay sesión
     * (por ejemplo, al iniciar sesión o registrarse).
     */
    fun crearApi(tokenProvider: () -> String?): SupabaseApi {
        val clienteHttp = OkHttpClient.Builder()
            //este interceptor agrega los encabezados a TODAS las peticiones
            .addInterceptor { cadena ->
                val constructor = cadena.request().newBuilder()
                    .header("apikey", SupabaseConfig.PUBLISHABLE_KEY) // identifica el proyecto
                tokenProvider()?.let { token ->
                    constructor.header("Authorization", "Bearer $token") // identifica al usuario (RLS)
                }
                cadena.proceed(constructor.build())
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    // BASIC solo muestra método, URL y código. BODY imprimiría contraseñas
                    // y tokens en Logcat: úsalo solo un momento para depurar.
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                    redactHeader("apikey")
                }
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS) // las fotos tardan más en subirse
            .build()

        return Retrofit.Builder()
            .baseUrl("${SupabaseConfig.URL}/") //Retrofit exige que la URL base termine en "/"
            .client(clienteHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SupabaseApi::class.java)
    }
}
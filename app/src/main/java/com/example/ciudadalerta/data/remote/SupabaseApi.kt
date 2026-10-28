package com.example.ciudadalerta.data.remote

import com.example.ciudadalerta.data.remote.dto.AuthResponse
import com.example.ciudadalerta.data.remote.dto.EstatusUpdate
import com.example.ciudadalerta.data.remote.dto.LoginRequest
import com.example.ciudadalerta.data.remote.dto.PerfilDto
import com.example.ciudadalerta.data.remote.dto.RefreshRequest
import com.example.ciudadalerta.data.remote.dto.ReporteDto
import com.example.ciudadalerta.data.remote.dto.SignUpRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Contrato REST contra Supabase: Auth (/auth/v1), base de datos (/rest/v1) y Storage (/storage/v1).
 *
 * CLAVE: todas las funciones devuelven Response<T> en lugar de T. Así un 400, 401 o 409 NO lanza
 * excepción: el repositorio revisa response.isSuccessful y lee el código y el cuerpo del error.
 * Los encabezados apikey y Authorization los agrega el interceptor de RetrofitClient.
 */
interface SupabaseApi {

    //AUTH

    /** Registra un usuario nuevo. El trigger del servidor le crea su fila en `perfiles`. */
    @POST("auth/v1/signup")
    suspend fun registrar(@Body solicitud: SignUpRequest): Response<AuthResponse>

    /** Inicia sesión con correo y contraseña; devuelve los tokens. */
    @POST("auth/v1/token?grant_type=password") // CLAVE: el grant_type va fijo en la URL
    suspend fun iniciarSesion(@Body solicitud: LoginRequest): Response<AuthResponse>

    /** Cambia un refresh_token por una sesión nueva cuando el access_token expira (1 hora). */
    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun renovarSesion(@Body solicitud: RefreshRequest): Response<AuthResponse>

    // PERFILES

    /**
     * Obtiene el perfil (con su rol) de un usuario.
     * CLAVE: PostgREST filtra con "operador.valor", así que se llama con filtroId = "eq.<uuid>".
     * Devuelve una lista: vacía si el perfil no existe o las políticas RLS no lo permiten.
     */
    @GET("rest/v1/perfiles")
    suspend fun obtenerPerfil(
        @Query("id") filtroId: String,
        @Query("select") columnas: String = "*"
    ): Response<List<PerfilDto>>

    //REPORTES

    /**
     * Envía un reporte nuevo.
     * CLAVE: "return=minimal" evita que el servidor devuelva el registro (respuesta 201 sin cuerpo).
     * Si el id ya existe responde 409: significa que ese reporte ya se había subido.
     */
    @Headers("Prefer: return=minimal")
    @POST("rest/v1/reportes")
    suspend fun crearReporte(@Body reporte: ReporteDto): Response<Unit>

    /**
     * Lista los reportes junto al nombre de quien los creó.
     * CLAVE: RLS filtra solo: el ciudadano recibe únicamente los suyos y el administrador todos.
     * "perfiles(nombre)" hace el JOIN usando la llave foránea usuario_id.
     */
    @GET("rest/v1/reportes")
    suspend fun obtenerReportes(
        @Query("select") columnas: String = "*,perfiles(nombre)",
        @Query("order") orden: String = "fecha_creacion.desc"
    ): Response<List<ReporteDto>>

    /**
     * Cambia el estatus de un reporte. Se llama con filtroId = "eq.<uuid>".
     * CLAVE: "return=representation" devuelve las filas modificadas. Si la lista llega VACÍA,
     * nadie lo modificó: RLS bloquea en silencio a quien no es administrador (no da error).
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/reportes")
    suspend fun actualizarEstatus(
        @Query("id") filtroId: String,
        @Body cambio: EstatusUpdate
    ): Response<List<ReporteDto>>

    //FOTOS

    /**
     * Sube la foto de un reporte al bucket.
     * CLAVE: encoded = true deja pasar las "/" de la ruta ("uid/idReporte.jpg") sin escaparlas.
     * El cuerpo se arma con archivo.asRequestBody("image/jpeg".toMediaType()), que fija el Content-Type.
     * Un 409 significa que esa foto ya estaba subida.
     */
    @POST("storage/v1/object/{bucket}/{ruta}")
    suspend fun subirFoto(
        @Path("bucket") bucket: String,
        @Path("ruta", encoded = true) ruta: String,
        @Body foto: RequestBody
    ): Response<Unit>

    /**
     * Descarga una foto de un bucket privado.
     * CLAVE: la ruta "authenticated" exige el token del usuario, y las políticas de Storage deciden
     * si puede verla (dueño o administrador). @Streaming evita cargar la imagen entera en memoria.
     */
    @Streaming
    @GET("storage/v1/object/authenticated/{bucket}/{ruta}")
    suspend fun descargarFoto(
        @Path("bucket") bucket: String,
        @Path("ruta", encoded = true) ruta: String
    ): Response<ResponseBody>
}
package com.example.ciudadalerta.di

import android.content.Context
import com.example.ciudadalerta.data.local.CiudadAlertaDatabase
import com.example.ciudadalerta.data.local.SessionManager
import com.example.ciudadalerta.data.remote.RetrofitClient
import com.example.ciudadalerta.data.remote.SupabaseApi
import com.example.ciudadalerta.data.repository.AuthRepositoryImpl
import com.example.ciudadalerta.data.repository.FotoStorageImpl
import com.example.ciudadalerta.data.repository.LocationProviderImpl
import com.example.ciudadalerta.data.repository.ReporteRepositoryImpl
import com.example.ciudadalerta.domain.location.LocationProvider
import com.example.ciudadalerta.domain.repository.AuthRepository
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.domain.storage.FotoStorage

/**
 * Contenedor de dependencias de la aplicación (inyección manual).
 * Crea cada componente una sola vez y lo comparte con toda la app:
 * Application -> Activity -> Compose -> ViewModel -> Repository -> local (Room) / remoto (Retrofit).
 * Los repositorios se exponen como interfaces del dominio, así las capas superiores
 * no dependen de Room ni de Retrofit.
 */
class AppContainer(context: Context) {

    // siempre el contexto de la aplicación, nunca el de una Activity
    private val appContext = context.applicationContext

    //Capa local
    val database: CiudadAlertaDatabase by lazy { CiudadAlertaDatabase.getDatabase(appContext) }
    val sessionManager: SessionManager by lazy { SessionManager(appContext) }

    // Capa remota
    //El interceptor pide el token en cada petición, así que siempre usa el vigente
    val supabaseApi: SupabaseApi by lazy {
        RetrofitClient.crearApi { sessionManager.obtenerAccessToken() }
    }

    //Repositorios
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(supabaseApi, sessionManager) }
    val reporteRepository: ReporteRepository by lazy { ReporteRepositoryImpl(database.reporteDao()) }

    //Servicios del dispositivo
    val fotoStorage: FotoStorage by lazy { FotoStorageImpl(appContext) }
    val locationProvider: LocationProvider by lazy { LocationProviderImpl(appContext) }
}
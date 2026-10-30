package com.example.ciudadalerta

import android.app.Application
import com.example.ciudadalerta.di.AppContainer

/**
 * Clase Application: se crea antes que cualquier Activity y vive mientras la app esté en memoria.
 * Guarda el [AppContainer] para que toda la app  comparta las mismas
 * dependencias.
 */
class CiudadAlertaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this) //se crea una sola vez al arrancar el proceso
    }
}
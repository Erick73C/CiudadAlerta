package com.example.ciudadalerta.domain.sync


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ciudadalerta.CiudadAlertaApp
import com.example.ciudadalerta.domain.models.ResultadoSync

/**
 * Tarea en segundo plano que sincroniza reportes con Supabase.
 * WorkManager la crea sin pasar por la Activity, por eso toma las dependencias
 * del AppContainer guardado en la Application.
 */
class SyncWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CiudadAlertaApp).container
        val auth = container.authRepository

        // Sin sesión no hay nada que sincronizar (RLS rechazaría todo)
        if (auth.restaurarSesion() == null) return Result.success()

        // El access_token dura 1 hora: se renueva antes de llamar a la API.
        // false = sin internet o sesión inválida (en ese caso limpiar() ya quitó la sesión)
        if (!auth.renovarSesionSiExpiro()) {
            return if (auth.restaurarSesion() == null) Result.success() else Result.retry()
        }

        val resultado = container.reporteRepository.sincronizarTodo()
        Log.d(TAG, "Sincronización terminada: $resultado (intento $runAttemptCount)")
        return when (resultado) {
            ResultadoSync.EXITO -> Result.success()
            ResultadoSync.REINTENTAR -> Result.retry() // WorkManager aplica el backoff exponencial
            ResultadoSync.FALLO -> Result.failure()    // en la periódica, la siguiente vuelta sí corre
        }
    }

    private companion object {
        const val TAG = "SyncWorker"
    }
}
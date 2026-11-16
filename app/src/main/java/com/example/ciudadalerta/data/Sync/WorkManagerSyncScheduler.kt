package com.example.ciudadalerta.domain.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
/** Implementación de [SyncScheduler] con Jetpack WorkManager. */
class WorkManagerSyncScheduler(context: Context) : SyncScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    // el trabajo solo corre cuando hay conexión; si no, WorkManager espera la señal
    private val conInternet = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun solicitarSincronizacion() {
        val solicitud = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(conInternet)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        // "unique" evita acumular trabajos duplicados. APPEND_OR_REPLACE (y no KEEP) hace que
        // un reporte creado mientras otra sincronización corre quede en cola y no se pierda.
        workManager.enqueueUniqueWork(NOMBRE_INMEDIATA, ExistingWorkPolicy.APPEND_OR_REPLACE, solicitud)
    }

    // Todos los trabajos de sincronización llevan la etiqueta "sync": así se observa su estado juntos
    override val sincronizando: Flow<Boolean> =
        workManager.getWorkInfosByTagFlow(ETIQUETA).map { trabajos ->
            trabajos.any { it.state == WorkInfo.State.RUNNING }
        }


    override fun programarPeriodica() {
        val solicitud = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES) // 15 min es el mínimo
            .setConstraints(conInternet)
            .build()
        // KEEP: si ya estaba programada no se reinicia el reloj
        workManager.enqueueUniquePeriodicWork(NOMBRE_PERIODICA, ExistingPeriodicWorkPolicy.KEEP, solicitud)
    }

    private companion object {
        const val NOMBRE_INMEDIATA = "sync_inmediata"
        const val NOMBRE_PERIODICA = "sync_periodica"
        const val ETIQUETA = "sync"
    }
}
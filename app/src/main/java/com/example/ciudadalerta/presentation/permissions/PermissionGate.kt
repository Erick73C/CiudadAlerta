package com.example.ciudadalerta.presentation.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private val PERMISOS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

// Cada cuánto se recuerda tras "Más tarde". 1 minuto para probar. DEPUES PONER 24 HORAS
private const val RECORDATORIO_MS = 60_000L

private const val PREFS = "permisos_prefs"
private const val KEY_RECHAZADO = "rechazado"
private const val KEY_MAS_TARDE_TS = "mas_tarde_ts"

private enum class Dialogo { NINGUNO, SOLICITAR, MANUAL }

/**
 * Envuelve la app y solicita los permisos de cámara y ubicación con tres opciones:
 * Permitir, Más tarde (recordatorio periódico) y Ahora no (queda como denegado
 * y se informa que debe activarlos manualmente en Ajustes).
 */
@Composable
fun PermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    var dialogo by remember { mutableStateOf(Dialogo.NINGUNO) }
    var reintento by remember { mutableIntStateOf(0) }

    fun concedido(p: String) =
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    // La ubicación aproximada o la precisa son suficientes
    fun faltan() = !concedido(Manifest.permission.CAMERA) ||
            !(concedido(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    concedido(Manifest.permission.ACCESS_COARSE_LOCATION))

    fun programarRecordatorio() {
        prefs.edit().putLong(KEY_MAS_TARDE_TS, System.currentTimeMillis()).apply()
        dialogo = Dialogo.NINGUNO
        reintento++ // relanza el LaunchedEffect que espera y vuelve a preguntar
    }

    fun marcarRechazado() {
        prefs.edit().putBoolean(KEY_RECHAZADO, true).apply()
        dialogo = Dialogo.MANUAL
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (!faltan()) return@rememberLauncherForActivityResult
        // Si Android ya no permite volver a preguntar, solo queda Ajustes
        val permanente = activity != null && PERMISOS.any { p ->
            resultado[p] == false &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, p)
        }
        if (permanente) marcarRechazado() else programarRecordatorio()
    }

    // Decide cuándo mostrar el diálogo inicial o el recordatorio
    LaunchedEffect(reintento) {
        if (!faltan() || prefs.getBoolean(KEY_RECHAZADO, false)) return@LaunchedEffect
        val transcurrido = System.currentTimeMillis() - prefs.getLong(KEY_MAS_TARDE_TS, 0L)
        val restante = RECORDATORIO_MS - transcurrido
        if (restante > 0) delay(restante)
        if (faltan() && !prefs.getBoolean(KEY_RECHAZADO, false)) dialogo = Dialogo.SOLICITAR
    }

    content()

    when (dialogo) {
        Dialogo.SOLICITAR -> AlertDialog(
            onDismissRequest = { /* obliga a elegir una opción */ },
            title = { Text("Permisos necesarios") },
            text = {
                Text(
                    "Ciudad Alerta necesita la cámara para adjuntar una foto del problema " +
                            "y la ubicación para señalar dónde ocurre."
                )
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            dialogo = Dialogo.NINGUNO
                            launcher.launch(PERMISOS)
                        }
                    ) { Text("Permitir") }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { programarRecordatorio() }
                    ) { Text("Más tarde") }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { marcarRechazado() }
                    ) { Text("Ahora no") }
                }
            }
        )

        Dialogo.MANUAL -> AlertDialog(
            onDismissRequest = { dialogo = Dialogo.NINGUNO },
            title = { Text("Permisos denegados") },
            text = {
                Text(
                    "Sin estos permisos no podrás adjuntar fotos ni tu ubicación en los " +
                            "reportes. Para activarlos, ve a Ajustes > Permisos de la app."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogo = Dialogo.NINGUNO
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }) { Text("Abrir ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { dialogo = Dialogo.NINGUNO }) { Text("Entendido") }
            }
        )

        Dialogo.NINGUNO -> Unit
    }
}
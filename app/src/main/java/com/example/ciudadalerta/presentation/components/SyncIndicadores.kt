package com.example.ciudadalerta.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadalerta.data.local.entity.EstadoSync
import com.example.ciudadalerta.domain.models.EstadoRed
import kotlinx.coroutines.delay

/** Etiqueta pequeña con el estado de sincronización de UN reporte. */
@Composable
fun SyncChip(sincronizacion: String, modifier: Modifier = Modifier) {
    val (texto, color, icono) = when (sincronizacion) {
        EstadoSync.LOCAL -> Triple("Pendiente de envío", Color(0xFFF57C00), Icons.Default.Refresh)
        EstadoSync.ESTATUS_PENDIENTE -> Triple("Cambio sin enviar", Color(0xFFF57C00), Icons.Default.Refresh)
        else -> Triple("Enviado", Color(0xFF43A047), Icons.Default.Check)
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Banner superior con el estado de la red y la sincronización.
 * Prioridad: sin conexión > sincronizando > "conexión restablecida" (2.5 s) > pendientes > nada.
 */
@Composable
fun SyncBanner(
    estado: EstadoRed,
    pendientes: Int,
    onSincronizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // CLAVE: el mensaje verde solo aparece al REGRESAR de una desconexión, no al abrir la app.
    // La clave es el Boolean "enLinea" (y no el estado completo) para que SINCRONIZANDO no cancele el temporizador.
    val enLinea = estado != EstadoRed.SIN_CONEXION
    var huboDesconexion by remember { mutableStateOf(false) }
    var mostrarConectado by remember { mutableStateOf(false) }
    LaunchedEffect(enLinea) {
        if (!enLinea) {
            huboDesconexion = true
        } else if (huboDesconexion) {
            huboDesconexion = false
            mostrarConectado = true
            delay(2_500)
            mostrarConectado = false
        }
    }

    when {
        estado == EstadoRed.SIN_CONEXION -> BannerBase(
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer,
            icono = Icons.Default.Warning,
            texto = if (pendientes > 0) "Sin conexión · $pendientes por enviar cuando haya internet"
            else "Sin conexión · tus reportes se guardan y se envían al reconectar",
            modifier = modifier
        )
        estado == EstadoRed.SINCRONIZANDO -> BannerBase(
            fondo = MaterialTheme.colorScheme.secondaryContainer,
            contenido = MaterialTheme.colorScheme.onSecondaryContainer,
            icono = Icons.Default.Refresh,
            texto = "Sincronizando con el servidor…",
            progreso = true,
            modifier = modifier
        )
        mostrarConectado -> BannerBase(
            fondo = Color(0xFFE8F5E9),
            contenido = Color(0xFF2E7D32),
            icono = Icons.Default.Check,
            texto = "Conexión restablecida",
            modifier = modifier
        )
        pendientes > 0 -> BannerBase(
            fondo = Color(0xFFFFF3E0),
            contenido = Color(0xFFE65100),
            icono = Icons.Default.Refresh,
            texto = if (pendientes == 1) "1 reporte pendiente de envío" else "$pendientes reportes pendientes de envío",
            accion = { TextButton(onClick = onSincronizar) { Text("Sincronizar") } },
            modifier = modifier
        )
    }
}

@Composable
private fun BannerBase(
    fondo: Color,
    contenido: Color,
    icono: ImageVector,
    texto: String,
    modifier: Modifier = Modifier,
    progreso: Boolean = false,
    accion: (@Composable () -> Unit)? = null
) {
    Surface(color = fondo, contentColor = contenido, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                accion?.invoke()
            }
            if (progreso) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
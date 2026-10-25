package com.example.ciudadalerta.presentation.screens.administrador

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ciudadalerta.presentation.util.colorPorEstatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Detalle de un reporte para el administrador: muestra todos los datos que capturó
 * el ciudadano (foto, ubicación, categoría...) y permite cambiar el estatus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    navController: NavController,
    viewModel: ReportDetailViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val reporte = state.reporte
        when {
            state.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            reporte == null -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { Text("Este reporte ya no existe.") }

            else -> DetalleContenido(
                reporte = reporte,
                estatusSeleccionado = state.estatusSeleccionado,
                guardando = state.guardando,
                onEstatusSeleccionado = viewModel::onEstatusSeleccionado,
                onActualizar = viewModel::actualizarEstatus,
                onSinAppDeMapas = { viewModel.mostrarMensaje("No hay una app de mapas instalada") },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun DetalleContenido(
    reporte: ReportDetailUi,
    estatusSeleccionado: String?,
    guardando: Boolean,
    onEstatusSeleccionado: (String) -> Unit,
    onActualizar: () -> Unit,
    onSinAppDeMapas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // La foto se decodifica fuera del hilo principal y reducida para no gastar memoria
    val fotoBitmap by produceState<ImageBitmap?>(initialValue = null, reporte.fotoPath) {
        value = reporte.fotoPath?.let { path ->
            withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 })
                    ?.asImageBitmap()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Foto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = fotoBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto del reporte",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = if (reporte.fotoPath == null) "Sin foto" else "Cargando foto...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Título y estatus actual
        val colorEstatus = colorPorEstatus(reporte.estatus)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reporte.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = colorEstatus.copy(alpha = 0.2f),
                contentColor = colorEstatus
            ) {
                Text(
                    text = reporte.estatus,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        if (reporte.urgente) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Peligro inminente: requiere atención inmediata",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Datos del reporte
        DatoFila("Descripción", reporte.descripcion)
        DatoFila("Categoría", reporte.categoria)
        DatoFila("Reportado por", reporte.nombreUsuario)
        DatoFila("Fecha del reporte", reporte.fecha)
        DatoFila("Hora aproximada del incidente", reporte.horaIncidente)

        // Ubicación
        val latitud = reporte.latitud
        val longitud = reporte.longitud
        if (latitud != null && longitud != null) {
            DatoFila("Ubicación", "%.5f, %.5f".format(latitud, longitud))
            OutlinedButton(
                onClick = {
                    abrirEnMaps(context, latitud, longitud, reporte.titulo, onSinAppDeMapas)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir en Maps")
            }
        } else {
            DatoFila("Ubicación", "Sin ubicación registrada")
        }

        HorizontalDivider()

        // Cambio de estatus (RF-08)
        Text("Actualizar estatus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column(modifier = Modifier.selectableGroup()) {
            estatusReporte.forEach { estatus ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = (estatus == estatusSeleccionado),
                            onClick = { onEstatusSeleccionado(estatus) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (estatus == estatusSeleccionado), onClick = null)
                    Text(
                        text = estatus,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Button(
            onClick = onActualizar,
            enabled = !guardando && estatusSeleccionado != null && estatusSeleccionado != reporte.estatus,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (guardando) "Guardando..." else "Actualizar estatus", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DatoFila(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Abre la ubicación del reporte en la app de mapas del dispositivo (sin API key). */
private fun abrirEnMaps(
    context: Context,
    latitud: Double,
    longitud: Double,
    titulo: String,
    onSinApp: () -> Unit
) {

    val uri = Uri.parse("geo:0,0?q=$latitud,$longitud(${Uri.encode(titulo)})")

    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        onSinApp()
    }
}
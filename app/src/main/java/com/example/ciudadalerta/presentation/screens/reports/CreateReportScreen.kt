package com.example.ciudadalerta.presentation.screens.reports

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.util.Calendar


/**
 *
 * Pantalla que permite al ciudadano llenar un formulario para reportar una nueva incidencia.
 * Incluye validación visual, selección de categorías (Radios), opciones adicionales (Checkbox)
 * y selección de hora (TimePicker).
 * @author NOMBRE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    navController: NavController,
    viewModel: CreateReportViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // Estado propio de la UI: el TimePicker
    var mostrarTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = false
    )

    // Al guardar con éxito, volvemos a la lista
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) navController.popBackStack()
    }

    // Vista previa de la foto (reducida para no gastar memoria)
    val fotoBitmap = remember(state.fotoPath) {
        state.fotoPath?.let {
            BitmapFactory.decodeFile(it, BitmapFactory.Options().apply { inSampleSize = 4 })
                ?.asImageBitmap()
        }
    }

    // Cámara
    val tomarFotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito -> viewModel.onFotoResultado(exito) }

    val lanzarCamara = {
        val foto = viewModel.prepararFoto()
        tomarFotoLauncher.launch(Uri.parse(foto.uri))
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzarCamara()
        else viewModel.mostrarError("Sin permiso de cámara. Actívalo en Ajustes > Permisos de la app.")
    }

    // Ubicación
    val permisoUbicacionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) viewModel.obtenerUbicacion()
        else viewModel.mostrarError("Sin permiso de ubicación. Actívalo en Ajustes > Permisos de la app.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Reporte") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Evidencia (Foto)
            Button(
                onClick = {
                    if (tienePermiso(context, Manifest.permission.CAMERA)) lanzarCamara()
                    else permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (fotoBitmap != null) {
                    Image(
                        bitmap = fotoBitmap,
                        contentDescription = "Foto del reporte",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Cámara", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toma una foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 2. Cajas de Texto
            OutlinedTextField(
                value = state.titulo,
                onValueChange = viewModel::onTituloChanged,
                label = { Text("Título del reporte") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChanged,
                label = { Text("Descripción detallada") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // 3. Categoría (Radio Buttons)
            Text("Categoría del Problema:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Column(modifier = Modifier.selectableGroup()) {
                categoriasReporte.forEach { categoria ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (categoria == state.categoria),
                                onClick = { viewModel.onCategoriaChanged(categoria) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (categoria == state.categoria),
                            onClick = null
                        )
                        Text(
                            text = categoria,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            // 4. Hora (TimePicker)
            Text("Hora aproximada del incidente:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = state.hora ?: "No seleccionada")
                OutlinedButton(onClick = { mostrarTimePicker = true }) {
                    Text("Seleccionar Hora")
                }
            }

            if (mostrarTimePicker) {
                AlertDialog(
                    onDismissRequest = { mostrarTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onHoraChanged(
                                "${timePickerState.hour}:${timePickerState.minute.toString().padStart(2, '0')}"
                            )
                            mostrarTimePicker = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar") }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }

            // 5. Ubicación GPS
            OutlinedButton(
                onClick = {
                    if (tienePermiso(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        tienePermiso(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    ) viewModel.obtenerUbicacion()
                    else permisoUbicacionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                enabled = !state.obteniendoUbicacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.obteniendoUbicacion) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Ubicación")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        state.obteniendoUbicacion -> "Obteniendo ubicación..."
                        state.ubicacion != null -> "Ubicación: %.5f, %.5f".format(
                            state.ubicacion!!.latitud, state.ubicacion!!.longitud
                        )
                        else -> "Obtener mi ubicación GPS"
                    }
                )
            }

            // 6. Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.requiereAtencionInmediata,
                    onCheckedChange = viewModel::onUrgenteChanged
                )
                Text("Este problema representa un peligro inminente")
            }

            // Mensaje de error
            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Guardar
            Button(
                onClick = viewModel::guardarReporte,
                enabled = !state.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (state.guardando) "Guardando..." else "Guardar Reporte", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


private fun tienePermiso(context: Context, permiso: String): Boolean =
    ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
package com.example.ciudadalerta.presentation.screens.ciudadano

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun CreateReportScreen(navController: NavController) {
    // Estados del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Estado para Radio Buttons
    val opcionesCategoria = listOf("Bache", "Alumbrado", "Fuga de Agua", "Basura", "Otro")
    var categoriaSeleccionada by remember { mutableStateOf(opcionesCategoria[0]) }

    // Estado para Checkbox
    var requiereAtencionInmediata by remember { mutableStateOf(false) }

    // Estado para el TimePicker
    var horaIncidente by remember { mutableStateOf("No seleccionada") }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = false
    )

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
                // verticalScroll permite que la pantalla sea scrolleable si el formulario es muy largo
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Botón para Evidencia (Foto) - Placeholder
            Button(
                onClick = { /* Lógica de cámara pendiente */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Cámara", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toma o selecciona una foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 2. Cajas de Texto (TextFields)
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del reporte") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción detallada") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // 3. Selección de Categoría (Radio Buttons)
            Text("Categoría del Problema:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Column(modifier = Modifier.selectableGroup()) {
                opcionesCategoria.forEach { categoria ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (categoria == categoriaSeleccionada),
                                onClick = { categoriaSeleccionada = categoria },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (categoria == categoriaSeleccionada),
                            onClick = null // null porque el Row ya maneja el clic
                        )
                        Text(
                            text = categoria,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            // 4. Selector de Hora (TimePicker)
            Text("Hora aproximada del incidente:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = horaIncidente)
                OutlinedButton(onClick = { mostrarTimePicker = true }) {
                    Text("Seleccionar Hora")
                }
            }

            // Diálogo del TimePicker
            if (mostrarTimePicker) {
                AlertDialog(
                    onDismissRequest = { mostrarTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            horaIncidente = "${timePickerState.hour}:${timePickerState.minute.toString().padStart(2, '0')}"
                            mostrarTimePicker = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar") }
                    },
                    text = {
                        TimePicker(state = timePickerState)
                    }
                )
            }

            // 5. Ubicación (Placeholder de GPS)
            OutlinedButton(
                onClick = { /* Lógica de GPS pendiente */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Ubicación")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Obtener mi ubicación GPS")
            }

            // 6. Opciones Adicionales (Checkbox)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = requiereAtencionInmediata,
                    onCheckedChange = { requiereAtencionInmediata = it }
                )
                Text("Este problema representa un peligro inminente")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Botón de Guardar / Enviar
            Button(
                onClick = {
                    // Aquí en el futuro llamaremos al ViewModel para guardar en Room
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Reporte", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
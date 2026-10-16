package com.example.ciudadalerta.presentation.screens.ciudadano
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ciudadalerta.presentation.navigation.Rutas


/**
 *
 * Pantalla que muestra el historial de reportes creados por el ciudadano.
 * Utiliza un Scaffold para contener la barra superior, el botón de creación (FAB)
 * y una LazyColumn para renderizar la lista de incidencias.
 * @author NOMBRE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenListScreen(
    navController: NavController,
    viewModel: CitizenListViewModel
) {
    val nombreUsuario by viewModel.nombreUsuario.collectAsState()
    val listaReportes by viewModel.reportes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = nombreUsuario,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("Mis Reportes", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Rutas.CreateReport.ruta) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Crear nuevo reporte")
            }
        }
    ) { paddingValues ->
        if (listaReportes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes reportes. Toca + para crear uno.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(listaReportes, key = { it.id }) { reporte ->
                    ReporteItem(reporte = reporte)
                }
            }
        }
    }
}

/**
 *
 * Componente visual (Tarjeta) que representa un elemento individual dentro de la lista de reportes.
 * @author NOMBRE
 */
@Composable
fun ReporteItem(reporte: ReporteDummy) {
    // Definimos un color dependiendo del estatus del reporte
    val colorEstatus = when (reporte.estatus) {
        "Pendiente" -> Color(0xFFE53935) // Rojo
        "En Revisión" -> Color(0xFFFDD835) // Amarillo
        "Resuelto" -> Color(0xFF43A047) // Verde
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reporte.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Etiqueta del estatus
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reporte.descripcion,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fecha de reporte: ${reporte.fecha}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
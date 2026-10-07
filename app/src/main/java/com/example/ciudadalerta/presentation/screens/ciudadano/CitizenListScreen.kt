package com.example.ciudadalerta.presentation.screens.ciudadano

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ciudadalerta.presentation.navigation.Rutas

/**
 *
 * Modelo de datos temporal para representar un reporte en la interfaz.
 * Más adelante esto será reemplazado por la entidad real del dominio y base de datos.
 * @author NOMBRE
 */
data class ReporteDummy(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val estatus: String,
    val fecha: String
)

/**
 *
 * Pantalla que muestra el historial de reportes creados por el ciudadano.
 * Utiliza un Scaffold para contener la barra superior, el botón de creación (FAB)
 * y una LazyColumn para renderizar la lista de incidencias.
 * @author NOMBRE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenListScreen(navController: NavController) {

    // Lista de reportes harcodeados (Dummy Data) para ver el diseño
    val listaReportes = listOf(
        ReporteDummy("1", "Bache profundo", "Bache en el carril derecho que daña los neumáticos.", "Pendiente", "06/10/2026"),
        ReporteDummy("2", "Luminaria fundida", "Poste de luz sin funcionar desde hace 3 días en el parque.", "En Revisión", "04/10/2026"),
        ReporteDummy("3", "Fuga de agua", "Fuga constante de agua potable en la banqueta.", "Resuelto", "01/10/2026"),
        ReporteDummy("4", "Semáforo descompuesto", "Semáforo parpadeando en rojo todo el día.", "Pendiente", "28/09/2026")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reportes", fontWeight = FontWeight.Bold) },
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
        // Contenedor principal de la lista
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(listaReportes) { reporte ->
                ReporteItem(reporte = reporte)
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
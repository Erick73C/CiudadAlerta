package com.example.ciudadalerta.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListScreen
import com.example.ciudadalerta.presentation.screens.ciudadano.CreateReportScreen
import com.example.ciudadalerta.presentation.screens.login.LoginScreen


/**
 *
 * Define todas las pantallas disponibles en la aplicación y sus rutas.
 * Utiliza una clase sellada (sealed class) para garantizar rutas fuertemente tipadas
 *
 * @author TU NOMBRE
 */
@Composable
fun AppNavigation() {
    // El NavController es el objeto que nos permite cambiar de pantalla
    val navController = rememberNavController()

    // El NavHost es el contenedor de las pantallas.
    // Empezaremos en la pantalla de Login.
    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta
    ) {

        // 1. Pantalla de Login
        composable(route = Rutas.Login.ruta) {
            LoginScreen(navController = navController)
        }

        // 2. Pantalla de Lista de Ciudadano
        composable(route = Rutas.CitizenList.ruta) {
            CitizenListScreen(navController = navController)
        }

        // 3. Pantalla para Crear Reporte EJEMPLO BASICO
        composable(route = Rutas.CreateReport.ruta) {
            CreateReportScreen(navController = navController)
        }

        // 4. Pantalla de Dashboard Administrador EJEMPLO BASICO
        composable(route = Rutas.AdminDashboard.ruta) {
            //TODO PONER LA RUTA
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Todos los Reportes (Administrador)")
                Spacer(modifier = Modifier.height(16.dp))
                // Simulamos abrir el reporte con ID "123"
                Button(onClick = { navController.navigate(Rutas.ReportDetail.createRoute("123")) }) {
                    Text("Ver Detalle del Reporte 123")
                }
            }
        }

        // 5. Pantalla de Detalle de Reporte (Recibe argumentos) EJEMPLO BASICO
        composable(
            route = Rutas.ReportDetail.ruta,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Recuperamos el ID que pasamos en la ruta
            val reportId = backStackEntry.arguments?.getString("reportId") ?: "ID desconocido"
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Detalle del Reporte: $reportId")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Volver")
                }
            }
        }
    }
}
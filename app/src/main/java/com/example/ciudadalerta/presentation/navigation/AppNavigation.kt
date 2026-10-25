package com.example.ciudadalerta.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadalerta.data.local.CiudadAlertaDatabase
import com.example.ciudadalerta.data.repository.ReporteRepositoryImpl
import com.example.ciudadalerta.data.repository.UsuarioRepositoryImpl
import com.example.ciudadalerta.data.repository.LocationProviderImpl
import com.example.ciudadalerta.data.repository.FotoStorageImpl
import com.example.ciudadalerta.presentation.screens.administrador.AdminDashboardScreen
import com.example.ciudadalerta.presentation.screens.administrador.AdminDashboardViewModel
import com.example.ciudadalerta.presentation.screens.administrador.AdminDashboardViewModelFactory
import com.example.ciudadalerta.presentation.screens.administrador.ReportDetailScreen
import com.example.ciudadalerta.presentation.screens.administrador.ReportDetailViewModel
import com.example.ciudadalerta.presentation.screens.administrador.ReportDetailViewModelFactory
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListScreen
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListViewModel
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListViewModelFactory
import com.example.ciudadalerta.presentation.screens.reports.CreateReportScreen
import com.example.ciudadalerta.presentation.screens.login.LoginScreen
import com.example.ciudadalerta.presentation.screens.login.LoginState
import com.example.ciudadalerta.presentation.screens.login.LoginViewModel
import com.example.ciudadalerta.presentation.screens.login.LoginViewModelFactory
import com.example.ciudadalerta.presentation.screens.registrer.RegisterScreen
import com.example.ciudadalerta.presentation.screens.registrer.RegisterViewModel
import com.example.ciudadalerta.presentation.screens.registrer.RegisterViewModelFactory
import com.example.ciudadalerta.presentation.screens.reports.CreateReportViewModel
import com.example.ciudadalerta.presentation.screens.reports.CreateReportViewModelFactory


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

    // Obtenemos el contexto de la aplicación, necesario para inicializar Room
    val context = LocalContext.current

    // 1. Instanciamos la Base de Datos y el Repositorio de Usuarios
    val database = CiudadAlertaDatabase.getDatabase(context)
    val usuarioRepository = UsuarioRepositoryImpl(database.usuarioDao())
    val registrorepository = UsuarioRepositoryImpl(database.usuarioDao())
    val reporteRepository = ReporteRepositoryImpl(database.reporteDao())

    val fotoStorage = remember { FotoStorageImpl(context.applicationContext) }
    val locationProvider = remember { LocationProviderImpl(context.applicationContext) }

    // 2. Creamos un Factory para poder inyectar el repositorio al LoginViewModel
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(usuarioRepository)
    )

    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(registrorepository)
    )

    val adminViewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(reporteRepository)
    )

    // El NavHost es el contenedor de las pantallas.
    // Empezaremos en la pantalla de Login.
    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta
    ) {

        // 0. Pantalla de Login
        composable(route = Rutas.Login.ruta) {
            LoginScreen(
                navController = navController,
                viewModel = loginViewModel,
                onNavigateToRegister = {
                    navController.navigate(Rutas.Register.ruta)
                }
            )
        }

        composable(route = Rutas.Register.ruta) {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 2. Pantalla de Lista de Ciudadano
        composable(route = Rutas.CitizenList.ruta) {
            // El usuario logueado vive en el LoginViewModel
            val loginState by loginViewModel.loginState.collectAsState()
            val usuario = (loginState as? LoginState.Success)?.usuario

            if (usuario != null) {
                val citizenListViewModel: CitizenListViewModel = viewModel(
                    key = "citizen_${usuario.id}",
                    factory = CitizenListViewModelFactory(
                        reporteRepository,
                        usuario.nombre,
                        usuario.correo,
                        usuario.id
                    )
                )
                CitizenListScreen(
                    navController = navController,
                    viewModel = citizenListViewModel
                )
            }
        }

        // 3. Pantalla para Crear Reporte EJEMPLO BASICO
        composable(route = Rutas.CreateReport.ruta) {
            val loginState by loginViewModel.loginState.collectAsState()
            val usuario = (loginState as? LoginState.Success)?.usuario

            if (usuario != null) {
                val createReportViewModel: CreateReportViewModel = viewModel(
                    factory = CreateReportViewModelFactory(
                        reporteRepository, locationProvider, fotoStorage, usuario.id
                    )
                )
                CreateReportScreen(
                    navController = navController,
                    viewModel = createReportViewModel
                )
            }
        }

        // 4. Pantalla de Dashboard Administrador EJEMPLO BASICO
        composable(route = Rutas.AdminDashboard.ruta) {
            val loginState by loginViewModel.loginState.collectAsState()
            val usuario = (loginState as? LoginState.Success)?.usuario

            // Solo el rol Administrador ve este panel
            if (usuario != null && usuario.rol == "Administrador") {

                AdminDashboardScreen(
                    navController = navController,
                    viewModel = adminViewModel,
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // 5. Pantalla de Detalle de Reporte (Recibe argumentos) EJEMPLO BASICO
        // 5. Pantalla de Detalle de Reporte (solo Administrador)
        composable(
            route = Rutas.ReportDetail.ruta,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")?.toIntOrNull()
            val loginState by loginViewModel.loginState.collectAsState()
            val usuario = (loginState as? LoginState.Success)?.usuario

            if (usuario != null && usuario.rol == "Administrador" && reportId != null) {
                val detailViewModel: ReportDetailViewModel = viewModel(
                    factory = ReportDetailViewModelFactory(reporteRepository, reportId)
                )
                ReportDetailScreen(
                    navController = navController,
                    viewModel = detailViewModel
                )
            }
        }
    }
}
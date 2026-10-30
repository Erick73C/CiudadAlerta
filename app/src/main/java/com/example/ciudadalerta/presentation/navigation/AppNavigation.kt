package com.example.ciudadalerta.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadalerta.data.local.CiudadAlertaDatabase
import com.example.ciudadalerta.data.local.SessionManager
import com.example.ciudadalerta.data.remote.RetrofitClient
import com.example.ciudadalerta.data.repository.AuthRepositoryImpl

import com.example.ciudadalerta.data.repository.LocationProviderImpl
import com.example.ciudadalerta.data.repository.FotoStorageImpl
import com.example.ciudadalerta.data.repository.ReporteRepositoryImpl
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
 * Define todas las pantallas de la aplicación y arma las dependencias (base de datos local,
 * sesión, API de Supabase y repositorios) que se inyectan a los ViewModels.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 1. Datos locales (reportes) y repositorio de reportes
    val database = CiudadAlertaDatabase.getDatabase(context)
    val reporteRepository = ReporteRepositoryImpl(database.reporteDao())

    val fotoStorage = remember { FotoStorageImpl(context.applicationContext) }
    val locationProvider = remember { LocationProviderImpl(context.applicationContext) }

    // 2. Sesión y autenticación con Supabase
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val authRepository = remember {
        // CLAVE: el interceptor pide el token en cada petición, así que siempre usa el vigente
        val api = RetrofitClient.crearApi { sessionManager.obtenerAccessToken() }
        AuthRepositoryImpl(api, sessionManager)
    }

    // 3. ViewModels compartidos entre pantallas
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authRepository)
    )
    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(authRepository)
    )
    val adminViewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(reporteRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta
    ) {

        // 0. Login (si hay sesión guardada, LoginViewModel pasa a Success y LoginScreen navega sola)
        composable(route = Rutas.Login.ruta) {
            LoginScreen(
                navController = navController,
                viewModel = loginViewModel,
                onNavigateToRegister = { navController.navigate(Rutas.Register.ruta) }
            )
        }

        composable(route = Rutas.Register.ruta) {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // 2. Lista del ciudadano
        composable(route = Rutas.CitizenList.ruta) {
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
                    viewModel = citizenListViewModel,
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // 3. Crear reporte
        composable(route = Rutas.CreateReport.ruta) {
            val loginState by loginViewModel.loginState.collectAsState()
            val usuario = (loginState as? LoginState.Success)?.usuario

            if (usuario != null) {
                val createReportViewModel: CreateReportViewModel = viewModel(
                    factory = CreateReportViewModelFactory(
                        reporteRepository, locationProvider, fotoStorage, usuario.id, usuario.nombre
                    )
                )
                CreateReportScreen(
                    navController = navController,
                    viewModel = createReportViewModel
                )
            }
        }

        // 4. Panel del administrador
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

        // 5. Detalle de un reporte (solo Administrador)
        composable(
            route = Rutas.ReportDetail.ruta,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            // CLAVE: el id ahora es un UUID (texto), ya no se convierte a Int
            val reportId = backStackEntry.arguments?.getString("reportId")
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
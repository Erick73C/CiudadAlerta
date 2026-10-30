package com.example.ciudadalerta.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadalerta.di.AppContainer
import com.example.ciudadalerta.presentation.screens.administrador.AdminDashboardScreen
import com.example.ciudadalerta.presentation.screens.administrador.AdminDashboardViewModel
import com.example.ciudadalerta.presentation.screens.administrador.AdminDashboardViewModelFactory
import com.example.ciudadalerta.presentation.screens.administrador.ReportDetailScreen
import com.example.ciudadalerta.presentation.screens.administrador.ReportDetailViewModel
import com.example.ciudadalerta.presentation.screens.administrador.ReportDetailViewModelFactory
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListScreen
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListViewModel
import com.example.ciudadalerta.presentation.screens.ciudadano.CitizenListViewModelFactory
import com.example.ciudadalerta.presentation.screens.login.LoginScreen
import com.example.ciudadalerta.presentation.screens.login.LoginState
import com.example.ciudadalerta.presentation.screens.login.LoginViewModel
import com.example.ciudadalerta.presentation.screens.login.LoginViewModelFactory
import com.example.ciudadalerta.presentation.screens.registrer.RegisterScreen
import com.example.ciudadalerta.presentation.screens.registrer.RegisterViewModel
import com.example.ciudadalerta.presentation.screens.registrer.RegisterViewModelFactory
import com.example.ciudadalerta.presentation.screens.reports.CreateReportScreen
import com.example.ciudadalerta.presentation.screens.reports.CreateReportViewModel
import com.example.ciudadalerta.presentation.screens.reports.CreateReportViewModelFactory

/**
 * Define todas las pantallas de la aplicación utilizando las dependencias del [AppContainer].
 */
@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()

    // ViewModels compartidos entre pantallas utilizando las dependencias del contenedor
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(container.authRepository)
    )
    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(container.authRepository)
    )
    val adminViewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(container.reporteRepository)
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
                        container.reporteRepository,
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
                        container.reporteRepository,
                        container.locationProvider,
                        container.fotoStorage,
                        usuario.id,
                        usuario.nombre
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
            val reportId = backStackEntry.arguments?.getString("reportId")
            val loginState by loginViewModel.loginState.collectAsState()
            val usuario = (loginState as? LoginState.Success)?.usuario

            if (usuario != null && usuario.rol == "Administrador" && reportId != null) {
                val detailViewModel: ReportDetailViewModel = viewModel(
                    factory = ReportDetailViewModelFactory(container.reporteRepository, reportId)
                )
                ReportDetailScreen(
                    navController = navController,
                    viewModel = detailViewModel
                )
            }
        }
    }
}

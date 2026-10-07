package com.example.ciudadalerta.presentation.navigation

/**
 * Define todas las pantallas disponibles en la aplicación y sus rutas.
 *
 * @author TU NOMBRE
 */
sealed class Rutas(val ruta: String) {
    // Pantalla de inicio de sesión
    object Login : Rutas("login_screen")

    // Flujo del Ciudadano
    object CitizenList : Rutas("citizen_list_screen")
    object CreateReport : Rutas("create_report_screen")

    // Flujo del Administrador
    object AdminDashboard : Rutas("admin_dashboard_screen")

    // Detalle de un reporte (Requiere un parámetro: el ID del reporte)
    object ReportDetail : Rutas("report_detail_screen/{reportId}") {
        // Función auxiliar para construir la ruta con el ID dinámico

        /**
         *
         * Construye la ruta de navegación inyectando el ID del reporte seleccionado.
         * @author TU NOMBRE
         * @param reportId El identificador único del reporte a visualizar.
         * @return La ruta formateada como String (ej. "report_detail_screen/123").
         */
        fun createRoute(reportId: String): String {
            return "report_detail_screen/$reportId"
        }
    }
}
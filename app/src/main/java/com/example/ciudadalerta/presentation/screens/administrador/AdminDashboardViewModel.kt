package com.example.ciudadalerta.presentation.screens.administrador


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.local.entity.ReporteConUsuario
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.presentation.util.aFechaHora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel del panel del administrador: combina todos los reportes de Room
 * con el filtro de estatus elegido y calcula los conteos.
 *
 * @author YO
 */
class AdminDashboardViewModel(
    reporteRepository: ReporteRepository
) : ViewModel() {

    private val filtro = MutableStateFlow(FILTRO_TODOS)

    val uiState: StateFlow<AdminDashboardUiState> = combine(
        reporteRepository.obtenerReportesConUsuario(),
        filtro
    ) { lista, filtroActual ->
        val todos = lista.map { it.toUi() }
        AdminDashboardUiState(
            reportes = if (filtroActual == FILTRO_TODOS) todos
            else todos.filter { it.estatus == filtroActual },
            filtro = filtroActual,
            conteos = todos.groupingBy { it.estatus }.eachCount(),
            total = todos.size,
            cargando = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdminDashboardUiState()
    )

    fun onFiltroChanged(nuevo: String) {
        filtro.value = nuevo
    }
}

private fun ReporteConUsuario.toUi() = AdminReporteUi(
    id = reporte.id,
    titulo = reporte.titulo,
    descripcion = reporte.descripcion,
    categoria = reporte.categoria,
    estatus = reporte.estatus,
    fecha = reporte.fechaCreacion.aFechaHora(),
    nombreUsuario = nombreUsuario ?: "Usuario desconocido",
    urgente = reporte.requiereAtencionInmediata
)
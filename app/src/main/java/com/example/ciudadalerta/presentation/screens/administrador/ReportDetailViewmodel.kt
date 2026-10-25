package com.example.ciudadalerta.presentation.screens.administrador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.local.entity.ReporteConUsuario
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.presentation.util.aFechaHora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del detalle de un reporte para el administrador (RF-08).
 * Observa el reporte en Room y permite cambiar su estatus.
 */
class ReportDetailViewModel(
    private val reporteRepository: ReporteRepository,
    private val reporteId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reporteRepository.obtenerReporteConUsuario(reporteId).collect { datos ->
                val ui = datos?.toDetailUi()
                _uiState.update { actual ->
                    actual.copy(
                        reporte = ui,
                        cargando = false,
                        // El selector arranca en el estatus guardado y no se pisa mientras el admin elige
                        estatusSeleccionado = actual.estatusSeleccionado ?: ui?.estatus
                    )
                }
            }
        }
    }

    fun onEstatusSeleccionado(estatus: String) =
        _uiState.update { it.copy(estatusSeleccionado = estatus) }

    fun actualizarEstatus() {
        val actual = _uiState.value
        val nuevo = actual.estatusSeleccionado ?: return
        if (nuevo == actual.reporte?.estatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                reporteRepository.actualizarEstatus(reporteId, nuevo)
                _uiState.update { it.copy(guardando = false, mensaje = "Estatus actualizado a $nuevo") }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensaje = "No se pudo actualizar el estatus") }
            }
        }
    }

    fun mostrarMensaje(texto: String) = _uiState.update { it.copy(mensaje = texto) }
    fun mensajeMostrado() = _uiState.update { it.copy(mensaje = null) }
}

private fun ReporteConUsuario.toDetailUi() = ReportDetailUi(
    id = reporte.id,
    titulo = reporte.titulo,
    descripcion = reporte.descripcion,
    categoria = reporte.categoria,
    estatus = reporte.estatus,
    fecha = reporte.fechaCreacion.aFechaHora(),
    horaIncidente = reporte.hora,
    nombreUsuario = nombreUsuario ?: "Usuario desconocido",
    urgente = reporte.requiereAtencionInmediata,
    fotoPath = reporte.fotoPath,
    latitud = reporte.latitud,
    longitud = reporte.longitud
)
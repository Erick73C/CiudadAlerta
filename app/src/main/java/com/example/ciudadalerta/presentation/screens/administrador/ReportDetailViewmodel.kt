package com.example.ciudadalerta.presentation.screens.administrador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.presentation.util.aFechaHora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.domain.models.ResultadoFoto

/**
 * ViewModel del detalle de un reporte para el administrador (RF-08).
 * Observa el reporte en Room y permite cambiar su estatus.
 */
class ReportDetailViewModel(
    private val reporteRepository: ReporteRepository,
    private val reporteId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    private var fotoSolicitada: String? = null

    init {
        viewModelScope.launch {
            reporteRepository.obtenerReportePorId(reporteId).collect { datos ->
                val ui = datos?.toDetailUi()
                _uiState.update { actual ->
                    actual.copy(
                        reporte = ui,
                        cargando = false,
                        // El selector arranca en el estatus guardado y no se pisa mientras el admin elige
                        estatusSeleccionado = actual.estatusSeleccionado ?: ui?.estatus
                    )
                }
                // La foto se pide una sola vez por ruta (el collect se repite cada vez que Room cambia)
                ui?.fotoPath?.let { if (it != fotoSolicitada) cargarFoto(it) }
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

    fun reintentarFoto() {
        _uiState.value.reporte?.fotoPath?.let { cargarFoto(it) }
    }

    private fun cargarFoto(path: String) {
        fotoSolicitada = path
        viewModelScope.launch {
            _uiState.update { it.copy(fotoCargando = true, fotoError = null) }
            when (val resultado = reporteRepository.obtenerFoto(path)) {
                is ResultadoFoto.Lista ->
                    _uiState.update { it.copy(fotoCargando = false, fotoRuta = resultado.ruta) }
                ResultadoFoto.SinConexion ->
                    _uiState.update { it.copy(fotoCargando = false, fotoError = "Sin conexión: no se pudo descargar la foto") }
                ResultadoFoto.NoDisponible ->
                    _uiState.update { it.copy(fotoCargando = false, fotoError = "La foto no está disponible") }
            }
        }
    }
}

private fun ReporteEntity.toDetailUi() = ReportDetailUi(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria,
    estatus = estatus,
    fecha = fechaCreacion.aFechaHora(),
    horaIncidente = hora,
    nombreUsuario = nombreUsuario ?: "Usuario desconocido",
    urgente = requiereAtencionInmediata,
    fotoPath = fotoPath,
    latitud = latitud,
    longitud = longitud,
    sincronizacion = estatusSincronizacion
)
package com.example.ciudadalerta.presentation.screens.reports
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.data.local.entity.ReporteEntity
import com.example.ciudadalerta.domain.location.LocationProvider
import com.example.ciudadalerta.domain.models.FotoCaptura
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.domain.storage.FotoStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel del formulario de creación de reportes.
 * Maneja el estado, la foto, la ubicación y el guardado en Room vía [ReporteRepository].
 */
class CreateReportViewModel(
    private val reporteRepository: ReporteRepository,
    private val locationProvider: LocationProvider,
    private val fotoStorage: FotoStorage,
    private val usuarioId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    // Foto reservada para la cámara que todavía no se confirma
    private var fotoPendiente: FotoCaptura? = null
    private var reporteGuardado = false

    fun onTituloChanged(valor: String) = _uiState.update { it.copy(titulo = valor, errorMessage = null) }
    fun onDescripcionChanged(valor: String) = _uiState.update { it.copy(descripcion = valor, errorMessage = null) }
    fun onCategoriaChanged(valor: String) = _uiState.update { it.copy(categoria = valor) }
    fun onUrgenteChanged(valor: Boolean) = _uiState.update { it.copy(requiereAtencionInmediata = valor) }
    fun onHoraChanged(valor: String) = _uiState.update { it.copy(hora = valor) }
    fun mostrarError(mensaje: String) = _uiState.update { it.copy(errorMessage = mensaje) }

    // Foto

    /** Reserva el archivo para la cámara. El composable lanza TakePicture con el uri devuelto. */
    fun prepararFoto(): FotoCaptura {
        fotoPendiente?.let { fotoStorage.eliminarFoto(it.path) }
        return fotoStorage.crearFotoNueva().also { fotoPendiente = it }
    }

    /** Resultado de TakePicture: si se tomó, reemplaza la foto anterior; si no, descarta el archivo. */
    fun onFotoResultado(exito: Boolean) {
        val pendiente = fotoPendiente ?: return
        fotoPendiente = null
        if (exito) {
            _uiState.value.fotoPath?.let { fotoStorage.eliminarFoto(it) }
            _uiState.update { it.copy(fotoPath = pendiente.path, errorMessage = null) }
        } else {
            fotoStorage.eliminarFoto(pendiente.path)
        }
    }

    //Ubicación

    fun obtenerUbicacion() {
        viewModelScope.launch {
            _uiState.update { it.copy(obteniendoUbicacion = true, errorMessage = null) }
            val ubicacion = locationProvider.obtenerUbicacionActual()
            _uiState.update {
                it.copy(
                    obteniendoUbicacion = false,
                    ubicacion = ubicacion ?: it.ubicacion,
                    errorMessage = if (ubicacion == null)
                        "No se pudo obtener la ubicación. Revisa que el GPS esté encendido."
                    else null
                )
            }
        }
    }

    //Guardado

    fun guardarReporte() {
        val s = _uiState.value
        if (s.titulo.isBlank() || s.descripcion.isBlank()) {
            mostrarError("Completa el título y la descripción")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorMessage = null) }
            try {
                reporteRepository.insertarReporte(
                    ReporteEntity(
                        titulo = s.titulo.trim(),
                        descripcion = s.descripcion.trim(),
                        categoria = s.categoria,
                        hora = s.hora ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                        requiereAtencionInmediata = s.requiereAtencionInmediata,
                        usuarioId = usuarioId,
                        latitud = s.ubicacion?.latitud,
                        longitud = s.ubicacion?.longitud,
                        fotoPath = s.fotoPath
                    )
                )
                reporteGuardado = true
                _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, errorMessage = "No se pudo guardar el reporte") }
            }
        }
    }

    /** Si se sale sin guardar, se borran las fotos para no dejar archivos huérfanos. */
    override fun onCleared() {
        fotoPendiente?.let { fotoStorage.eliminarFoto(it.path) }
        if (!reporteGuardado) _uiState.value.fotoPath?.let { fotoStorage.eliminarFoto(it) }
        super.onCleared()
    }
}
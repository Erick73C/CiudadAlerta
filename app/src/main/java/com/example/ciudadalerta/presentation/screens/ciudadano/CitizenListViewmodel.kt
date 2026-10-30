package com.example.ciudadalerta.presentation.screens.ciudadano

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.presentation.util.aFechaHora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo de datos temporal para la UI.
 * @author Erick Omar Perez Gonzalez
 */
data class ReporteDummy(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val estatus: String,
    val fecha: String
)

/**
 * ViewModel que gestiona el estado de la pantalla principal del ciudadano.
 * Se conecta a la base de datos a través del ReporteRepository.
 * @author YO
 */
class CitizenListViewModel(
    private val reporteRepository: ReporteRepository,
    usuarioNombre: String,
    private val usuarioCorreo: String,
    private val usuarioId: String
) : ViewModel() {

    // Estado para el nombre/correo del usuario
    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario.asStateFlow()

    // Estado para la lista de reportes
    private val _reportes = MutableStateFlow<List<ReporteDummy>>(emptyList())
    val reportes: StateFlow<List<ReporteDummy>> = _reportes.asStateFlow()

    init {
        // Establecemos el saludo inicial con el nombre (o el correo si el nombre está vacío)
        _nombreUsuario.value = usuarioNombre.ifBlank { usuarioCorreo }
        // Cargamos los datos reales desde Room
        cargarReportesDesdeBD()
    }

    private fun cargarReportesDesdeBD() {
        viewModelScope.launch {
            // recolectamos el Flow de la base de datos
            reporteRepository.obtenerReportesPorUsuario(usuarioId).collect { listaEntidades ->
                // Mapeamos los datos de la BD (Entity) a nuestro modelo visual (Dummy/UI Model)
                _reportes.value = listaEntidades.map { entidad ->
                    ReporteDummy(
                        id = entidad.id.toString(),
                        titulo = entidad.titulo,
                        descripcion = entidad.descripcion,
                        estatus = entidad.estatus,
                        fecha = entidad.fechaCreacion.aFechaHora()// Por ahora usamos el campo hora
                    )
                }
            }
        }
    }
}
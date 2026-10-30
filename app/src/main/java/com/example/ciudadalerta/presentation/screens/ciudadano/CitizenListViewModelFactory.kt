package com.example.ciudadalerta.presentation.screens.ciudadano

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ciudadalerta.domain.repository.ReporteRepository

/**
 * Permite la inyección manual de dependencias para CitizenListViewModel.
 * Inyecta el repositorio de reportes y los datos del usuario logueado.
 *
 * @property reporteRepository Instancia del repositorio de reportes para la capa de datos.
 * @property usuarioCorreo Correo del usuario que inició sesión.
 * @property usuarioId ID del usuario en la base de datos local.
 * @author YO
 */
class CitizenListViewModelFactory(
    private val reporteRepository: ReporteRepository,
    private val usuarioNombre: String,
    private val usuarioCorreo: String,
    private val usuarioId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CitizenListViewModel::class.java)) {
            return CitizenListViewModel(reporteRepository, usuarioNombre, usuarioCorreo, usuarioId) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
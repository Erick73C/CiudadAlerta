package com.example.ciudadalerta.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ciudadalerta.domain.location.LocationProvider
import com.example.ciudadalerta.domain.repository.ReporteRepository
import com.example.ciudadalerta.domain.storage.FotoStorage

class CreateReportViewModelFactory(
    private val reporteRepository: ReporteRepository,
    private val locationProvider: LocationProvider,
    private val fotoStorage: FotoStorage,
    private val usuarioId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateReportViewModel::class.java)) {
            return CreateReportViewModel(reporteRepository, locationProvider, fotoStorage, usuarioId) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
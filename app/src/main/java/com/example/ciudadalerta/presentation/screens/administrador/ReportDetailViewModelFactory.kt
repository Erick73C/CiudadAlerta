package com.example.ciudadalerta.presentation.screens.administrador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ciudadalerta.domain.repository.ReporteRepository

class ReportDetailViewModelFactory(
    private val reporteRepository: ReporteRepository,
    private val reporteId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportDetailViewModel::class.java)) {
            return ReportDetailViewModel(reporteRepository, reporteId) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
package com.example.ciudadalerta.presentation.screens.administrador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ciudadalerta.domain.repository.ReporteRepository

class AdminDashboardViewModelFactory(
    private val reporteRepository: ReporteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
            return AdminDashboardViewModel(reporteRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
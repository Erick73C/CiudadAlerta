package com.example.ciudadalerta.domain.location

import com.example.ciudadalerta.domain.models.Ubicacion


interface LocationProvider {
    /** Devuelve la ubicación actual o null si no hay permiso, GPS apagado o falla. */
    suspend fun obtenerUbicacionActual(): Ubicacion?
}
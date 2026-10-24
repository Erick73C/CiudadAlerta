package com.example.ciudadalerta.domain.storage

import com.example.ciudadalerta.domain.models.FotoCaptura

interface FotoStorage {
    /** Reserva un archivo nuevo para la foto de un reporte. */
    fun crearFotoNueva(): FotoCaptura

    /** Borra una foto (por ejemplo, si el usuario la descarta o retoma). */
    fun eliminarFoto(path: String)
}
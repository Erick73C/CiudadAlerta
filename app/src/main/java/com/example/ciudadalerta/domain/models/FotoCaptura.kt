package com.example.ciudadalerta.domain.models

/**
 * Foto que se va a tomar.
 * @property uri Dirección que se le entrega a la cámara para escribir la imagen.
 * @property path Ruta absoluta del archivo; es lo que se guarda en Room.
 */
data class FotoCaptura(
    val uri: String,
    val path: String
)

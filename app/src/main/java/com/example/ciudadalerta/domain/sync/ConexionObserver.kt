package com.example.ciudadalerta.domain.sync

import kotlinx.coroutines.flow.Flow

/** Informa en tiempo real si el dispositivo tiene internet. */
interface ConexionObserver {
    val conectado: Flow<Boolean>
}
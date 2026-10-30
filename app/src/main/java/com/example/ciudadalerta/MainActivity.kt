package com.example.ciudadalerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ciudadalerta.presentation.navigation.AppNavigation
import com.example.ciudadalerta.presentation.permissions.PermissionGate
import com.example.ciudadalerta.ui.theme.CiudadAlertaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // CLAVE: la Activity no crea nada, solo recibe el contenedor de la Application
        val container = (application as CiudadAlertaApp).container

        setContent {
            CiudadAlertaTheme {
                PermissionGate {
                    AppNavigation(container = container)
                }
            }
        }
    }
}
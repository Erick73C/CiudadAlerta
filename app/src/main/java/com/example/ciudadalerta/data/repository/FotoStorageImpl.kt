package com.example.ciudadalerta.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import com.example.ciudadalerta.domain.models.FotoCaptura
import com.example.ciudadalerta.domain.storage.FotoStorage
import java.io.File

class FotoStorageImpl(private val context: Context) : FotoStorage {

    override fun crearFotoNueva(): FotoCaptura {
        //Apunta al archivo que esta en resorces en xml
        val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "reporte_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // igual que android:authorities del manifest
            archivo
        )
        return FotoCaptura(uri = uri.toString(), path = archivo.absolutePath)
    }

    override fun eliminarFoto(path: String) {
        File(path).delete()
    }
}
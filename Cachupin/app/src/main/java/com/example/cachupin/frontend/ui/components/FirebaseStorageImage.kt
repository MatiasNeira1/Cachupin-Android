package com.example.cachupin.frontend.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Muestra una imagen almacenada en Firebase Storage.
 *
 * @param storageRefOrGsUrl puede ser:
 *  - Un path relativo en Storage, por ejemplo: "productos/perro/comida1.png"
 *  - O una URL gs:// completa de Firebase Storage.
 */
@Composable
fun FirebaseStorageImage(
    storageRefOrGsUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var hasTried by remember { mutableStateOf(false) }

    LaunchedEffect(storageRefOrGsUrl) {
        if (storageRefOrGsUrl.isBlank()) {
            downloadUrl = null
            hasTried = true
            return@LaunchedEffect
        }

        val storage = FirebaseStorage.getInstance()
        val ref = if (storageRefOrGsUrl.startsWith("gs://")) {
            storage.getReferenceFromUrl(storageRefOrGsUrl)
        } else {
            storage.reference.child(storageRefOrGsUrl)
        }

        downloadUrl = try {
            hasTried = true
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    when {
        downloadUrl != null -> {
            AsyncImage(
                model = downloadUrl,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }

        hasTried -> {
            // Placeholder si no se pudo cargar la imagen
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = contentDescription ?: ""
                )
            }
        }

        else -> {
            // Mientras aún no se resuelve nada, mostramos un contenedor vacío
            Box(modifier = modifier)
        }
    }
}

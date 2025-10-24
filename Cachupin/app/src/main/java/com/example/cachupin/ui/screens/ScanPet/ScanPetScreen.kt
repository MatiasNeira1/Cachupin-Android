package com.example.cachupin.ui.screens.ScanPet

import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPetScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }


    LaunchedEffect(lifecycleOwner) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider = provider
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanea tu Mascota", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¡Escanea la imagen de tu mascota!")
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                factory = { previewView }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                Toast.makeText(context, "Botón de escaneo presionado", Toast.LENGTH_SHORT).show()
            }) {
                Text("Escanear Mascota")
            }
        }
    }
}

@Composable
private fun bindCameraUseCases(
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider?,
    previewView: PreviewView
) {
    val context = LocalContext.current

    if (cameraProvider == null) {
        Toast.makeText(context, "Error: Camera provider no disponible", Toast.LENGTH_SHORT).show()
        return
    }

    val preview = Preview.Builder().build()
    preview.setSurfaceProvider(previewView.surfaceProvider)

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
    } catch (exc: Exception) {
        Toast.makeText(context, "Error al iniciar la cámara: ${exc.message}", Toast.LENGTH_SHORT).show()
    }
}

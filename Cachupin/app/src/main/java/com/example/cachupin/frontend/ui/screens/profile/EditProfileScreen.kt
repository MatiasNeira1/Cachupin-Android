package com.example.cachupin.frontend.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, uid: String) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    // Cargar los datos actuales del perfil
    LaunchedEffect(uid) {
        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            if (doc.exists()) {
                nombre = doc.getString("nombre") ?: ""
                email = doc.getString("email") ?: ""
            }
        } catch (e: Exception) {
            Toast.makeText(navController.context, "Error al cargar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text(text = "Editar detalles de tu perfil", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Campos de texto para editar el nombre y correo
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val userData = mapOf(
                            "nombre" to nombre,
                            "email" to email
                        )
                        db.collection("usuarios")
                            .document(uid)
                            .update(userData)
                            .addOnSuccessListener {
                                Toast.makeText(navController.context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(navController.context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}

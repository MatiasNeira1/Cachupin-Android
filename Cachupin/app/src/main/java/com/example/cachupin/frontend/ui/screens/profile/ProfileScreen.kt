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
import com.example.cachupin.domain.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, uid: String) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(uid) {
        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            if (doc.exists()) {
                userProfile = doc.toObject(UserProfile::class.java)
            }
        } catch (e: Exception) {
            Toast.makeText(navController.context, "Error al cargar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.SemiBold) }
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
                userProfile?.let { profile ->
                    Text(text = "Detalles de tu perfil", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text(text = "Nombre: ${profile.nombre}")
                    Text(text = "Email: ${profile.email}")
                } ?: run {
                    Text(text = "No se pudo cargar el perfil.")
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("edit_profile/$uid")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Perfil")
            }
        }
    }
}

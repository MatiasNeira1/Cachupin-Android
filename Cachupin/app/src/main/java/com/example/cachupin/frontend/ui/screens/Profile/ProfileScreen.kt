package com.example.cachupin.frontend.ui.screens.Profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cachupin.frontend.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    uid: String,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(uid) {
        viewModel.loadProfile(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadProfile(uid) }) {
                            Text("Reintentar")
                        }
                    }
                }

                state.profile != null -> {
                    val p = state.profile!!
                    val createdText = p.createdAt?.let {
                        val df = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        df.format(Date(it))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nombre", fontWeight = FontWeight.Bold)
                            Text(p.nombre.ifBlank { "Sin nombre" })

                            Spacer(Modifier.height(12.dp))

                            Text("Email", fontWeight = FontWeight.Bold)
                            Text(p.email.ifBlank { "Sin email" })

                            Spacer(Modifier.height(12.dp))

                            Text("UID", fontWeight = FontWeight.Bold)
                            Text(p.uid)

                            if (createdText != null) {
                                Spacer(Modifier.height(12.dp))
                                Text("Creado", fontWeight = FontWeight.Bold)
                                Text(createdText)
                            }
                        }
                    }
                }

                else -> {
                    Text("No hay datos de perfil.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

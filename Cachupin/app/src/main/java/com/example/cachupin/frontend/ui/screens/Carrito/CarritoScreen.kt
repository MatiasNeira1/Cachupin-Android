package com.example.cachupin.frontend.ui.screens.Carrito

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cachupin.domain.CarritoItem
import com.example.cachupin.frontend.viewmodel.CartViewModel
import com.example.cachupin.frontend.viewmodel.CartUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavController,
    viewModel: CartViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState: CartUiState = viewModel.uiState
    var isCheckoutDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                val total = uiState.carrito.sumOf { it.precio * it.qty }
                Text(
                    "Total: ${formatCLP(total)}",
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (uiState.carrito.isEmpty()) {
                            Toast.makeText(
                                context,
                                "El carrito está vacío",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            isCheckoutDialogVisible = true
                        }
                    }
                ) {
                    Text("Checkout")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.carrito.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("El carrito está vacío.")
                    }
                }

                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(uiState.carrito) { item ->
                            CarritoItemCard(
                                item = item,
                                onRemove = {
                                    viewModel.removeItem(
                                        item = item,
                                        onSuccess = { msg ->
                                            Toast.makeText(
                                                context,
                                                msg,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { msg ->
                                            Toast.makeText(
                                                context,
                                                msg,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isCheckoutDialogVisible) {
        AlertDialog(
            onDismissRequest = { isCheckoutDialogVisible = false },
            title = { Text("Confirmar Compra") },
            text = { Text("¿Estás seguro de que deseas finalizar tu compra?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.checkout(
                        onSuccess = {
                            isCheckoutDialogVisible = false
                            Toast.makeText(
                                context,
                                "Compra realizada con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onError = { msg ->
                            isCheckoutDialogVisible = false
                            Toast.makeText(
                                context,
                                msg,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(onClick = { isCheckoutDialogVisible = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CarritoItemCard(item: CarritoItem, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Cantidad: ${item.qty}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Precio: ${formatCLP(item.precio * item.qty)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

private fun formatCLP(value: Int): String {
    val cl = Locale.Builder().setLanguage("es").setRegion("CL").build()
    val nf = NumberFormat.getNumberInstance(cl)
    return "$${nf.format(value)}"
}

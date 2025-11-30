package com.example.cachupin.frontend.ui.screens.Carrito

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cachupin.R
import com.example.cachupin.frontend.data.repository.CartStorage
import com.example.cachupin.domain.CarritoItem
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var carrito by remember { mutableStateOf<List<CarritoItem>>(emptyList()) }
    val totalItems = carrito.sumOf { it.qty }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCheckoutDialogVisible by remember { mutableStateOf(false) }

    // Cargar carrito desde Firebase
    LaunchedEffect(Unit) {
        CartStorage.load(
            onResult = { items -> carrito = items },
            onError = { /* log opcional */ }
        )
    }

    // Botón de Checkout
    fun handleCheckout() {
        isCheckoutDialogVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Text("Total: ${formatCLP(carrito.sumOf { it.precio * it.qty })}", modifier = Modifier.weight(1f))
                Button(onClick = { handleCheckout() }) {
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
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                carrito.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("El carrito está vacío.")
                    }
                }
                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(carrito) { item ->
                            CarritoItemCard(
                                item = item,
                                onRemove = {
                                    // Actualizar stock en Firestore
                                    val productRef = db.collection("productos").document(item.nombre)
                                    productRef.get().addOnSuccessListener { doc ->
                                        val stock = doc.getLong("stock")?.toInt() ?: 0
                                        val newStock = stock + item.qty  // Incrementar el stock al eliminar el artículo

                                        // Actualizar el stock
                                        productRef.update("stock", newStock)
                                            .addOnSuccessListener {
                                                // Eliminar producto del carrito
                                                CartStorage.remove(item) { updatedCart ->
                                                    carrito = updatedCart
                                                    Toast.makeText(
                                                        context,
                                                        "${item.nombre} eliminado del carrito",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "No se pudo actualizar el stock",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Confirmación de Checkout
    if (isCheckoutDialogVisible) {
        AlertDialog(
            onDismissRequest = { isCheckoutDialogVisible = false },
            title = { Text("Confirmar Compra") },
            text = { Text("¿Estás seguro de que deseas finalizar tu compra?") },
            confirmButton = {
                Button(onClick = {
                    // Lógica de confirmación de compra
                    isCheckoutDialogVisible = false
                    Toast.makeText(context, "Compra realizada con éxito", Toast.LENGTH_SHORT).show()
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
                Text(item.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("Cantidad: ${item.qty}", style = MaterialTheme.typography.bodySmall)
                Text("Precio: ${formatCLP(item.precio * item.qty)}", style = MaterialTheme.typography.bodyMedium)
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

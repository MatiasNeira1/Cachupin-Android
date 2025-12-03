package com.example.cachupin.frontend.ui.screens.Productos

import android.widget.Toast
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.cachupin.domain.Producto
import com.example.cachupin.frontend.viewmodel.ProductosUiState
import com.example.cachupin.frontend.viewmodel.ProductosViewModel
import java.text.NumberFormat
import java.util.Locale
import com.example.cachupin.frontend.ui.components.FirebaseStorageImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    navController: NavController,
    viewModel: ProductosViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState: ProductosUiState = viewModel.uiState


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cachupin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(badge = {
                        if (uiState.totalItems > 0) Badge {
                            Text(uiState.totalItems.toString())
                        }
                    }) {
                        IconButton(onClick = { navController.navigate("carrito") }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Productos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

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

                uiState.productos.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay productos disponibles.")
                    }
                }

                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(
                            items = uiState.productos,
                            key = { it.id }
                        ) { producto ->
                            ProductoCard(
                                producto = producto,
                                onAddToCart = {
                                    viewModel.onAddToCart(
                                        producto = producto,
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
}

@Composable
fun ProductoCard(

    producto: Producto,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (producto.imageUrl.isNotBlank()) {
                FirebaseStorageImage(
                    storageRefOrGsUrl = producto.imageUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            } else {
                Text("Imagen no disponible")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (producto.descripcion.isNotBlank()) {
                    Text(
                        producto.descripcion,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Categoría: ${producto.categoria}",
                    style = MaterialTheme.typography.labelMedium
                )
                if (producto.material.isNotBlank()) {
                    Text(
                        "Material: ${producto.material}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (producto.peso.isNotBlank()) {
                    Text(
                        "Peso: ${producto.peso}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    formatCLP(producto.precio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onAddToCart,
                    enabled = producto.stock > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (producto.stock > 0) "Añadir al carrito"
                        else "Sin stock"
                    )
                }
            }
        }
    }
}

private fun formatCLP(value: Int): String {
    val cl = Locale.Builder().setLanguage("es").setRegion("CL").build()
    val nf = NumberFormat.getNumberInstance(cl)
    return "$${nf.format(value)}"
}

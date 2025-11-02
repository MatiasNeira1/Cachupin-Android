package com.example.cachupin.ui.screens.Productos

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cachupin.R
import com.example.cachupin.data.repository.CartStorage
import com.example.cachupin.domain.CarritoItem
import java.text.NumberFormat
import java.util.Locale

data class Producto(
    val imageRes: Int,
    val nombre: String,
    val precio: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(navController: NavController? = null) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val productos = listOf(
        Producto(R.drawable.correa_gato, "Correa para gato", 5990),
        Producto(R.drawable.comida_gato, "Comida húmeda", 10990),
        Producto(R.drawable.caja_arena_gato, "Caja de arena para gato", 20990),
        Producto(R.drawable.juguete_gato, "Juguete interactivo", 12990),
        Producto(R.drawable.comida_perro1, "Comida para perro basica", 5990),
        Producto(R.drawable.comida_perro2, "Comida húmeda", 10990),
        Producto(R.drawable.comida_perro3, "Comida perro 10Kg", 20990),
        Producto(R.drawable.comida_perro4, "Comida perro 5Kg", 12990)

    )

    var carrito by remember { mutableStateOf(CartStorage.load(context)) }
    val totalItems = carrito.sumOf { it.qty }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cachupin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Botón para volver a la pantalla anterior
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(badge = {
                        if (totalItems > 0) Badge { Text(totalItems.toString()) }
                    }) {
                        IconButton(onClick = { navController?.navigate("carrito") }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "carrito")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Productos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier =  Modifier.padding(vertical = 8.dp)
                )
            }

            val productosFiltrados = productos.filter {
                it.nombre.contains(searchQuery, ignoreCase = true)
            }

            items(productosFiltrados) { producto ->
                ProductoCard(
                    producto = producto,
                    onAddToCart = {
                        val mutable = carrito.toMutableList()
                        val existing = mutable.find {
                            it.imageRes == producto.imageRes &&
                                    it.nombre == producto.nombre &&
                                    it.precio == producto.precio
                        }
                        if (existing != null) existing.qty++ else {
                            mutable.add(
                                CarritoItem(
                                    imageRes = producto.imageRes,
                                    nombre = producto.nombre,
                                    precio = producto.precio,
                                    qty = 1
                                )
                            )
                        }

                        CartStorage.save(context, mutable)
                        carrito = mutable

                        Toast.makeText(
                            context,
                            "${producto.nombre} añadido al carrito",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
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
            // Imagen del producto
            Image(
                painter = painterResource(id = producto.imageRes),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
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
                Text(
                    formatCLP(producto.precio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
                    Text("Añadir al carrito")
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

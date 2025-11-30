package com.example.cachupin.frontend.ui.screens.Productos

import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cachupin.frontend.data.repository.CartStorage
import com.example.cachupin.domain.CarritoItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale
import android.util.Log
import coil.compose.rememberImagePainter

data class Producto(
    val nombre: String,
    val precio: Int,
    val imageUrl: String,
    val categoria: String,
    val stock: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var carrito by remember { mutableStateOf<List<CarritoItem>>(emptyList()) }
    val totalItems = carrito.sumOf { it.qty }

    // Cargar los productos desde Firestore
    DisposableEffect(Unit) {
        val reg: ListenerRegistration = db.collection("productos")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    // Error al leer los productos
                    errorMessage = e.message ?: "Error al leer productos."
                    productos = emptyList()
                    isLoading = false
                    Log.e("Firestore", "Error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snap == null || snap.isEmpty) {
                    errorMessage = "No se pudieron obtener productos."
                    productos = emptyList()
                    isLoading = false
                    Log.d("Firestore", "No se encontraron productos.")
                    return@addSnapshotListener
                }

                // Mapear los productos usando el campo 'imageUrl'
                val lista = snap.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val precio = (doc.getLong("precio") ?: return@mapNotNull null).toInt()
                    val imageUrl = doc.getString("imagenUrl") ?: return@mapNotNull null
                    val categoria = doc.getString("categoria") ?: return@mapNotNull null
                    val stock = (doc.getLong("stock") ?: return@mapNotNull null).toInt()

                    Producto(nombre, precio, imageUrl, categoria, stock)
                }

                productos = lista
                errorMessage = null
                isLoading = false
            }

        onDispose { reg.remove() }
    }

    // Cargar carrito desde Firebase
    LaunchedEffect(Unit) {
        CartStorage.load(
            onResult = { items -> carrito = items },
            onError = { /* log opcional */ }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cachupin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(badge = {
                        if (totalItems > 0) Badge { Text(totalItems.toString()) }
                    }) {
                        IconButton(onClick = { navController?.navigate("carrito") }) {
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
                productos.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay productos disponibles.")
                    }
                }
                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(productos) { producto ->
                            ProductoCard(
                                producto = producto,
                                onAddToCart = {
                                    if (producto.stock > 0) {
                                        val mutable = carrito.toMutableList()
                                        val idx = mutable.indexOfFirst {
                                            it.nombre == producto.nombre && it.precio == producto.precio
                                        }
                                        if (idx != -1) {
                                            val cur = mutable[idx]
                                            mutable[idx] = cur.copy(qty = cur.qty + 1)
                                        } else {
                                            mutable.add(
                                                CarritoItem(
                                                    nombre = producto.nombre,
                                                    precio = producto.precio,
                                                    qty = 1,
                                                    categoria = producto.categoria,
                                                    imageUrl = producto.imageUrl
                                                )
                                            )
                                        }
                                        val newList = mutable.toList()
                                        CartStorage.save(newList) { ok ->
                                            if (ok) {
                                                carrito = newList
                                                // Reducir stock en Firestore
                                                val newStock = producto.stock - 1
                                                db.collection("productos")
                                                    .document(producto.nombre)
                                                    .update("stock", newStock)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "${producto.nombre} añadido al carrito",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                            context,
                                                            "No se pudo actualizar el stock",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "No se pudo añadir al carrito",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Producto fuera de stock",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
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
            Image(
                painter = rememberImagePainter(producto.imageUrl),
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
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(producto.categoria, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text(formatCLP(producto.precio), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
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

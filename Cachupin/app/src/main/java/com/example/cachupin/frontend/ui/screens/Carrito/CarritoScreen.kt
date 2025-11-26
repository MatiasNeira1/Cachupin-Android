package com.example.cachupin.frontend.ui.screens.Carrito

import android.R
import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cachupin.frontend.data.repository.CartStorage
import com.example.cachupin.domain.CarritoItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(navController: NavController) {

    var items by remember { mutableStateOf<List<CarritoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar carrito desde Firebase
    LaunchedEffect(Unit) {
        CartStorage.load(
            onResult = { list ->
                items = list
                isLoading = false
            },
            onError = {
                isLoading = false
            }
        )
    }

    val total = items.sumOf { it.precio * it.qty }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu carrito", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            CartStorage.clear { ok ->
                                if (ok) items = emptyList()
                            }
                        },
                        enabled = items.isNotEmpty()
                    ) { Text("Vaciar") }
                }
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total: ${formatCLP(total)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(onClick = {
                            // TODO: flujo de pago
                        }) {
                            Text("Comprar")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tu carrito está vacío")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    items(items, key = { "${it.imageRes}-${it.nombre}-${it.precio}" }) { item ->
                        CarritoItemRow(
                            item = item,
                            onInc = {
                                // Incrementar cantidad localmente y guardar en Firebase
                                val updated = items.toMutableList()
                                val idx = updated.indexOfFirst {
                                    it.imageRes == item.imageRes &&
                                            it.nombre == item.nombre &&
                                            it.precio == item.precio
                                }
                                if (idx != -1) {
                                    val current = updated[idx]
                                    updated[idx] = current.copy(qty = current.qty + 1)
                                    val finalList = updated.toList()
                                    items = finalList
                                    CartStorage.save(finalList)
                                }
                            },
                            onDec = {
                                val updated = items.toMutableList()
                                val idx = updated.indexOfFirst {
                                    it.imageRes == item.imageRes &&
                                            it.nombre == item.nombre &&
                                            it.precio == item.precio
                                }
                                if (idx != -1) {
                                    val current = updated[idx]
                                    if (current.qty > 1) {
                                        updated[idx] = current.copy(qty = current.qty - 1)
                                    } else {
                                        updated.removeAt(idx)
                                    }
                                    val finalList = updated.toList()
                                    items = finalList
                                    CartStorage.save(finalList)
                                }
                            },
                            onRemove = {
                                val finalList = items.filterNot {
                                    it.imageRes == item.imageRes &&
                                            it.nombre == item.nombre &&
                                            it.precio == item.precio
                                }
                                items = finalList
                                CartStorage.save(finalList)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(84.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CarritoItemRow(
    item: CarritoItem,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit
) {
    val safeImage = remember(item.imageRes) {
        try {
            item.imageRes
        } catch (e: Resources.NotFoundException) {
            R.drawable.ic_menu_report_image
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = safeImage),
                contentDescription = item.nombre,
                modifier = Modifier.size(84.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium)
                Text(formatCLP(item.precio), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onDec, enabled = item.qty > 1) { Text("–") }
                    Text(
                        text = "${item.qty}",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(onClick = onInc) { Text("+") }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar")
            }
        }
    }
}

fun formatCLP(value: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$${nf.format(value)}"
}

package com.example.cachupin.ui.screens.Carrito

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

data class CarritoItem(
    val imageRes: Int,
    val nombre: String,
    val precio: Int,
    var qty: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(navController: NavController) {
    val context = LocalContext.current
    var items by remember { mutableStateOf(CartStorage.load(context)) }

    if (items == null) items = emptyList()

    val total by remember(items) { mutableStateOf(items.sumOf { it.precio * it.qty }) }

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
                            items = emptyList()
                            CartStorage.save(context, items)
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
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Tu carrito está vacío")
            }
        } else {
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
                            item.qty++
                            items = items.toList()
                            CartStorage.save(context, items)
                        },
                        onDec = {
                            if (item.qty > 1) {
                                item.qty--
                                items = items.toList()
                                CartStorage.save(context, items)
                            }
                        },
                        onRemove = {
                            items = items.filterNot {
                                it.nombre == item.nombre &&
                                        it.imageRes == item.imageRes &&
                                        it.precio == item.precio
                            }
                            CartStorage.save(context, items)
                        }
                    )
                }
                item { Spacer(Modifier.height(84.dp)) }
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
            android.R.drawable.ic_menu_report_image
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

object CartStorage {
    private const val PREFS_NAME = "MyAppPrefs"
    private const val CART_KEY = "carrito"

    fun load(context: Context): List<CarritoItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(CART_KEY, "[]") ?: "[]"

        return try {
            val arr = JSONArray(raw)
            val temp = mutableListOf<CarritoItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)

                // Validar que el recurso exista
                val imageRes = try {
                    o.getInt("imageRes")
                } catch (_: Exception) {
                    android.R.drawable.ic_menu_report_image
                }

                temp.add(
                    CarritoItem(
                        imageRes = imageRes,
                        nombre = o.optString("nombre", "Producto desconocido"),
                        precio = o.optInt("precio", 0),
                        qty = o.optInt("qty", 1)
                    )
                )
            }

            temp.groupBy { Triple(it.imageRes, it.nombre, it.precio) }
                .map { (k, list) ->
                    CarritoItem(
                        imageRes = k.first,
                        nombre = k.second,
                        precio = k.third,
                        qty = list.sumOf { it.qty }
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun save(context: Context, items: List<CarritoItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        try {
            items.forEach {
                val o = JSONObject().apply {
                    put("imageRes", it.imageRes)
                    put("nombre", it.nombre)
                    put("precio", it.precio)
                    put("qty", it.qty)
                }
                arr.put(o)
            }
            prefs.edit().putString(CART_KEY, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun formatCLP(value: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$${nf.format(value)}"
}

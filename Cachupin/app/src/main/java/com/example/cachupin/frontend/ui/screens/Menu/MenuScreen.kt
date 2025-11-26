package com.example.cachupin.frontend.ui.screens.Menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cachupin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DestacadoProducto(
    val imageRes: Int,
    val nombre: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("Invitado") }
    val userEmail = auth.currentUser?.email ?: "Invitado"

    // Obtener el nombre del usuario desde Firestore
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("nombre") ?: "Invitado"
                    }
                }
                .addOnFailureListener {
                    userName = "Invitado"
                }
        }
    }

    val images: List<Int> = listOf(
        R.drawable.correa_gato,
        R.drawable.comida_gato,
        R.drawable.juguete_gato
    )
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(images) {
        while (true) {
            delay(3000)
            currentImageIndex = (currentImageIndex + 1) % images.size
        }
    }

    var destacados by remember { mutableStateOf<List<DestacadoProducto>>(emptyList()) }
    var loadingDestacados by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("productos")
            .limit(4)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val imageKey = doc.getString("imageKey") ?: ""
                    val imageRes = imageResFromKey(imageKey)
                    DestacadoProducto(imageRes = imageRes, nombre = nombre)
                }
                destacados = lista
                loadingDestacados = false
            }
            .addOnFailureListener {
                loadingDestacados = false
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxHeight()) {
                Text(
                    text = "Menú",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = "Hola, $userName",  // Mostrar el nombre del usuario
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Productos") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("productos")
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Carrito") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("carrito")
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Agendar Hora") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("Hora")
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Escanea a tu mascota") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("scanpet")
                        }
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            auth.signOut()
                            drawerState.close()
                            navController.navigate("login") {
                                popUpTo("menu") { inclusive = true }
                            }
                        }
                    }
                )
            }
        },
        scrimColor = MaterialTheme.colorScheme.scrim
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cachupin", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("© 2025 Cachupin")
                        Text("Términos • Privacidad")
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Banner estático
                Banner(
                    img = R.drawable.banner_img,
                    height = 450
                ) {}

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Productos Destacados",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                when {
                    loadingDestacados -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    destacados.isEmpty() -> {
                        Text(
                            text = "No hay productos destacados por ahora.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    else -> {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            items(destacados) { prod ->
                                ProductCard(
                                    imageRes = prod.imageRes,
                                    onClick = {
                                        navController.navigate("productos")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(imageRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(150.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Producto",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun Banner(
    img: Int,
    height: Int = 750,
    mode: ContentScale = ContentScale.Crop,
    position: Alignment = Alignment.Center,
    overlayContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        Image(
            painter = painterResource(id = img),
            contentDescription = "Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = mode
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier.align(position),
            contentAlignment = Alignment.Center
        ) {
            overlayContent()
        }
    }
}

private fun imageResFromKey(key: String): Int =
    when (key) {
        "correa_gato" -> R.drawable.correa_gato
        "comida_gato" -> R.drawable.comida_gato
        "caja_arena_gato" -> R.drawable.caja_arena_gato
        "juguete_gato" -> R.drawable.juguete_gato
        "comida_perro1" -> R.drawable.comida_perro1
        "comida_perro2" -> R.drawable.comida_perro2
        "comida_perro3" -> R.drawable.comida_perro3
        "comida_perro4" -> R.drawable.comida_perro4
        else -> R.drawable.comida_perro1
    }

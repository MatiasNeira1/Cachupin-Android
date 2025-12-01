package com.example.cachupin.frontend.ui.screens.Menu

import androidx.compose.material3.HorizontalDivider
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cachupin.R
import com.example.cachupin.data.repository.MenuDestacado
import com.example.cachupin.frontend.viewmodel.MenuUiState
import com.example.cachupin.frontend.viewmodel.MenuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: MenuViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val uiState: MenuUiState = viewModel.uiState

    val images: List<Int> = listOf(
        R.drawable.banner_img,

    )
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(images) {
        while (true) {
            delay(3000)
            currentImageIndex = (currentImageIndex + 1) % images.size
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
                    text = "Hola, ${uiState.userName}",
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            viewModel.signOut()
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
                // Banner (si quieres usar el carrusel de imágenes, puedes pasarlo aquí)
                Banner(
                    img = images[currentImageIndex],
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
                    uiState.loadingDestacados -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.errorDestacados != null -> {
                        Text(
                            text = uiState.errorDestacados ?: "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    uiState.destacados.isEmpty() -> {
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
                            items(uiState.destacados) { prod: MenuDestacado ->
                                val imageRes = imageResFromKey(prod.imageKey)
                                ProductCard(
                                    imageRes = imageRes,
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

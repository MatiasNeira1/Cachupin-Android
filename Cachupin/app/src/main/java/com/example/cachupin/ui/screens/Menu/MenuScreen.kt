package com.example.cachupin.ui.screens.Menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.cachupin.R
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxHeight()) {
                Text(
                    text = "Menú",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
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
                    {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("Hora") } }
                )
                NavigationDrawerItem(
                    label = { Text("Escanea a tu mascota") },
                    selected = false,
                    {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("scanpet") } }

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

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val productImages = listOf(
                        R.drawable.comida_perro1,
                        R.drawable.comida_perro2,
                        R.drawable.comida_perro3,
                        R.drawable.comida_perro4
                    )
                    items(productImages) { image ->
                        ProductCard(
                            imageRes = image,
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
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            overlayContent()
        }
    }
}



package com.example.cachupin

import com.example.cachupin.frontend.ui.screens.Menu.MenuScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cachupin.frontend.ui.screens.Productos.ProductosScreen
import com.example.cachupin.frontend.ui.screens.Carrito.CarritoScreen
import com.example.cachupin.ui.screens.DatePickerScreen
import com.example.cachupin.frontend.ui.screens.Register.RegisterScreen
import com.example.cachupin.frontend.ui.screens.ScanPet.ScanPetScreen
import com.example.cachupin.frontend.ui.screens.Login.LoginScreen



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Route(val path: String) {
    object Login : Route("login")
    object Menu : Route("menu")
    object Productos : Route("productos")
    object Carrito : Route("carrito")
    object Register : Route("register")
    object ScanPet : Route("scanpet")
    object DatePicker : Route("datepicker")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Login.path) {
        composable(Route.Login.path) { LoginScreen(navController) }
        composable(Route.Menu.path) { MenuScreen(navController) }
        composable(Route.Productos.path) { ProductosScreen(navController) }
        composable(Route.Carrito.path) { CarritoScreen(navController) }
        composable(Route.Register.path) { RegisterScreen(navController) }
        composable(Route.ScanPet.path) { ScanPetScreen(navController) }
        composable(Route.DatePicker.path) { DatePickerScreen(navController) }
    }
}


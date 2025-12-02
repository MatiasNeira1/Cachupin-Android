package com.example.cachupin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cachupin.frontend.ui.screens.Login.LoginScreen
import com.example.cachupin.frontend.ui.screens.Menu.MenuScreen
import com.example.cachupin.frontend.ui.screens.profile.ProfileScreen
import com.example.cachupin.frontend.ui.screens.profile.EditProfileScreen

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
    object Profile : Route("profile/{uid}")
    object EditProfile : Route("edit_profile/{uid}")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Login.path) {
        composable(Route.Login.path) { LoginScreen(navController) }
        composable(Route.Menu.path) { MenuScreen(navController) }
        composable(Route.Profile.path) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            ProfileScreen(navController, uid)
        }
        composable(Route.EditProfile.path) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            EditProfileScreen(navController, uid)
        }
    }
}

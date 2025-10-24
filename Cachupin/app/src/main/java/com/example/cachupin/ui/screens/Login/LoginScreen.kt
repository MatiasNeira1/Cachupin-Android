package com.example.cachupin.ui.screens.Login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.json.JSONArray


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val result = handleLogin(context, email, password)
                if (result == "ok") {
                    error = ""
                    Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    error = result
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}


fun handleLogin(context: Context, email: String, password: String): String {
    if (email.isEmpty() || password.isEmpty()) {
        return "Por favor, ingresa el correo y la contraseña."
    }

    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val usersJson = prefs.getString("users", "[]")
    val usersArray = JSONArray(usersJson)

    for (i in 0 until usersArray.length()) {
        val user = usersArray.getJSONObject(i)
        if (user.getString("email") == email) {
            return if (user.getString("password") == password) {
                "ok"
            } else {
                "Contraseña incorrecta"
            }
        }
    }
    return "Usuario no encontrado"
}

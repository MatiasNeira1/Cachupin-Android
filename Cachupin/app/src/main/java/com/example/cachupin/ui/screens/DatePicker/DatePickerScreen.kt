package com.example.cachupin.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerScreen(navController: NavController? = null) {
    val context = LocalContext.current

    var selectedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }

    val selectedDateText by remember(selectedMillis) {
        mutableStateOf(formatDate(selectedMillis))
    }
    val selectedTimeText by remember(selectedHour, selectedMinute) {
        mutableStateOf(String.format("%02d:%02d", selectedHour, selectedMinute))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Seleccionar Fecha y Hora") }) }
    ) { pv ->
        Column(
            modifier = Modifier
                .padding(pv)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Fecha seleccionada:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = selectedDateText)

            Button(onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH)
                val d = cal.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val picked = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            // Conserva la hora ya elegida
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedMillis = picked.timeInMillis
                    },
                    y, m, d
                ).show()
            }) {
                Text("Elegir Fecha")
            }

            Text(
                text = "Hora seleccionada:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = selectedTimeText)

            Button(onClick = {
                Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedHour = hourOfDay
                        selectedMinute = minute

                        // Actualiza el millis con la hora nueva manteniendo la fecha
                        val tmp = Calendar.getInstance().apply {
                            timeInMillis = selectedMillis
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedMillis = tmp.timeInMillis
                    },
                    selectedHour,
                    selectedMinute,
                    true // 24h
                ).show()
            }) {
                Text("Elegir Hora")
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { navController?.navigate("next_screen") },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) { Text("Guardar") }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

package com.example.cachupin.frontend.ui.screens.DatePicker

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val today = Calendar.getInstance()
    today.add(Calendar.DATE, 1)  // Aseguramos que la fecha seleccionada no sea antes de hoy

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.timeInMillis
    )

    val timePickerState = rememberTimePickerState(
        initialHour = 8,  // Establecemos la hora inicial en 8 AM
        initialMinute = 0,
        is24Hour = true
    )

    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(today.timeInMillis) }
    var selectedTimeMillis by remember { mutableStateOf(today.timeInMillis) }

    val selectedDateText = formatDate(selectedDateMillis)

    val selectedTimeText = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        timePickerState.hour,
        timePickerState.minute
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seleccionar Fecha y Hora") }
            )
        }
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

            Button(onClick = { showDateDialog = true }) {
                Text("Elegir Fecha")
            }


            Button(
                onClick = {
                    val millis = selectedDateMillis
                    if (millis > 0) {
                        navController.navigate("select_time/$millis")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Seleccionar horario")
            }
        }
    }

    if (showDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                    showDateDialog = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTimeMillis = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }.timeInMillis
                    showTimeDialog = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Seleccionar hora") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}


private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

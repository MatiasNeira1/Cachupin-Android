package com.example.cachupin.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // --- Estados de DatePicker y TimePicker (Material 3) ---
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true
    )

    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    val selectedDateText = datePickerState.selectedDateMillis
        ?.let { formatDate(it) }
        ?: "Selecciona una fecha"

    val selectedTimeText = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        timePickerState.hour,
        timePickerState.minute
    )

    val selectedDateTimeMillis by remember(
        datePickerState.selectedDateMillis,
        timePickerState.hour,
        timePickerState.minute
    ) {
        mutableStateOf(
            datePickerState.selectedDateMillis?.let { baseDate ->
                Calendar.getInstance().apply {
                    timeInMillis = baseDate
                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    set(Calendar.MINUTE, timePickerState.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        )
    }

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

            Text(
                text = "Hora seleccionada:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = selectedTimeText)

            Button(onClick = { showTimeDialog = true }) {
                Text("Elegir Hora")
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    navController?.navigate("next_screen")
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Guardar")
            }
        }
    }

    if (showDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = { showDateDialog = false }) {
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

    // -------- Dialogo de HORA (Material3) --------
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(onClick = { showTimeDialog = false }) {
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

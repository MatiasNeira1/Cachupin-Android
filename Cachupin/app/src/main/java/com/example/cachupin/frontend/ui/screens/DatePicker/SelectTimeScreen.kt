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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTimeScreen(navController: NavController, millis: Long, uid: String) {
    val context = LocalContext.current // Aquí obtienes el contexto

    val REQUEST_CODE_PERMISSIONS = 100

    val startHour = 8
    val endHour = 18
    val availableHours = (startHour..endHour).map { hour ->
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Hora", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Seleccione una hora entre las 8:00 AM y las 6:00 PM", fontWeight = FontWeight.Bold)

            availableHours.forEach { hourMillis ->
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(hourMillis))
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_CALENDAR
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as Activity,
                                arrayOf(Manifest.permission.WRITE_CALENDAR),
                                REQUEST_CODE_PERMISSIONS
                            )
                        } else {
                            saveEventToCalendar(context, hourMillis, uid) // Pasa el contexto aquí
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agendar a las $formattedTime")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun saveEventToCalendar(context: Context, millis: Long, uid: String) {
    // Guardar la cita en el calendario
    val contentResolver = context.contentResolver

    val startDate = Calendar.getInstance().apply { timeInMillis = millis }
    val endDate = Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.HOUR_OF_DAY, 1) // Evento de 1 hora
    }

    val eventTitle = "Hora agendada veterinario: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))}"

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startDate.timeInMillis)
        put(CalendarContract.Events.DTEND, endDate.timeInMillis)
        put(CalendarContract.Events.TITLE, eventTitle)
        put(CalendarContract.Events.DESCRIPTION, "Hora agendada para el veterinario a las ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))}")
        put(CalendarContract.Events.CALENDAR_ID, 1)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    try {
        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        if (uri != null) {
            Toast.makeText(context, "Evento guardado en el calendario", Toast.LENGTH_SHORT).show()

            // Guardar la cita en Firebase
            saveEventInFirebase(uid, millis, context)
        } else {
            Toast.makeText(context, "Error al guardar el evento", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun saveEventInFirebase(uid: String, millis: Long, context: Context) {
    val db = FirebaseFirestore.getInstance()

    val citaData = mutableMapOf<String, Any>(
        "usuarioId" to uid,
        "fecha" to millis,
        "titulo" to "Cita Veterinario",
        "descripcion" to "Hora agendada para el veterinario",
        "hora" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    )

    db.collection("citas")
        .add(citaData)
        .addOnSuccessListener { documentReference ->
            Toast.makeText(context, "Cita guardada en Firebase", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al guardar la cita: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}


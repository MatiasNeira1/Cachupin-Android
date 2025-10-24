package com.example.cachupin.ui.screens.DatePicker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity

// MainActivity remains the same
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    DatePickerScreen(navController = rememberNavController()) // Pass NavController instance here
                }
            }
        }
    }
}

@Composable
fun DatePickerScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Selecciona una fecha", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            showDatePicker(context) { date ->
                selectedDate = date
            }
        }) {
            Text("Seleccionar Fecha")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedDate.isNotEmpty()) {
            Text("Fecha seleccionada: $selectedDate", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setCalendarConstraints(
            CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build()
        )
        .setTitleText("Seleccionar fecha")
        .build()

    datePicker.addOnPositiveButtonClickListener { selection ->

        val date = Date(selection)

        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val formattedDate = format.format(date)

        onDateSelected(formattedDate)
    }

    val activity = context as? AppCompatActivity
    activity?.supportFragmentManager?.let { fragmentManager ->
        datePicker.show(fragmentManager, "DATE_PICKER_TAG")
    }
}

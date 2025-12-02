package com.example.cachupin.data.repository

import android.content.Context
import android.widget.Toast
import com.example.cachupin.domain.Cita
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.FirebaseApp
import java.text.SimpleDateFormat
import java.util.*

class CitaRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "cachupin-319c4")

    fun saveEventInFirebase(uid: String, millis: Long, context: Context) {
        val cita = Cita(
            usuarioId = uid,
            fecha = millis,
            titulo = "Cita Veterinario",
            descripcion = "Hora agendada para el veterinario",
            hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
        )

        db.collection("citas")
            .add(cita)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(context, "Cita guardada en Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al guardar la cita: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

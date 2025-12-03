package com.example.cachupin.backend.data.repository

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

) {

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onError(task.exception ?: Exception("Error al registrar"))
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                val uid = user?.uid
                if (uid == null) {
                    onError(IllegalStateException("No se pudo obtener el UID del usuario"))
                    return@addOnCompleteListener
                }

                val userData = hashMapOf(
                    "email" to email,
                    "uid" to uid,
                    "nombre" to name,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("usuarios")
                    .document(uid)
                    .set(userData)
                    .addOnFailureListener { e ->
                        Log.e("AuthRepository", "Error guardando datos de usuario", e)
                    }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,      // devuelve el nombre del usuario
        onError: (Throwable) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onError(task.exception ?: Exception("Correo o contraseña incorrectos"))
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                val uid = user?.uid ?: run {
                    onError(IllegalStateException("No se pudo obtener el usuario actual"))
                    return@addOnCompleteListener
                }

                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val userName = doc.getString("nombre") ?: "Usuario"
                            onSuccess(userName)
                        } else {
                            onError(IllegalStateException("Usuario no encontrado en Firestore"))
                        }
                    }
                    .addOnFailureListener { e ->
                        onError(e)
                    }
            }
    }
}
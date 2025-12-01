package com.example.cachupin.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class MenuDestacado(
    val nombre: String,
    val imageKey: String
)

class MenuRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getUserProfile(
        onResult: (name: String, email: String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult("Invitado", "Invitado")
            return
        }

        val uid = currentUser.uid
        val email = currentUser.email ?: "Invitado"

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = if (doc.exists()) {
                    doc.getString("nombre") ?: "Invitado"
                } else {
                    "Invitado"
                }
                onResult(name, email)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun loadDestacados(
        limit: Long = 4,
        onResult: (List<MenuDestacado>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        db.collection("productos")
            .limit(limit)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val imageKey = doc.getString("imageKey") ?: ""
                    MenuDestacado(
                        nombre = nombre,
                        imageKey = imageKey
                    )
                }
                onResult(lista)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun signOut() {
        auth.signOut()
    }
}

package com.example.cachupin.backend.data.repository

import android.util.Log
import com.example.cachupin.domain.MenuDestacado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
            .orderBy("nombre")
            .limit(limit)
            .get()
            .addOnSuccessListener { result ->
                android.util.Log.d("MenuRepository", "Docs en productos: ${result.size()}")

                val lista = result.documents.map { doc ->
                    val nombre = doc.getString("nombre") ?: doc.id
                    val imageUrl = doc.getString("imageUrl") ?: ""

                    MenuDestacado(
                        nombre = nombre,
                        imageUrl = imageUrl
                    )
                }

                android.util.Log.d("MenuRepository", "Destacados mapeados: ${lista.size}")
                onResult(lista)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MenuRepository", "Error cargando destacados", e)
                onError(e)
            }
    }


    fun signOut() {
        auth.signOut()
    }
}

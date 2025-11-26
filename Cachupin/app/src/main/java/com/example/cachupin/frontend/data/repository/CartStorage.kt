package com.example.cachupin.frontend.data.repository

import com.example.cachupin.domain.CarritoItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object CartStorage {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun cartDocRef() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("carts").document(uid)
        }

    fun load(
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val docRef = cartDocRef()
        if (docRef == null) {
            onResult(emptyList())
            return
        }

        docRef.get()
            .addOnSuccessListener { snapshot ->
                val items = mutableListOf<CarritoItem>()

                @Suppress("UNCHECKED_CAST")
                val rawList = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()

                for (m in rawList) {
                    val imageRes = (m["imageRes"] as? Long)?.toInt() ?: 0
                    val nombre = m["nombre"] as? String ?: ""
                    val precio = (m["precio"] as? Long)?.toInt() ?: 0
                    val qty = (m["qty"] as? Long)?.toInt() ?: 1

                    items.add(
                        CarritoItem(
                            imageRes = imageRes,
                            nombre = nombre,
                            precio = precio,
                            qty = qty
                        )
                    )
                }

                // Agrupar por producto por si vienen repetidos
                val consolidados = items
                    .groupBy { Triple(it.imageRes, it.nombre, it.precio) }
                    .map { (k, list) ->
                        CarritoItem(
                            imageRes = k.first,
                            nombre = k.second,
                            precio = k.third,
                            qty = list.sumOf { it.qty }
                        )
                    }

                onResult(consolidados)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    /**
     * Guarda el carrito completo en Firebase (sobrescribe).
     */
    fun save(
        items: List<CarritoItem>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val docRef = cartDocRef()
        if (docRef == null) {
            onComplete(false)
            return
        }

        val mapped = items.map {
            mapOf(
                "imageRes" to it.imageRes,
                "nombre" to it.nombre,
                "precio" to it.precio,
                "qty" to it.qty
            )
        }

        val data = mapOf("items" to mapped)

        docRef.set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Vacía el carrito en Firebase.
     */
    fun clear(onComplete: (Boolean) -> Unit = {}) {
        save(emptyList(), onComplete)
    }
}

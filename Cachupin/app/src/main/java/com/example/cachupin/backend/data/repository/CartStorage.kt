package com.example.cachupin.backend.data.repository

import com.example.cachupin.domain.CarritoItem
import com.google.firebase.firestore.FirebaseFirestore

object CartStorage {
    private val db = FirebaseFirestore.getInstance()

    fun load(onResult: (List<CarritoItem>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("carrito")
            .get()
            .addOnSuccessListener { snapshot ->
                val carrito = snapshot.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val precio = (doc.getLong("precio") ?: return@mapNotNull null).toInt()
                    val qty = (doc.getLong("qty") ?: return@mapNotNull null).toInt()
                    val categoria = doc.getString("categoria") ?: return@mapNotNull null
                    val imageUrl = doc.getString("imageUrl") ?: return@mapNotNull null

                    CarritoItem(nombre, precio, qty, categoria, imageUrl)
                }
                onResult(carrito)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun save(carrito: List<CarritoItem>, onResult: (Boolean) -> Unit) {
        val batch = db.batch()
        carrito.forEachIndexed { index, item ->
            val docRef = db.collection("carrito").document("item_$index")
            batch.set(
                docRef,
                mapOf(
                    "nombre" to item.nombre,
                    "precio" to item.precio,
                    "qty" to item.qty,
                    "categoria" to item.categoria,
                    "imageUrl" to item.imageUrl
                )
            )
        }
        batch.commit().addOnCompleteListener { task ->
            onResult(task.isSuccessful)
        }
    }

    fun remove(
        item: CarritoItem,
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("carrito")
            .whereEqualTo("nombre", item.nombre)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    load(onResult = onResult, onError = onError)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        updateProductStock(item)
                        load(onResult = onResult, onError = onError)
                    }
                    .addOnFailureListener { e ->
                        onError(e)
                    }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    private fun updateProductStock(item: CarritoItem) {
        val productRef = db.collection("productos").document(item.nombre)
        productRef.get().addOnSuccessListener { doc ->
            val stock = doc.getLong("stock")?.toInt() ?: 0
            val newStock = stock + item.qty

            productRef.update("stock", newStock)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
        }
    }
}

package com.example.cachupin.data.repository

import com.example.cachupin.domain.CarritoItem
import com.google.firebase.firestore.FirebaseFirestore

class CartRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun loadCart(
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CartStorage.load(
            onResult = onResult,
            onError = onError
        )
    }

    fun removeItem(
        item: CarritoItem,
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val productRef = db.collection("productos").document(item.nombre)

        productRef.get()
            .addOnSuccessListener { doc ->
                val stockActual = doc.getLong("stock")?.toInt() ?: 0
                val newStock = stockActual + item.qty

                productRef.update("stock", newStock)
                    .addOnSuccessListener {
                        CartStorage.remove(item) { updatedCart ->
                            onResult(updatedCart)
                        }
                    }
                    .addOnFailureListener { e ->
                        onError(e)
                    }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun checkout(
        carrito: List<CarritoItem>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {

        onSuccess()
    }
}

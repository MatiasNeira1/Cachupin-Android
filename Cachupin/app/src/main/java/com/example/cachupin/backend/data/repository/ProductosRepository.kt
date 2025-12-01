package com.example.cachupin.backend.data.repository

import com.example.cachupin.domain.CarritoItem
import com.example.cachupin.domain.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log
import com.google.firebase.FirebaseApp

class ProductosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "cachupin-319c4")
) {

    fun listenProductos(
        onResult: (List<Producto>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        Log.d("FIREBASE_CFG",
            "projectId=${FirebaseApp.getInstance().options.projectId} " +
                    "appId=${FirebaseApp.getInstance().options.applicationId}"
        )
        return db.collection("productos")
            .orderBy("nombre")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e("ProductosRepository", "Listener error", e)
                    onError(e)
                    return@addSnapshotListener
                }

                Log.d(
                    "ProductosRepository",
                    "snap=${snap != null} size=${snap?.size() ?: -1} fromCache=${snap?.metadata?.isFromCache}"
                )

                if (snap == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                snap.documents.forEach { doc ->
                    Log.d("ProductosRepository", "DOC ${doc.id} => ${doc.data}")
                }

                val lista = snap.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Producto::class.java)?.copy(id = doc.id)
                    } catch (ex: Exception) {
                        Log.e(
                            "ProductosRepository",
                            "toObject falló doc=${doc.id} data=${doc.data}",
                            ex
                        )
                        null
                    }
                }

                onResult(lista)
            }
    }

    /**
     * Carga el carrito desde el almacenamiento local (CartStorage).
     */
    fun loadCart(
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CartStorage.load(
            onResult = onResult,
            onError = onError
        )
    }

    /**
     * Añade un producto al carrito y actualiza el stock en Firestore.
     */
    fun addToCart(
        producto: Producto,
        currentCart: List<CarritoItem>,
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (producto.stock <= 0) {
            onError(IllegalStateException("Producto fuera de stock"))
            return
        }

        val mutable = currentCart.toMutableList()
        val idx = mutable.indexOfFirst {
            it.nombre == producto.nombre && it.precio == producto.precio
        }

        if (idx != -1) {
            val cur = mutable[idx]
            mutable[idx] = cur.copy(qty = cur.qty + 1)
        } else {
            mutable.add(
                CarritoItem(
                    nombre = producto.nombre,
                    precio = producto.precio,
                    qty = 1,
                    categoria = producto.categoria,
                    imageUrl = producto.imageUrl
                )
            )
        }

        val newList = mutable.toList()

        // Guardar el carrito
        CartStorage.save(newList) { ok ->
            if (!ok) {
                onError(IllegalStateException("No se pudo guardar el carrito"))
                return@save
            }

            // Actualizar stock en Firestore
            val newStock = producto.stock - 1
            val docId = if (producto.id.isNotBlank()) producto.id else producto.nombre

            db.collection("productos")
                .document(docId)
                .update("stock", newStock)
                .addOnSuccessListener {
                    onResult(newList)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        }
    }
}

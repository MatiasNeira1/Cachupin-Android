package com.example.cachupin.data.repository

import com.example.cachupin.domain.CarritoItem
import com.example.cachupin.domain.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProductosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Escucha en tiempo real los productos de Firestore.
     * Devuelve un ListenerRegistration para poder detener la escucha.
     */
    fun listenProductos(
        onResult: (List<Producto>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return db.collection("productos")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }

                if (snap == null || snap.isEmpty) {
                    // Sin productos, devolvemos lista vacía (no es error crítico)
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val lista = snap.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val precio = (doc.getLong("precio") ?: return@mapNotNull null).toInt()
                    val imageUrl = doc.getString("imagenUrl")
                        ?: doc.getString("imageUrl")
                        ?: return@mapNotNull null
                    val categoria = doc.getString("categoria") ?: ""
                    val stock = (doc.getLong("stock") ?: 0L).toInt()

                    Producto(
                        nombre = nombre,
                        precio = precio,
                        imageUrl = imageUrl,
                        categoria = categoria,
                        stock = stock
                    )
                }

                onResult(lista)
            }
    }

    /**
     * Carga el carrito desde almacenamiento local (CartStorage).
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
     * Añade un producto al carrito:
     *  - Actualiza la lista de CarritoItem
     *  - Guarda el carrito en CartStorage
     *  - Actualiza el stock en Firestore
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

        CartStorage.save(newList) { ok ->
            if (!ok) {
                onError(IllegalStateException("No se pudo guardar el carrito"))
                return@save
            }

            val newStock = producto.stock - 1
            db.collection("productos")
                .document(producto.nombre)
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

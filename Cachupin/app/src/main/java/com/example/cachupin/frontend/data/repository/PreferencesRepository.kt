package com.example.cachupin.frontend.data.repository

import android.content.Context
import com.example.cachupin.domain.CarritoItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreferencesRepository(
    @Suppress("unused") private val context: Context   // ya no se usa, pero lo dejo para no romper tu código
) {

    companion object {
        private const val CARTS_COLLECTION = "carts"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun cartDoc() =
        auth.currentUser?.uid?.let { uid ->
            db.collection(CARTS_COLLECTION).document(uid)
        }

    fun loadCart(
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val docRef = cartDoc()
        if (docRef == null) {
            // No hay usuario logueado
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
                            nombre   = nombre,
                            precio   = precio,
                            qty      = qty
                        )
                    )
                }

                // Agrupamos por seguridad (evitar duplicados)
                val consolidados = items
                    .groupBy { Triple(it.imageRes, it.nombre, it.precio) }
                    .map { (k, list) ->
                        CarritoItem(
                            imageRes = k.first,
                            nombre   = k.second,
                            precio   = k.third,
                            qty      = list.sumOf { it.qty }
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
    fun saveCart(
        items: List<CarritoItem>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val docRef = cartDoc()
        if (docRef == null) {
            onComplete(false)
            return
        }

        val mapped = items.map {
            mapOf(
                "imageRes" to it.imageRes,
                "nombre"   to it.nombre,
                "precio"   to it.precio,
                "qty"      to it.qty
            )
        }

        val data = mapOf("items" to mapped)

        docRef.set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Agrega o incrementa un item.
     */
    fun addOrIncrement(
        imageRes: Int,
        nombre: String,
        precio: Int,
        onComplete: (Boolean) -> Unit = {}
    ) {
        loadCart(
            onResult = { current ->
                val list = current.toMutableList()
                val existing = list.find {
                    it.imageRes == imageRes &&
                            it.nombre == nombre &&
                            it.precio == precio
                }
                if (existing != null) {
                    existing.qty += 1
                } else {
                    list.add(
                        CarritoItem(
                            imageRes = imageRes,
                            nombre   = nombre,
                            precio   = precio,
                            qty      = 1
                        )
                    )
                }
                saveCart(list, onComplete)
            },
            onError = { onComplete(false) }
        )
    }

    /**
     * Disminuye qty o elimina si llega a 0.
     */
    fun decrementOrRemove(
        imageRes: Int,
        nombre: String,
        precio: Int,
        onComplete: (Boolean) -> Unit = {}
    ) {
        loadCart(
            onResult = { current ->
                val list = current.toMutableList()
                val idx = list.indexOfFirst {
                    it.imageRes == imageRes &&
                            it.nombre == nombre &&
                            it.precio == precio
                }
                if (idx != -1) {
                    val item = list[idx]
                    if (item.qty > 1) {
                        item.qty -= 1
                    } else {
                        list.removeAt(idx)
                    }
                    saveCart(list, onComplete)
                } else {
                    onComplete(false)
                }
            },
            onError = { onComplete(false) }
        )
    }

    /**
     * Elimina un item concreto.
     */
    fun remove(
        imageRes: Int,
        nombre: String,
        precio: Int,
        onComplete: (Boolean) -> Unit = {}
    ) {
        loadCart(
            onResult = { current ->
                val list = current.filterNot {
                    it.imageRes == imageRes &&
                            it.nombre == nombre &&
                            it.precio == precio
                }
                saveCart(list, onComplete)
            },
            onError = { onComplete(false) }
        )
    }

    /**
     * Vacía el carrito.
     */
    fun clearCart(onComplete: (Boolean) -> Unit = {}) {
        saveCart(emptyList(), onComplete)
    }
}

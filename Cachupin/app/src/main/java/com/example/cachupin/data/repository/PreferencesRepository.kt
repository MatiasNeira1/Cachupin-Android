package com.example.cachupin.data.repository

import android.content.Context
import com.example.cachupin.domain.CarritoItem
import org.json.JSONArray
import org.json.JSONObject

class PreferencesRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val CART_KEY   = "carrito"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Carga el carrito desde SharedPreferences.
     *  Si hay entradas sin qty, asume 1 y agrupa por (imageRes, nombre, precio).
     */
    fun loadCart(): List<CarritoItem> {
        val raw = prefs.getString(CART_KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val tmp = mutableListOf<CarritoItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            tmp.add(
                CarritoItem(
                    imageRes = o.getInt("imageRes"),
                    nombre   = o.getString("nombre"),
                    precio   = o.getInt("precio"),
                    qty      = if (o.has("qty")) o.getInt("qty") else 1
                )
            )
        }
        // Agrupa por seguridad (evita duplicados antiguos)
        return tmp.groupBy { Triple(it.imageRes, it.nombre, it.precio) }
            .map { (k, list) ->
                CarritoItem(
                    imageRes = k.first,
                    nombre   = k.second,
                    precio   = k.third,
                    qty      = list.sumOf { it.qty }
                )
            }
    }

    /** Guarda el carrito completo (con qty). */
    fun saveCart(items: List<CarritoItem>) {
        val arr = JSONArray()
        items.forEach {
            val o = JSONObject().apply {
                put("imageRes", it.imageRes)
                put("nombre",   it.nombre)
                put("precio",   it.precio)
                put("qty",      it.qty)
            }
            arr.put(o)
        }
        prefs.edit().putString(CART_KEY, arr.toString()).apply()
    }

    /** Agrega o incrementa un item (útil desde UI). */
    fun addOrIncrement(imageRes: Int, nombre: String, precio: Int) {
        val list = loadCart().toMutableList()
        val existing = list.find { it.imageRes == imageRes && it.nombre == nombre && it.precio == precio }
        if (existing != null) {
            existing.qty += 1
        } else {
            list.add(CarritoItem(imageRes, nombre, precio, qty = 1))
        }
        saveCart(list)
    }

    /** Disminuye qty (mínimo 1). Si queda en 0, elimina. */
    fun decrementOrRemove(imageRes: Int, nombre: String, precio: Int) {
        val list = loadCart().toMutableList()
        val idx = list.indexOfFirst { it.imageRes == imageRes && it.nombre == nombre && it.precio == precio }
        if (idx != -1) {
            val item = list[idx]
            if (item.qty > 1) item.qty -= 1 else list.removeAt(idx)
            saveCart(list)
        }
    }

    /** Elimina un item concreto. */
    fun remove(imageRes: Int, nombre: String, precio: Int) {
        val list = loadCart().filterNot { it.imageRes == imageRes && it.nombre == nombre && it.precio == precio }
        saveCart(list)
    }

    /** Vacía el carrito. */
    fun clearCart() {
        prefs.edit().remove(CART_KEY).apply()
    }
}

package com.example.cachupin.data.repository

import android.content.Context
import com.example.cachupin.domain.CarritoItem
import org.json.JSONArray
import org.json.JSONObject

object CartStorage {
    private const val PREFS_NAME = "MyAppPrefs"
    private const val CART_KEY = "carrito"

    fun load(context: Context): List<CarritoItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(CART_KEY, "[]") ?: "[]"

        // Añadir validación para evitar excepciones
        return try {
            val arr = JSONArray(raw)
            val temp = mutableListOf<CarritoItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                temp.add(
                    CarritoItem(
                        imageRes = o.getInt("imageRes"),
                        nombre = o.getString("nombre"),
                        precio = o.getInt("precio"),
                        qty = if (o.has("qty")) o.getInt("qty") else 1
                    )
                )
            }
            temp.groupBy { Triple(it.imageRes, it.nombre, it.precio) }
                .map { (k, list) ->
                    CarritoItem(
                        imageRes = k.first,
                        nombre = k.second,
                        precio = k.third,
                        qty = list.sumOf { it.qty }
                    )
                }
        } catch (e: Exception) {
            // Manejo de error si los datos están corruptos
            e.printStackTrace()
            emptyList()
        }
    }

    fun save(context: Context, items: List<CarritoItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        items.forEach {
            val o = JSONObject().apply {
                put("imageRes", it.imageRes)
                put("nombre", it.nombre)
                put("precio", it.precio)
                put("qty", it.qty)
            }
            arr.put(o)
        }
        prefs.edit().putString(CART_KEY, arr.toString()).apply()
    }
}


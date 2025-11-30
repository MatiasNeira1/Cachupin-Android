package com.example.cachupin.frontend.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cachupin.domain.CarritoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object PreferencesRepository {
    private const val PREF_NAME = "CachupinPrefs"
    private const val KEY_USER_LOGGED_IN = "userLoggedIn"
    private const val KEY_CART = "userCart"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setUserLoggedIn(context: Context, loggedIn: Boolean) {
        getSharedPreferences(context).edit { putBoolean(KEY_USER_LOGGED_IN, loggedIn) }
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_USER_LOGGED_IN, false)
    }

    fun saveCart(context: Context, carrito: List<CarritoItem>) {
        val gson = Gson()
        val cartJson = gson.toJson(carrito)
        getSharedPreferences(context).edit { putString(KEY_CART, cartJson) }
    }

    fun loadCart(context: Context): List<CarritoItem> {
        val gson = Gson()
        val cartJson = getSharedPreferences(context).getString(KEY_CART, null)
        return if (cartJson != null) {
            val type = object : TypeToken<List<CarritoItem>>() {}.type
            gson.fromJson(cartJson, type)
        } else {
            emptyList()
        }
    }

    fun clearCart(context: Context) {
        getSharedPreferences(context).edit { remove(KEY_CART) }
    }
}

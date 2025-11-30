package com.example.cachupin.domain

data class CarritoItem(
    val nombre: String,        // Nombre del producto
    val precio: Int,           // Precio del producto
    var qty: Int,              // Cantidad del producto en el carrito
    val categoria: String,     // Categoría del producto
    val imageUrl: String       // URL de la imagen del producto
)

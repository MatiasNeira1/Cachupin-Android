package com.example.cachupin.domain

data class CarritoItem(
    val nombre: String,
    val precio: Int,
    var qty: Int,
    val categoria: String,
    val imageUrl: String
)


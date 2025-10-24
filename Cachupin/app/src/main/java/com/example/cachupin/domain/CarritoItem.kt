package com.example.cachupin.domain

data class CarritoItem(
    val imageRes: Int,
    val nombre: String,
    val precio: Int,
    var qty: Int
)

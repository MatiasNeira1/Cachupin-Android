package com.example.cachupin.domain

data class Producto(
    val nombre: String = "",
    val precio: Int = 0,
    val imageUrl: String = "",
    val categoria: String = "",
    val stock: Int = 0
)

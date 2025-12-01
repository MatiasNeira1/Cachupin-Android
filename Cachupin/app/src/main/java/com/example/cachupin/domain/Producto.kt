package com.example.cachupin.domain

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val imageUrl: String = "",
    val peso: String = "",
    val precio: Int = 0,
    val material: String = "",
    val stock: Int = 0
)


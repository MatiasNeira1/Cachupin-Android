package com.example.cachupin.domain

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Producto @JvmOverloads constructor(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var categoria: String = "",

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imagenUrl: String = "",

    var peso: String = "",
    var precio: Int = 0,
    var material: String = "",
    var stock: Int = 0
)

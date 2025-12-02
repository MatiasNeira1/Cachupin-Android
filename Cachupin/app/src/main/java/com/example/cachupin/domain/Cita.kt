package com.example.cachupin.domain

data class Cita(
    val usuarioId: String = "",
    val fecha: Long = 0L,
    val titulo: String = "",
    val descripcion: String = "",
    val hora: String = ""
)
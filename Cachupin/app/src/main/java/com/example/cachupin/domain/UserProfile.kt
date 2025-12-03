package com.example.cachupin.domain

data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val createdAt: Long? = null
)

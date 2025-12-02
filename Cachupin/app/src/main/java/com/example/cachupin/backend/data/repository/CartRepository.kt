package com.example.cachupin.backend.data.repository

import com.example.cachupin.domain.CarritoItem

class CartRepository {

    fun loadCart(
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CartStorage.load(
            onResult = onResult,
            onError = { e -> onError(e) }
        )
    }

    fun removeItem(
        item: CarritoItem,
        onResult: (List<CarritoItem>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CartStorage.remove(
            item = item,
            onResult = onResult,
            onError = { e -> onError(e) }
        )
    }

    fun checkout(
        carrito: List<CarritoItem>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {

        onSuccess()
    }
}

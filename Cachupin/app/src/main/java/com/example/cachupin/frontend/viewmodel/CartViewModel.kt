package com.example.cachupin.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cachupin.data.repository.CartRepository
import com.example.cachupin.domain.CarritoItem

data class CartUiState(
    val carrito: List<CarritoItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CartViewModel(
    private val repository: CartRepository = CartRepository()
) : ViewModel() {

    var uiState by mutableStateOf(CartUiState(isLoading = true))
        private set

    init {
        loadCart()
    }

    private fun loadCart() {
        repository.loadCart(
            onResult = { items ->
                uiState = uiState.copy(
                    carrito = items,
                    isLoading = false,
                    errorMessage = null
                )
            },
            onError = {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el carrito"
                )
            }
        )
    }

    fun removeItem(
        item: CarritoItem,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        repository.removeItem(
            item = item,
            onResult = { updatedCart ->
                uiState = uiState.copy(carrito = updatedCart)
                onSuccess("${item.nombre} eliminado del carrito")
            },
            onError = {
                onError("No se pudo actualizar el stock")
            }
        )
    }

    fun checkout(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        repository.checkout(
            carrito = uiState.carrito,
            onSuccess = {
                onSuccess()
            },
            onError = {
                onError("Error al realizar la compra")
            }
        )
    }
}

package com.example.cachupin.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cachupin.data.repository.ProductosRepository
import com.example.cachupin.domain.CarritoItem
import com.example.cachupin.domain.Producto
import com.google.firebase.firestore.ListenerRegistration

data class ProductosUiState(
    val productos: List<Producto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val carrito: List<CarritoItem> = emptyList()
) {
    val totalItems: Int get() = carrito.sumOf { it.qty }
}

class ProductosViewModel(
    private val repository: ProductosRepository = ProductosRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ProductosUiState())
        private set

    private var listener: ListenerRegistration? = null

    init {
        listenProductos()
        loadCart()
    }

    private fun listenProductos() {
        uiState = uiState.copy(isLoading = true)

        listener = repository.listenProductos(
            onResult = { lista ->
                uiState = uiState.copy(
                    productos = lista,
                    isLoading = false,
                    errorMessage = null
                )
            },
            onError = { e ->
                uiState = uiState.copy(
                    productos = emptyList(),
                    isLoading = false,
                    errorMessage = e.message ?: "Error al leer productos."
                )
            }
        )
    }

    private fun loadCart() {
        repository.loadCart(
            onResult = { items ->
                uiState = uiState.copy(carrito = items)
            },
            onError = {
                // Si quieres, podrías setear un mensaje de error general aquí
            }
        )
    }

    fun onAddToCart(
        producto: Producto,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (producto.stock <= 0) {
            onError("Producto fuera de stock")
            return
        }

        repository.addToCart(
            producto = producto,
            currentCart = uiState.carrito,
            onResult = { newCart ->
                uiState = uiState.copy(carrito = newCart)
                onSuccess("${producto.nombre} añadido al carrito")
            },
            onError = { e ->
                onError(e.message ?: "No se pudo añadir al carrito")
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}

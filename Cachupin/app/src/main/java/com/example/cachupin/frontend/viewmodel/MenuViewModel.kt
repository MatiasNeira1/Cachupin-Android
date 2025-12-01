package com.example.cachupin.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cachupin.data.repository.MenuDestacado
import com.example.cachupin.data.repository.MenuRepository

data class MenuUiState(
    val userName: String = "Invitado",
    val userEmail: String = "Invitado",
    val destacados: List<MenuDestacado> = emptyList(),
    val loadingDestacados: Boolean = false,
    val errorDestacados: String? = null
)

class MenuViewModel(
    private val repository: MenuRepository = MenuRepository()
) : ViewModel() {

    var uiState by mutableStateOf(MenuUiState())
        private set

    init {
        loadUser()
        loadDestacados()
    }

    private fun loadUser() {
        repository.getUserProfile(
            onResult = { name, email ->
                uiState = uiState.copy(
                    userName = name,
                    userEmail = email
                )
            },
            onError = {
                uiState = uiState.copy(
                    userName = "Invitado",
                    userEmail = "Invitado"
                )
            }
        )
    }

    private fun loadDestacados() {
        uiState = uiState.copy(loadingDestacados = true, errorDestacados = null)

        repository.loadDestacados(
            onResult = { lista ->
                uiState = uiState.copy(
                    destacados = lista,
                    loadingDestacados = false,
                    errorDestacados = null
                )
            },
            onError = { e ->
                uiState = uiState.copy(
                    destacados = emptyList(),
                    loadingDestacados = false,
                    errorDestacados = e.message ?: "Error al cargar destacados."
                )
            }
        )
    }

    fun signOut() {
        repository.signOut()
    }
}

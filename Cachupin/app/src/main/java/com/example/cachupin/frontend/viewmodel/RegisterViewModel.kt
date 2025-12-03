package com.example.cachupin.frontend.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cachupin.backend.data.repository.AuthRepository

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        uiState = uiState.copy(confirmPassword = value, errorMessage = null)
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun register(
        onSuccess: (String) -> Unit,          // 👈 ahora devuelve el nombre
        onErrorToast: (String) -> Unit
    ) {
        val state = uiState
        var error: String? = null

        when {
            state.name.isBlank() ||
                    state.email.isBlank() ||
                    state.password.isBlank() ||
                    state.confirmPassword.isBlank() ->
                error = "Completa todos los campos"

            !isEmailValid(state.email) ->
                error = "Correo electrónico no válido"

            !isPasswordValid(state.password) ->
                error = "La contraseña debe tener al menos 6 caracteres"

            state.password != state.confirmPassword ->
                error = "Las contraseñas no coinciden"
        }

        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            onErrorToast(error)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        repository.registerUser(
            name = state.name,
            email = state.email,
            password = state.password,
            onSuccess = {
                val nameToShow = state.name.ifBlank { "Usuario" }
                uiState = uiState.copy(isLoading = false)
                onSuccess(nameToShow)
            },
            onError = { e ->
                val msg = e.message ?: "Error al registrar"
                uiState = uiState.copy(isLoading = false, errorMessage = msg)
                onErrorToast(msg)
            }
        )
    }
}
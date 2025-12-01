package com.example.cachupin.frontend.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cachupin.backend.data.repository.AuthRepository

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(
        onSuccess: (String) -> Unit,     // recibe el nombre del usuario
        onErrorToast: (String) -> Unit
    ) {
        val state = uiState
        var error: String? = null

        when {
            state.email.isBlank() || state.password.isBlank() ->
                error = "Por favor, completa todos los campos."

            !isEmailValid(state.email) ->
                error = "El correo electrónico no es válido."
        }

        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            onErrorToast(error)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        repository.loginUser(
            email = state.email,
            password = state.password,
            onSuccess = { userName ->
                uiState = uiState.copy(isLoading = false)
                onSuccess(userName)
            },
            onError = { e ->
                val msg = e.message ?: "Correo o contraseña incorrectos"
                uiState = uiState.copy(isLoading = false, errorMessage = msg)
                onErrorToast(msg)
            }
        )
    }
}

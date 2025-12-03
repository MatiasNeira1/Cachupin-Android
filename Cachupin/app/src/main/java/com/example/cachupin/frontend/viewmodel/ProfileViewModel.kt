package com.example.cachupin.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cachupin.backend.data.repository.ProfileRepository
import com.example.cachupin.domain.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun loadProfile(uid: String) {
        if (uid.isBlank()) {
            uiState = uiState.copy(errorMessage = "UID inválido")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        repository.getProfileByUid(
            uid = uid,
            onSuccess = { profile ->
                uiState = uiState.copy(isLoading = false, profile = profile)
            },
            onError = { e ->
                uiState = uiState.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar perfil")
            }
        )
    }
}

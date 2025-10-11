package com.example.swipy.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.models.*
import com.example.swipy.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInUserId: String? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(loggedInUserId = repo.currentUserIdOrNull()))
    val state: StateFlow<AuthUiState> = _state

    fun login(creds: Credentials) {
        if (!Validators.email(creds.email)) return setError("Email invalide")
        if (!Validators.password(creds.password)) return setError("8 caractères minimum")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val res = repo.login(creds.email, creds.password)
            _state.update {
                res.fold(
                    onSuccess = { id -> it.copy(isLoading = false, loggedInUserId = id) },
                    onFailure = { e -> it.copy(isLoading = false, error = e.message) }
                )
            }
        }
    }

    fun register(data: RegisterData) {
        if (!Validators.email(data.email)) return setError("Email invalide")
        if (!Validators.password(data.password)) return setError("8 caractères minimum")
        if (data.password != data.confirm) return setError("Les mots de passe ne correspondent pas")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val res = repo.register(data.email, data.password)
            _state.update {
                res.fold(
                    onSuccess = { id -> it.copy(isLoading = false, loggedInUserId = id) },
                    onFailure = { e -> it.copy(isLoading = false, error = e.message) }
                )
            }
        }
    }

    fun logout() = viewModelScope.launch { repo.logout(); _state.update { AuthUiState() } }
    fun clearError() = _state.update { it.copy(error = null) }
    private fun setError(msg: String) { _state.update { it.copy(error = msg) } }
}

package com.example.swipy.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.domain.models.Credentials
import com.example.swipy.domain.models.RegisterData
import com.example.swipy.domain.models.User
import com.example.swipy.domain.models.Validators
import com.example.swipy.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInUser: User? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state


    fun login(creds: Credentials) {
        if (!Validators.email(creds.email)) {
            return setError("Email invalide")
        }
        if (!Validators.password(creds.password)) {
            return setError("8 caractères minimum")
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val res = repo.login(creds.email, creds.password)

            if (res.isSuccess) {
                val user = res.getOrNull()
                _state.update { it.copy(isLoading = false, loggedInUser = user) }
            } else {
                val msg = res.exceptionOrNull()?.message ?: "Erreur inconnue"
                _state.update { it.copy(isLoading = false, error = msg) }
            }
        }
    }

    fun register(data: RegisterData) {
        if (!Validators.email(data.email)) {
            return setError("Email invalide")
        }
        if (!Validators.password(data.password)) {
            return setError("8 caractères minimum")
        }
        if (data.password != data.confirm) {
            return setError("Les mots de passe ne correspondent pas")
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val res = repo.register(
                email = data.email,
                password = data.password,
                firstname = data.firstname,
                lastname = data.lastname,
                age = data.age,
                gender = data.gender,
                bio = data.bio.ifBlank { null },
                city = data.city.ifBlank { null },
                country = data.country.ifBlank { null },
                latitude = data.latitude,
                longitude = data.longitude,
                photos = data.photos
            )

            if (res.isSuccess) {
                val user = res.getOrNull()
                _state.update { it.copy(isLoading = false, loggedInUser = user) }
            } else {
                val msg = res.exceptionOrNull()?.message ?: "Erreur inconnue"
                _state.update { it.copy(isLoading = false, error = msg) }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        repo.logout()
        _state.update { AuthUiState() }
    }

    private fun setError(msg: String) {
        _state.update { it.copy(error = msg) }
    }
}

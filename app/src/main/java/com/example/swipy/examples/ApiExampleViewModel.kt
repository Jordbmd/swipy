package com.example.swipy.examples

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.remote.ApiRepository
import com.example.swipy.data.remote.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ApiUiState(
    val isLoading: Boolean = false,
    val users: List<UserResponse> = emptyList(),
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class ApiExampleViewModel : ViewModel() {
    
    private val apiRepository = ApiRepository()
    
    private val _state = MutableStateFlow(ApiUiState())
    val state: StateFlow<ApiUiState> = _state
    
    fun fetchUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = apiRepository.getUsers()
            
            result.onSuccess { users ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    users = users,
                    error = null
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Une erreur est survenue"
                )
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = apiRepository.login(email, password)
            
            result.onSuccess { loginResponse ->
                android.util.Log.d("ApiExample", "Login success: Token = ${loginResponse.token}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    loginSuccess = true,
                    error = null
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    loginSuccess = false,
                    error = exception.message ?: "Échec de connexion"
                )
            }
        }
    }
    
    fun register(email: String, password: String, firstname: String, lastname: String, age: Int, gender: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = apiRepository.register(email, password, firstname, lastname, age, gender)
            
            result.onSuccess { user ->
                android.util.Log.d("ApiExample", "Registration success: User ID = ${user.id}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = null
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Échec de l'inscription"
                )
            }
        }
    }
}

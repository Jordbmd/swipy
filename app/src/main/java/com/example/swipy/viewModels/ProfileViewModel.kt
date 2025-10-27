package com.example.swipy.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.models.User
import com.example.swipy.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    fun loadProfile(userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = repository.getUserById(userId)
            if (user != null) {
                _state.update { it.copy(isLoading = false, user = user) }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Utilisateur non trouvé") }
            }
        }
    }

    fun updateProfile(
        userId: Int,
        firstname: String,
        lastname: String,
        age: Int,
        bio: String,
        city: String,
        country: String,
        maxDistance: Int,
        photos: List<String>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val updatedUser = repository.updateUser(
                userId = userId,
                firstname = firstname,
                lastname = lastname,
                age = age,
                bio = bio,
                city = city,
                country = country,
                maxDistance = maxDistance,
                photos = photos
            )
            
            _state.update { 
                it.copy(
                    isLoading = false, 
                    user = updatedUser,
                    successMessage = "Profil mis à jour !"
                ) 
            }
            
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(successMessage = null) }
        }
    }
}


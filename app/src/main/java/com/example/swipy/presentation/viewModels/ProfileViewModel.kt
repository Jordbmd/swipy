package com.example.swipy.presentation.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.AuthRepositoryImpl
import com.example.swipy.domain.utils.LocationData
import com.example.swipy.domain.utils.LocationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isLoadingLocation: Boolean = false,
    val locationSuggestions: List<LocationData> = emptyList()
)

class ProfileViewModel(
    private val repository: AuthRepositoryImpl,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state
    
    private val locationManager = LocationManager(context)

    fun updateProfile(
        userId: Int,
        firstname: String,
        lastname: String,
        age: Int,
        bio: String,
        city: String,
        country: String,
        latitude: Double?,
        longitude: Double?,
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
                latitude = latitude,
                longitude = longitude,
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

            delay(2000)
            _state.update { it.copy(successMessage = null) }
        }
    }
    
    fun getCurrentLocation(onResult: (LocationData?, String?) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLocation = true) }
            
            val result = locationManager.getCurrentLocation()
            
            result.onSuccess { locationData ->
                _state.update { 
                    it.copy(
                        isLoadingLocation = false,
                        successMessage = "Localisation récupérée !"
                    ) 
                }
                onResult(locationData, null)
                
                delay(2000)
                _state.update { it.copy(successMessage = null) }
            }.onFailure { error ->
                _state.update { 
                    it.copy(
                        isLoadingLocation = false,
                        errorMessage = "Impossible de récupérer la localisation"
                    ) 
                }
                onResult(null, error.message)
                
                delay(2000)
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }
    
    fun searchLocation(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _state.update { it.copy(locationSuggestions = emptyList()) }
                return@launch
            }
            
            val result = locationManager.searchLocation(query)
            
            result.onSuccess { suggestions ->
                _state.update { it.copy(locationSuggestions = suggestions) }
            }.onFailure {
                _state.update { it.copy(locationSuggestions = emptyList()) }
            }
        }
    }
    
    fun validateLocation(city: String, country: String, onResult: (LocationData?, String?) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLocation = true) }
            
            val result = locationManager.validateLocation(city, country)
            
            result.onSuccess { locationData ->
                _state.update { 
                    it.copy(
                        isLoadingLocation = false,
                        successMessage = "Localisation validée !"
                    ) 
                }
                onResult(locationData, null)
                
                delay(2000)
                _state.update { it.copy(successMessage = null) }
            }.onFailure { error ->
                _state.update { 
                    it.copy(
                        isLoadingLocation = false,
                        errorMessage = "Localisation invalide"
                    ) 
                }
                onResult(null, error.message)
                
                delay(2000)
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }
    
    fun clearLocationSuggestions() {
        _state.update { it.copy(locationSuggestions = emptyList()) }
    }
    
    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }
}


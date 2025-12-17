package com.example.swipy.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.SwipeRepositoryImpl
import com.example.swipy.data.local.datasource.FilterPreferences
import com.example.swipy.domain.utils.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwipeUiState(
    val isLoading: Boolean = false,
    val profiles: List<User> = emptyList(),
    val currentProfileIndex: Int = 0,
    val error: String? = null,
    val matchedUser: User? = null
)

class SwipeViewModel(
    private val swipeRepository: SwipeRepositoryImpl,
    private val filterPreferences: FilterPreferences,
    private val locationManager: LocationManager,
    private val currentUserId: Int
) : ViewModel() {
    
    private val _state = MutableStateFlow(SwipeUiState())
    val state: StateFlow<SwipeUiState> = _state.asStateFlow()

    private val _minAge = MutableStateFlow(18)
    private val _maxAge = MutableStateFlow(99)
    private val _maxDistance = MutableStateFlow(10000f)
    private val _preferredGender = MutableStateFlow<String?>(null)
    
    val minAge: StateFlow<Int> = _minAge.asStateFlow()
    val maxAge: StateFlow<Int> = _maxAge.asStateFlow()
    val maxDistance: StateFlow<Float> = _maxDistance.asStateFlow()
    val preferredGender: StateFlow<String?> = _preferredGender.asStateFlow()
    
    init {
        viewModelScope.launch {
            filterPreferences.minAge.collect { _minAge.value = it }
        }
        viewModelScope.launch {
            filterPreferences.maxAge.collect { _maxAge.value = it }
        }
        viewModelScope.launch {
            filterPreferences.maxDistance.collect { _maxDistance.value = it }
        }
        viewModelScope.launch {
            filterPreferences.preferredGender.collect { _preferredGender.value = it }
        }
        loadProfiles()
    }
    
    fun loadProfiles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val locationResult = locationManager.getCurrentLocation()
            
            val location = locationResult.getOrElse {
                Log.w("SwipeViewModel", "Failed to get location, using default (0,0)", it)
                com.example.swipy.domain.utils.LocationData(0.0, 0.0, "", "")
            }
            
            Log.d("SwipeViewModel", "Loading profiles with location: lat=${location.latitude}, lon=${location.longitude}")
            Log.d("SwipeViewModel", "Filters: minAge=${_minAge.value}, maxAge=${_maxAge.value}, maxDistance=${_maxDistance.value}")
            
            val result = swipeRepository.getUsersForSwipe(
                _minAge.value,
                _maxAge.value,
                _maxDistance.value,
                location.latitude,
                location.longitude
            )
            
            result.onSuccess { profiles ->
                Log.d("SwipeViewModel", "Loaded ${profiles.size} profiles")
                _state.update { it.copy(isLoading = false, profiles = profiles, currentProfileIndex = 0) }
            }.onFailure { error ->
                Log.e("SwipeViewModel", "Error loading profiles", error)
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun swipeRight() {
        val currentIndex = _state.value.currentProfileIndex
        val profiles = _state.value.profiles
        
        if (currentIndex < profiles.size) {
            val likedUser = profiles[currentIndex]
            Log.d("SwipeViewModel", "Swiping right on ${likedUser.firstname} (id: ${likedUser.id})")
            viewModelScope.launch {
                val isMatch = swipeRepository.likeUser(currentUserId, likedUser.id)
                Log.d("SwipeViewModel", "Is match: $isMatch")
                if (isMatch) {
                    Log.d("SwipeViewModel", "🎉 Setting matchedUser in state: ${likedUser.firstname}")
                    _state.update { it.copy(matchedUser = likedUser) }
                }
            }
            moveToNextProfile()
        }
    }
    
    fun clearMatch() {
        Log.d("SwipeViewModel", "Clearing match")
        _state.update { it.copy(matchedUser = null) }
    }

    fun swipeLeft() {
        val currentIndex = _state.value.currentProfileIndex
        val profiles = _state.value.profiles
        
        if (currentIndex < profiles.size) {
            val dislikedUser = profiles[currentIndex]
            viewModelScope.launch {
                swipeRepository.dislikeUser(currentUserId, dislikedUser.id)
            }
            moveToNextProfile()
        }
    }

    private fun moveToNextProfile() {
        _state.update { 
            it.copy(currentProfileIndex = it.currentProfileIndex + 1)
        }
        
        if (_state.value.currentProfileIndex >= _state.value.profiles.size) {
            loadProfiles()
        }
    }

    fun getCurrentProfile(): User? {
        val state = _state.value
        return if (state.currentProfileIndex < state.profiles.size) {
            state.profiles[state.currentProfileIndex]
        } else {
            null
        }
    }
    
    fun updateFilters(minAge: Int, maxAge: Int, maxDistance: Float, preferredGender: String? = null) {
        viewModelScope.launch {
            filterPreferences.updateMinAge(minAge)
            filterPreferences.updateMaxAge(maxAge)
            filterPreferences.updateMaxDistance(maxDistance)
            filterPreferences.updatePreferredGender(preferredGender)
            _minAge.value = minAge
            _maxAge.value = maxAge
            _maxDistance.value = maxDistance
            _preferredGender.value = preferredGender
            loadProfiles()
        }
    }
}

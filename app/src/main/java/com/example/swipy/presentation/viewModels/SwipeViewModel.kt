package com.example.swipy.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.domain.models.User
import com.example.swipy.data.repository.SwipeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwipeUiState(
    val isLoading: Boolean = false,
    val profiles: List<User> = emptyList(),
    val currentProfileIndex: Int = 0,
    val error: String? = null,
    val matchedUser: User? = null
)

class SwipeViewModel(private val repo: SwipeRepositoryImpl, private val currentUserId: Int) : ViewModel() {

    private val _state = MutableStateFlow(SwipeUiState())
    val state: StateFlow<SwipeUiState> = _state

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val profiles = repo.getPotentialMatches(currentUserId)
            Log.d("SwipeViewModel", "Loaded ${profiles.size} profiles for user $currentUserId")
            _state.update { it.copy(isLoading = false, profiles = profiles, currentProfileIndex = 0) }
        }
    }

    fun swipeRight() {
        val currentIndex = _state.value.currentProfileIndex
        val profiles = _state.value.profiles
        
        if (currentIndex < profiles.size) {
            val likedUser = profiles[currentIndex]
            Log.d("SwipeViewModel", "Swiping right on ${likedUser.firstname} (id: ${likedUser.id})")
            viewModelScope.launch {
                val isMatch = repo.likeUser(currentUserId, likedUser.id)
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
                repo.dislikeUser(currentUserId, dislikedUser.id)
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
}

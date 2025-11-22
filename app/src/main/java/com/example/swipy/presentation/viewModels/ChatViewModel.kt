package com.example.swipy.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipy.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversation: com.example.swipy.domain.models.Conversation? = null,
    val messages: List<com.example.swipy.domain.models.Message> = emptyList(),
    val isSending: Boolean = false
)

class ChatViewModel(private val repo: ChatRepository, private val currentUserId: Int) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var currentConversationId: Long? = null

    fun loadConversation(withUserId: Int) {
        viewModelScope.launch {
            val conv = repo.getOrCreateConversationBetween(currentUserId, withUserId)
            currentConversationId = conv.id
            _state.value = _state.value.copy(conversation = conv)

            repo.observeMessages(conv.id).collect { msgs ->
                _state.value = _state.value.copy(messages = msgs)
            }
        }
    }

    fun sendMessage(text: String) {
        val convId = currentConversationId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true)
            val res = repo.sendMessage(convId, currentUserId, text)
            _state.value = _state.value.copy(isSending = false)
            // mark read for receiver not necessary here
        }
    }

    fun markRead() {
        val convId = currentConversationId ?: return
        viewModelScope.launch {
            repo.markConversationRead(convId, currentUserId)
        }
    }
}

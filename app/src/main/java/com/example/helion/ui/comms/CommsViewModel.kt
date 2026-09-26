package com.example.helion.ui.comms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.CommsConversation
import com.example.helion.core.model.UniverseMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommsUiState(
    val isLoading: Boolean = false,
    val conversations: List<CommsConversation> = emptyList(),
    val selectedConversation: CommsConversation? = null,
    val messages: List<UniverseMessage> = emptyList(),
    val isSending: Boolean = false,
    val messageInput: String = ""
)

class CommsViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(CommsUiState(isLoading = true))
    val uiState: StateFlow<CommsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = container.commsRepository.refreshConversations()
            val list = res.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                conversations = list
            )
        }
    }

    fun selectConversation(conv: CommsConversation) {
        _uiState.value = _uiState.value.copy(selectedConversation = conv)
        viewModelScope.launch {
            val res = container.commsRepository.loadMessages(conv.conversationId)
            _uiState.value = _uiState.value.copy(messages = res.getOrDefault(emptyList()))
        }
    }

    fun closeConversation() {
        _uiState.value = _uiState.value.copy(selectedConversation = null, messages = emptyList())
        loadConversations()
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(messageInput = text)
    }

    fun sendMessage() {
        val conv = _uiState.value.selectedConversation ?: return
        val text = _uiState.value.messageInput.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, messageInput = "")
            val res = container.commsRepository.sendMessage(conv.conversationId, text)
            res.onSuccess {
                val updatedList = container.commsRepository.loadMessages(conv.conversationId)
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    messages = updatedList.getOrDefault(_uiState.value.messages)
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSending = false)
            }
        }
    }
}

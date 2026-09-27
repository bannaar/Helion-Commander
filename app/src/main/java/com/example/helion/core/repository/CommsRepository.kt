package com.example.helion.core.repository

import com.example.helion.core.model.CommsConversation
import com.example.helion.core.model.UniverseMessage
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommsRepository(
    private val api: CompanionApi
) {
    private val _conversations = MutableStateFlow<List<CommsConversation>>(emptyList())
    val conversations: StateFlow<List<CommsConversation>> = _conversations.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<UniverseMessage>>(emptyList())
    val activeMessages: StateFlow<List<UniverseMessage>> = _activeMessages.asStateFlow()

    suspend fun refreshConversations(): Result<List<CommsConversation>> {
        val res = api.getConversations()
        res.onSuccess { _conversations.value = it }
        return res
    }

    suspend fun loadMessages(conversationId: String): Result<List<UniverseMessage>> {
        val res = api.getMessages(conversationId)
        res.onSuccess { _activeMessages.value = it }
        return res
    }

    suspend fun sendMessage(conversationId: String, body: String): Result<UniverseMessage> {
        val res = api.sendMessage(conversationId, body)
        res.onSuccess { newMsg ->
            _activeMessages.value = _activeMessages.value + newMsg
            refreshConversations()
        }
        return res
    }

    fun clearEnvironmentState() {
        _conversations.value = emptyList()
        _activeMessages.value = emptyList()
    }
}

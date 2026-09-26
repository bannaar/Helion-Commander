package com.example.helion.core.model

enum class ConversationType(val label: String) {
    DIRECT("Direct Comms"),
    GUILD("Guild Secure"),
    ALLIANCE("Alliance Command"),
    FLEET("Squadron Tactical"),
    SYSTEM("Local Broadcast"),
    TRADE("Trade Sub-Band"),
    GROUP("Private Group")
}

enum class DeliveryState {
    PENDING,
    DELIVERED,
    FAILED
}

data class UniverseMessage(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val senderDisplayName: String,
    val senderCallSign: String,
    val sentAtEpoch: Long,
    val body: String,
    val deliveryState: DeliveryState = DeliveryState.DELIVERED,
    val isRead: Boolean = true
)

data class CommsConversation(
    val conversationId: String,
    val title: String,
    val type: ConversationType,
    val lastMessagePreview: String,
    val lastMessageTimeEpoch: Long,
    val unreadCount: Int = 0,
    val participantsSummary: String = ""
)

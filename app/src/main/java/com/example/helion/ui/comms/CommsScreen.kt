package com.example.helion.ui.comms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.CommsConversation
import com.example.helion.core.model.ConversationType
import com.example.helion.core.model.UniverseMessage
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionNullSecPurple
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionSurfaceVariant
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary

@Composable
fun CommsScreen(
    viewModel: CommsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    if (state.selectedConversation != null) {
        ConversationThreadView(
            conv = state.selectedConversation!!,
            messages = state.messages,
            inputText = state.messageInput,
            onInputChange = { viewModel.setInputText(it) },
            onSend = { viewModel.sendMessage() },
            onBack = { viewModel.closeConversation() },
            isSending = state.isSending,
            modifier = modifier
        )
    } else {
        ConversationListContent(
            conversations = state.conversations,
            onSelect = { viewModel.selectConversation(it) },
            modifier = modifier
        )
    }
}

@Composable
fun ConversationListContent(
    conversations: List<CommsConversation>,
    onSelect: (CommsConversation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("comms_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "COMMUNICATIONS & CHANNELS",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary,
                letterSpacing = 1.sp
            )
        }

        items(conversations) { conv ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(conv) },
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(HelionSurfaceHigh, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (conv.type) {
                                ConversationType.DIRECT -> Icons.Default.Person
                                ConversationType.GUILD -> Icons.Default.Shield
                                ConversationType.ALLIANCE -> Icons.Default.Group
                                ConversationType.SYSTEM -> Icons.Default.Radio
                                else -> Icons.Default.Radio
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (conv.type == ConversationType.GUILD) HelionNullSecPurple else HelionCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = conv.title, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = conv.lastMessagePreview, style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary, maxLines = 1)
                        }
                    }

                    if (conv.unreadCount > 0) {
                        Surface(shape = CircleShape, color = HelionCyan) {
                            Text(
                                text = "${conv.unreadCount}",
                                color = HelionDeepGraphite,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationThreadView(
    conv: CommsConversation,
    messages: List<UniverseMessage>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Conversation Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HelionDeepGraphite,
            border = BorderStroke(1.dp, HelionBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HelionCyan)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = conv.title, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "${conv.type.label} • ${conv.participantsSummary}", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == "cmd-bannaar"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMe) HelionSurfaceHigh else HelionSurface,
                        border = BorderStroke(1.dp, if (isMe) HelionCyan.copy(alpha = 0.5f) else HelionBorder),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isMe) "YOU [${msg.senderCallSign}]" else "${msg.senderDisplayName} [${msg.senderCallSign}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMe) HelionCyan else HelionAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SECURE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = msg.body, style = MaterialTheme.typography.bodyMedium, color = HelionTextPrimary)
                        }
                    }
                }
            }
        }

        // Compose Message Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HelionSurface
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("Transmit message over HELION frequency...") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HelionCyan,
                        unfocusedBorderColor = HelionBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank() && !isSending
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) HelionCyan else HelionTextMuted
                    )
                }
            }
        }
    }
}

package com.example.helion.ui.guild

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.GuildInfo
import com.example.helion.ui.components.HelionCard
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionNullSecPurple
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionSurfaceVariant
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GuildScreen(
    viewModel: GuildViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("OVERVIEW", "MEMBERS", "TERRITORY", "NOTICES")

    var createNoticeDialogOpen by remember { mutableStateOf(false) }
    var noticeTitle by remember { mutableStateOf("") }
    var noticeBody by remember { mutableStateOf("") }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    val guild = state.guildInfo
    if (guild == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No guild membership records found.", color = HelionTextSecondary)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().testTag("guild_screen")) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = HelionDeepGraphite,
            contentColor = HelionNullSecPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = HelionNullSecPurple,
                    height = 2.5.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) HelionNullSecPurple else HelionTextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> GuildOverviewContent(guild = guild)
            1 -> GuildMembersContent(guild = guild)
            2 -> GuildTerritoryContent(guild = guild)
            3 -> GuildNoticesContent(
                guild = guild,
                onOpenCreate = { createNoticeDialogOpen = true }
            )
        }
    }

    if (createNoticeDialogOpen) {
        AlertDialog(
            onDismissRequest = { createNoticeDialogOpen = false },
            title = { Text(text = "Post Guild Directive", color = HelionTextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("Directive Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HelionNullSecPurple, unfocusedBorderColor = HelionBorder)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text("Directive Body / Fleet Orders") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HelionNullSecPurple, unfocusedBorderColor = HelionBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank()) {
                            viewModel.postNotice(noticeTitle, noticeBody)
                            noticeTitle = ""
                            noticeBody = ""
                            createNoticeDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HelionNullSecPurple, contentColor = Color.White)
                ) {
                    Text("Broadcast Directive")
                }
            },
            dismissButton = {
                TextButton(onClick = { createNoticeDialogOpen = false }) {
                    Text("Cancel", color = HelionTextSecondary)
                }
            },
            containerColor = HelionSurface
        )
    }
}

@Composable
fun GuildOverviewContent(guild: GuildInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HelionCard(title = "Guild Command Hub", badgeText = guild.ticker, accentColor = HelionNullSecPurple) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HelionSurfaceHigh)
                            .border(1.5.dp, HelionNullSecPurple, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = HelionNullSecPurple, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = guild.name, style = MaterialTheme.typography.headlineMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "Ticker: [${guild.ticker}] • Members: ${guild.memberCount}", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = guild.description, style = MaterialTheme.typography.bodyMedium, color = HelionTextSecondary)

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "HOME SYSTEM", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = guild.homeSystemName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ALLIANCE MEMBERSHIP", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = guild.allianceName ?: "None", style = MaterialTheme.typography.titleMedium, color = HelionNullSecPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            HelionCard(title = "Assigned Server Permissions", accentColor = HelionCyan) {
                Text(
                    text = "Controls and administrative tools are gated strictly according to permissions returned by the authoritative HELION server:",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    guild.grantedPermissions.forEach { perm ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = HelionHighSecGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = perm, style = MaterialTheme.typography.labelMedium, color = HelionCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuildMembersContent(guild: GuildInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(guild.members) { mem ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (mem.isOnline) HelionHighSecGreen else HelionTextMuted)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = mem.displayName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "${mem.roleTitle} • Hull: ${mem.currentShipHull}", style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                        }
                    }

                    Text(text = mem.locationSystem, style = MaterialTheme.typography.labelMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GuildTerritoryContent(guild: GuildInfo) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ZERO SPACE SOVEREIGN SYSTEMS (${guild.territories.size})",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary
            )
        }

        items(guild.territories) { terr ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionNullSecPurple)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = terr.systemName, style = MaterialTheme.typography.titleLarge, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = terr.sovereigntyStatus, style = MaterialTheme.typography.labelSmall, color = HelionNullSecPurple)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = HelionNullSecPurple.copy(alpha = 0.2f)) {
                            Text(text = "0.0 ZERO SPACE", color = HelionNullSecPurple, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Daily Citadel Tariff Revenue: ${numberFormat.format(terr.dailyRevenueCr)} GSC",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionAmber,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GuildNoticesContent(
    guild: GuildInfo,
    onOpenCreate: () -> Unit
) {
    val canPost = guild.hasPermission("guild.notice.create")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canPost) {
            item {
                Button(
                    onClick = onOpenCreate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HelionNullSecPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post Guild Directive", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(guild.notices) { not ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, if (not.isPinned) HelionAmber else HelionBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = not.title, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        if (not.isPinned) {
                            Text(text = "PINNED", style = MaterialTheme.typography.labelSmall, color = HelionAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = not.body, style = MaterialTheme.typography.bodyMedium, color = HelionTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "By ${not.authorName} (${not.authorRole})", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                }
            }
        }
    }
}

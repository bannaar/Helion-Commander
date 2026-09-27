package com.example.helion.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.ui.components.EnvironmentBadge
import com.example.helion.ui.components.HelionCard
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionSurfaceVariant
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.helion.core.model.ServerEnvironment

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // Safeguard confirmation dialog for switching to PRODUCTION
    if (state.pendingProductionSwitch) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelProductionSwitch() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = HelionAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SWITCH TO PRODUCTION", fontWeight = FontWeight.Bold, color = HelionTextPrimary)
                }
            },
            text = {
                Text(
                    text = "You are selecting the persistent HELION Production target.\n\nTest assets, GSC, ships, progression, and developer state do not transfer.\n\nThis build is not connected to Production yet. Once a verified Production endpoint is configured, Production actions may affect persistent live state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HelionTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmProductionSwitch() },
                    colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite)
                ) {
                    Text("SELECT PRODUCTION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelProductionSwitch() },
                    border = BorderStroke(1.dp, HelionBorder)
                ) {
                    Text("CANCEL", color = HelionTextPrimary)
                }
            },
            containerColor = HelionSurfaceVariant,
            shape = RoundedCornerShape(8.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "SYSTEM & ENVIRONMENT CONFIGURATION",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary,
                letterSpacing = 1.sp
            )
        }

        // 1. ENVIRONMENT SELECTOR
        item {
            HelionCard(
                title = "Universe Server Environment",
                badgeText = state.currentServerEnvironment.badgeLabel,
                accentColor = when (state.currentServerEnvironment) {
                    ServerEnvironment.DEMO -> HelionCyan
                    ServerEnvironment.PRIVATE_TEST -> HelionAmber
                    ServerEnvironment.PRODUCTION -> HelionHighSecGreen
                }
            ) {
                Text(
                    text = "Select the target universe profile. The active environment governs API routing, local cache isolation, and credential/session namespace.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServerEnvironment.values().forEach { env ->
                        val isSelected = state.currentServerEnvironment == env
                        val configState = when (env) {
                            ServerEnvironment.DEMO -> "CONFIGURED (LOCAL SIM)"
                            ServerEnvironment.PRIVATE_TEST -> "NOT CONNECTED"
                            ServerEnvironment.PRODUCTION -> "NOT CONNECTED"
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.requestSwitchEnvironment(env) }
                                .testTag("env_selector_${env.id}"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) HelionSurfaceVariant else HelionSurfaceHigh,
                            border = BorderStroke(1.dp, if (isSelected) HelionCyan else HelionBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = env.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = HelionTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = HelionCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "STATUS: $configState",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (env == ServerEnvironment.DEMO) HelionHighSecGreen else HelionTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = env.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HelionTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. SERVER STATUS PROBE CARD
        item {
            HelionCard(
                title = "Server Connection & Status Probe",
                badgeText = when {
                    state.currentServerEnvironment == ServerEnvironment.DEMO && state.serverStatus != null -> "LOCAL SIM"
                    state.serverStatus != null -> "ONLINE"
                    else -> "NOT CONNECTED"
                },
                badgeColor = when {
                    state.currentServerEnvironment == ServerEnvironment.DEMO && state.serverStatus != null -> HelionCyan
                    state.serverStatus != null -> HelionHighSecGreen
                    else -> HelionTextMuted
                },
                accentColor = HelionCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target: ${state.currentServerEnvironment.displayName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { viewModel.refreshServerStatus() },
                            border = BorderStroke(1.dp, HelionBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Probe", modifier = Modifier.size(14.dp), tint = HelionCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PROBE", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                        }
                    }

                    if (state.serverStatus != null) {
                        val status = state.serverStatus!!
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionSurfaceVariant,
                            border = BorderStroke(0.8.dp, HelionHighSecGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("SERVICE: ${status.serviceName}", style = MaterialTheme.typography.labelSmall, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                                Text("VERSION: ${status.serverVersion} • PROTOCOL: ${status.protocolVersion}", style = MaterialTheme.typography.bodySmall, color = HelionTextPrimary)
                                Text("MAINTENANCE: ${if (status.maintenance) "YES (SUSPENDED)" else "NO (ACTIVE)"}", style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                                if (status.message != null) {
                                    Text(status.message, style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                }
                            }
                        }
                    } else if (state.serverStatusError != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionSurfaceVariant,
                            border = BorderStroke(0.8.dp, HelionBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = state.serverStatusError ?: "Server not configured.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HelionAmber
                                )
                                Text(
                                    text = "Real ${state.currentServerEnvironment.displayName} companion API endpoint will be configured when the authoritative HELION server adapter is connected.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. OFFLINE SIMULATION TOGGLE
        item {
            HelionCard(
                title = "Client Connectivity & Offline Mode",
                accentColor = if (state.isOfflineSimulated) HelionDangerRed else HelionHighSecGreen
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Simulate Offline Mode", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "When offline, you can browse cached ships, routes, and UniNet news. Authoritative actions are disabled to prevent stale commits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary
                        )
                    }
                    Switch(
                        checked = state.isOfflineSimulated,
                        onCheckedChange = { viewModel.setOfflineSimulated(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = HelionDangerRed, checkedTrackColor = HelionDangerRed.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // 3. NOTIFICATION SETTINGS
        item {
            HelionCard(title = "Sub-Space Notification Frequencies", accentColor = HelionCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NotificationToggleRow(
                        title = "Direct Comms Messages",
                        subtitle = "Alert when another pilot sends a direct comms message",
                        checked = state.notifyDMs,
                        onChecked = { viewModel.toggleDMs(it) }
                    )
                    NotificationToggleRow(
                        title = "Guild Directives & Alerts",
                        subtitle = "Alert on pinned guild orders and territory changes",
                        checked = state.notifyGuild,
                        onChecked = { viewModel.toggleGuild(it) }
                    )
                    NotificationToggleRow(
                        title = "Market Watch Opportunities",
                        subtitle = "Notify when watched commodities reach target arbitrage margins",
                        checked = state.notifyMarketWatch,
                        onChecked = { viewModel.toggleMarket(it) }
                    )
                    NotificationToggleRow(
                        title = "UniNet Flash Bulletins",
                        subtitle = "Critical frontier conflicts and system security alerts",
                        checked = state.notifyGalNetFlash,
                        onChecked = { viewModel.toggleGalNet(it) }
                    )
                }
            }
        }

        // 4. CACHE & PERSISTENCE
        item {
            HelionCard(title = "Local Cache Management", accentColor = HelionBorder) {
                Text(
                    text = "Room SQLite Database stores cached commander telemetry, star systems, and local loadout plans.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { viewModel.clearLocalCache() },
                    border = BorderStroke(1.dp, HelionCyan)
                ) {
                    Text("Purge Local Telemetry Cache", color = HelionCyan)
                }
                if (state.cacheClearedMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = state.cacheClearedMessage ?: "", style = MaterialTheme.typography.labelSmall, color = HelionHighSecGreen)
                }
            }
        }

        // 5. DIAGNOSTICS & ABOUT
        item {
            HelionCard(title = "System Architecture & Authority", accentColor = HelionCyan) {
                Text(
                    text = "CORE ARCHITECTURAL CONTRACT:",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• The Android client may REQUEST actions.\n• The HELION Universe Server VALIDATES and COMMITS them.\n• Client never declares authoritative state (GSC, ships, stock, standings).\n• Route plans and fitting plans are local hypothetical workspaces.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = HelionBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "HELION Commander Companion v1.0.0 (Build 36)",
                    style = MaterialTheme.typography.titleMedium,
                    color = HelionTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prototype companion client. Current builds use mock data until a verified HELION server adapter is connected.",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextMuted
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = HelionTextPrimary, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = HelionCyan, checkedTrackColor = HelionCyan.copy(alpha = 0.5f))
        )
    }
}

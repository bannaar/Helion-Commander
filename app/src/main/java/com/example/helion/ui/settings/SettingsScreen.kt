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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

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
                badgeText = state.currentEnvironment.name,
                accentColor = HelionAmber
            ) {
                Text(
                    text = "Select authoritative cluster to connect. Note: Private test environments use artificial 100-credit baselines which NEVER apply to live Production.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HelionEnvironment.values().forEach { env ->
                        val isSelected = state.currentEnvironment == env
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.switchEnvironment(env) },
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
                                        Text(text = env.displayName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (isSelected) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text(text = env.serverEndpoint, style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                                    Text(text = env.description, style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                                }
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
                            text = "When offline, you can browse cached ships, routes, and GalNet news. Authoritative actions (Market BUY/SELL, Refitting) are disabled to prevent stale commits.",
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
                        subtitle = "Alert when another pilot sends direct encrypted comms",
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
                        title = "GalNet Flash Wire Bulletins",
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
                    text = "• The Android client may REQUEST actions.\n• The HELION Universe Server VALIDATES and COMMITS them.\n• Client never declares authoritative state (credits, ships, stock, standings).\n• Route plans and fitting plans are local hypothetical workspaces.",
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
                    text = "Official companion for the persistent server-authoritative universe.",
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

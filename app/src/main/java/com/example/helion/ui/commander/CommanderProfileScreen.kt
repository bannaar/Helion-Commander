package com.example.helion.ui.commander

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.ui.components.EnvironmentBadge
import com.example.helion.ui.components.HelionCard
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionLowSecOrange
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
fun CommanderProfileScreen(
    viewModel: CommanderViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    val cmd = state.profile
    if (cmd == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Unable to load commander profile.", color = HelionTextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("commander_profile_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // HEADER HERO CARD
        item {
            HelionCard(
                accentColor = HelionCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(HelionSurfaceHigh)
                            .border(2.dp, HelionCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Commander Crest",
                            tint = HelionCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = cmd.displayName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CALLSIGN: [${cmd.callSign}] • ID: ${cmd.commanderId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cmd.rank,
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "CAREER TRACK", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = cmd.career, style = MaterialTheme.typography.bodyMedium, color = HelionTextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXPERIENCE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = "${numberFormat.format(cmd.xp)} XP", style = MaterialTheme.typography.bodyMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // FINANCIAL ASSETS & LOCATION
        item {
            HelionCard(title = "Financials & Deployment", accentColor = HelionAmber) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "CREDIT BALANCE (CR)", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        Text(
                            text = "${numberFormat.format(cmd.credits)} CR",
                            style = MaterialTheme.typography.headlineMedium,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ACCOUNT ENV", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        EnvironmentBadge(environment = cmd.environment)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "CURRENT SYSTEM", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = cmd.currentSystemName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "STATION DOCKED", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = cmd.currentStationName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary)
                    }
                }
            }
        }

        // FACTION STANDINGS
        item {
            HelionCard(
                title = "Galactic Faction Standings",
                badgeText = "${cmd.factionStandings.size} Factions",
                accentColor = HelionHighSecGreen
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    cmd.factionStandings.forEach { fac ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HelionSurfaceHigh, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = fac.factionName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Rank: ${fac.rankTitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionTextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (fac.standing >= 0) HelionHighSecGreen.copy(alpha = 0.15f) else HelionDangerRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (fac.standing > 0) "+${fac.standing}" else "${fac.standing}",
                                        color = if (fac.standing >= 0) HelionHighSecGreen else HelionDangerRed,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Normalize -10 to +10 range into 0 to 1
                            val normalized = ((fac.standing + 10f) / 20f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { normalized },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (fac.standing >= 0) HelionHighSecGreen else HelionDangerRed,
                                trackColor = HelionSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // LICENSES & PERMITS
        item {
            HelionCard(title = "Endorsements & Permits", accentColor = HelionCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cmd.licenses.forEach { lic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HelionSurfaceHigh, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = HelionHighSecGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = lic.name, style = MaterialTheme.typography.bodyMedium, color = HelionTextPrimary, fontWeight = FontWeight.Medium)
                                    Text(text = "Issued by: ${lic.issuingAuthority}", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                }
                            }
                            Text(text = "VALID", style = MaterialTheme.typography.labelSmall, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // AFFILIATIONS
        item {
            HelionCard(title = "Guild & Alliance Affiliation", accentColor = HelionNullSecPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "GUILD", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(
                            text = if (cmd.guildTicker != null) "Iron Vanguard [${cmd.guildTicker}]" else "Independent",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ALLIANCE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(
                            text = "Helios Compact",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionNullSecPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

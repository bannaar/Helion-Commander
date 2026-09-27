package com.example.helion.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.helion.ui.components.EnvironmentBadge
import com.example.helion.ui.components.HelionCard
import com.example.helion.ui.components.SecurityBadge
import com.example.helion.ui.components.StatBar
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHazardOrange
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionLowSecOrange
import com.example.ui.theme.HelionNullSecPurple
import com.example.ui.theme.HelionShieldBlue
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionSurfaceVariant
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    viewModel: HomeViewModel,
    onNavigateToCommander: () -> Unit,
    onNavigateToFleet: () -> Unit,
    onNavigateToUniverse: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToGalNet: () -> Unit,
    onNavigateToGuild: () -> Unit,
    onNavigateToComms: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = HelionCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ESTABLISHING COMPANION TELEMETRY LINK...",
                    style = MaterialTheme.typography.labelMedium,
                    color = HelionCyan
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_dashboard_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // 1. COMMANDER PROFILE CARD
        item {
            val cmd = state.commander
            HelionCard(
                title = "Commander Profile",
                badgeText = cmd?.rank ?: "Cadet",
                badgeColor = HelionCyan,
                accentColor = HelionCyan,
                onClick = onNavigateToCommander,
                modifier = Modifier.testTag("home_commander_card")
            ) {
                if (cmd != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = cmd.displayName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = HelionTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Callsign: [${cmd.callSign}] • ${cmd.career}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HelionTextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BALANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextMuted
                            )
                            Text(
                                text = "${numberFormat.format(cmd.credits)} GSC",
                                style = MaterialTheme.typography.titleLarge,
                                color = HelionAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Faction Standing preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cmd.factionStandings.take(2).forEach { fac ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(4.dp),
                                color = HelionSurfaceHigh
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = fac.factionName.take(16),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionTextSecondary
                                    )
                                    Text(
                                        text = if (fac.standing > 0) "+${fac.standing}" else "${fac.standing}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (fac.standing >= 0) HelionHighSecGreen else HelionDangerRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. ACTIVE SHIP HERO CARD
        item {
            val ship = state.activeShip
            HelionCard(
                title = "Active Fleet Asset",
                badgeText = ship?.hullDefinition?.shipClass ?: "Ship",
                badgeColor = HelionAmber,
                accentColor = HelionAmber,
                onClick = onNavigateToFleet,
                modifier = Modifier.testTag("home_active_ship_card")
            ) {
                if (ship != null) {
                    // Ship hero banner image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, HelionBorder, RoundedCornerShape(6.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.helion_ship_banner),
                            contentDescription = "Active Ship Hangar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, HelionDeepGraphite.copy(alpha = 0.9f))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = ship.shipName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = HelionTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ship.hullDefinition.hullName} • Reg: ${ship.registrationMark}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HelionCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ship Vital Status Bars
                    StatBar(
                        label = "Hull Integrity",
                        currentValue = "${ship.hullConditionPercent}%",
                        percent = ship.hullConditionPercent / 100f,
                        barColor = if (ship.hullConditionPercent > 70f) HelionHighSecGreen else HelionDangerRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(
                        label = "Sub-Space Fuel",
                        currentValue = "${ship.fuelPercent}%",
                        percent = ship.fuelPercent / 100f,
                        barColor = HelionCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(
                        label = "Cargo Space",
                        currentValue = "${ship.currentCargoTons} / ${ship.calculatedCargoCapacity} TONS",
                        percent = (ship.currentCargoTons.toFloat() / ship.calculatedCargoCapacity.toFloat()).coerceIn(0f, 1f),
                        barColor = HelionAmber
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Station: ${ship.currentLocationStationName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary
                        )
                        Text(
                            text = "Tap for Outfitting & Livery ➔",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2B. TACTICAL MISSIONS & FLEET OBJECTIVES CARD
        item {
            val missions = state.activeMissions.filter {
                it.status == com.example.helion.core.model.MissionStatus.ACTIVE ||
                        it.status == com.example.helion.core.model.MissionStatus.COMPLETED
            }
            val priority = missions.find { it.isPriorityTarget } ?: missions.firstOrNull()
            HelionCard(
                title = "Tactical Missions & Fleet Objectives",
                badgeText = "${missions.size} Active",
                badgeColor = HelionAmber,
                accentColor = HelionAmber,
                onClick = onNavigateToMissions,
                modifier = Modifier.testTag("home_missions_card")
            ) {
                if (priority != null) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = priority.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HelionTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sponsor: ${priority.sponsorFaction} • ${priority.category.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionCyan
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = HelionAmber.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, HelionAmber)
                            ) {
                                Text(
                                    text = "${numberFormat.format(priority.creditReward)} GSC",
                                    color = HelionAmber,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location marker row
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Location: ${priority.primaryLocation.systemName} • ${priority.primaryLocation.celestialBodyName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Overall Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FLEET OBJECTIVE PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextMuted,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${(priority.overallProgressPercent * 100).toInt()}% COMPLETE",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { priority.overallProgressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (priority.isFullyCompleted) HelionHighSecGreen else HelionCyan,
                            trackColor = HelionDeepGraphite
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fleet Task: ${priority.assignedShipName} (${priority.assignedFleetStatus.label})",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "View All Tactical Missions ➔",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. CURRENT LOCATION & ROUTE PLANNER CARD
        item {
            val sys = state.currentSystem
            val route = state.activeRoutePlan
            HelionCard(
                title = "Sector & Navigation",
                badgeText = sys?.securityClass?.label ?: "HIGH SEC",
                badgeColor = HelionHighSecGreen,
                accentColor = HelionShieldBlue,
                onClick = onNavigateToUniverse,
                modifier = Modifier.testTag("home_location_card")
            ) {
                if (sys != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sys.name.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = HelionTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sys.regionName} • ${sys.sovereignName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HelionTextSecondary
                            )
                        }
                        SecurityBadge(rating = sys.securityRating)
                    }

                    if (route != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HelionSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HelionBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ACTIVE PLANNED FLIGHTPATH",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${route.totalJumps} JUMPS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = route.origin.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = HelionCyan,
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp)
                                            .size(14.dp)
                                    )
                                    Text(
                                        text = route.destination.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lowest Security on path: +${route.lowestSecurityRating} • Next Gate: Harrow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. REGIONAL MARKET WATCH
        item {
            HelionCard(
                title = "Market Watch (Local Station)",
                badgeText = "Kepler Market",
                badgeColor = HelionAmber,
                accentColor = HelionAmber,
                onClick = onNavigateToMarket,
                modifier = Modifier.testTag("home_market_card")
            ) {
                if (state.marketWatchItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.marketWatchItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HelionSurfaceHigh, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Stock: ${item.stockUnits} ${item.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionTextSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${numberFormat.format(item.buyPrice)} GSC",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = HelionAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (item.priceTrendDelta >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = if (item.priceTrendDelta >= 0) HelionHighSecGreen else HelionDangerRed,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = "${if (item.priceTrendDelta >= 0) "+" else ""}${item.priceTrendDelta} GSC",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (item.priceTrendDelta >= 0) HelionHighSecGreen else HelionDangerRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. UNINET HEADLINES (legacy internal GalNet model)
        item {
            HelionCard(
                title = "UniNet Wire",
                badgeText = "Breaking",
                badgeColor = HelionDangerRed,
                accentColor = HelionDangerRed,
                onClick = onNavigateToGalNet,
                modifier = Modifier.testTag("home_galnet_card")
            ) {
                val article = state.latestArticles.firstOrNull()
                if (article != null) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = HelionDangerRed.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, HelionDangerRed)
                            ) {
                                Text(
                                    text = article.category.label.uppercase(),
                                    color = HelionDangerRed,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2 hours ago",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.headline,
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // 6. GUILD & COMMS SUMMARY
        item {
            val guild = state.guildInfo
            HelionCard(
                title = "Guild & Communications",
                badgeText = guild?.ticker ?: "IVG",
                badgeColor = HelionNullSecPurple,
                accentColor = HelionNullSecPurple,
                onClick = onNavigateToGuild,
                modifier = Modifier.testTag("home_guild_card")
            ) {
                if (guild != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = guild.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = HelionTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${guild.memberCount} Pilots • Alliance: ${guild.allianceName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextSecondary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = HelionHighSecGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HelionHighSecGreen)
                        ) {
                            Text(
                                text = "3 Online",
                                color = HelionHighSecGreen,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    val recentNotice = guild.notices.firstOrNull()
                    if (recentNotice != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionSurfaceHigh
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "NOTICE: ${recentNotice.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = recentNotice.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HelionTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. SERVER STATUS CARD
        item {
            HelionCard(
                title = "Universe Server Authority",
                badgeText = "Online",
                badgeColor = HelionHighSecGreen,
                accentColor = HelionCyan,
                onClick = onNavigateToSettings,
                modifier = Modifier.testTag("home_server_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        EnvironmentBadge(environment = state.environment)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next Scheduled Maintenance: Tuesday 09:00 UTC",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextMuted
                        )
                    }
                    Text(
                        text = "Ping: 28ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionHighSecGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

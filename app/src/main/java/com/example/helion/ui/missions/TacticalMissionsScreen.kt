package com.example.helion.ui.missions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.FleetTaskStatus
import com.example.helion.core.model.LocationMarker
import com.example.helion.core.model.MissionCategory
import com.example.helion.core.model.MissionStatus
import com.example.helion.core.model.ObjectiveStatus
import com.example.helion.core.model.TacticalMission
import com.example.helion.core.model.TacticalObjective
import com.example.helion.core.model.ThreatLevel
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
import com.example.ui.theme.HelionVoidBlack
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TacticalMissionsScreen(
    viewModel: TacticalMissionsViewModel,
    onNavigateToUniverse: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize().background(HelionVoidBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = HelionCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ACQUIRING FLEET TELEMETRY & TACTICAL OBJECTIVES...",
                    style = MaterialTheme.typography.labelMedium,
                    color = HelionCyan
                )
            }
        }
        return
    }

    // Filter missions by tab and category
    val currentTabMissions = remember(state.missions, state.selectedTab) {
        when (state.selectedTab) {
            0 -> state.missions.filter { it.status == MissionStatus.ACTIVE || it.status == MissionStatus.COMPLETED }
            1 -> state.missions.filter { it.status == MissionStatus.ACTIVE }
            2 -> state.missions.filter { it.status == MissionStatus.AVAILABLE }
            3 -> state.missions.filter { it.status == MissionStatus.CLAIMED }
            else -> emptyList()
        }
    }

    val filteredMissions = remember(currentTabMissions, state.selectedCategoryFilter, state.selectedThreatFilter) {
        currentTabMissions.filter { mission ->
            val matchCategory = state.selectedCategoryFilter == null || mission.category == state.selectedCategoryFilter
            val matchThreat = state.selectedThreatFilter == null || mission.threatLevel == state.selectedThreatFilter
            matchCategory && matchThreat
        }
    }

    // Metrics for HUD banner
    val activeCount = remember(state.missions) { state.missions.count { it.status == MissionStatus.ACTIVE || it.status == MissionStatus.COMPLETED } }
    val totalActiveBounty = remember(state.missions) {
        state.missions.filter { it.status == MissionStatus.ACTIVE || it.status == MissionStatus.COMPLETED }.sumOf { it.creditReward }
    }
    val priorityMission = remember(state.missions) { state.missions.find { it.isPriorityTarget } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HelionVoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HEADER TITLE & SIMULATION TOGGLE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FLEET COMMAND  /  TACTICAL DIRECTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TACTICAL MISSIONS & OBJECTIVES",
                        style = MaterialTheme.typography.titleLarge,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live Fleet Telemetry Stream Button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (state.isSimulatingFleetFeed) HelionCyan.copy(alpha = 0.2f) else HelionSurface,
                    border = BorderStroke(1.dp, if (state.isSimulatingFleetFeed) HelionCyan else HelionBorder),
                    modifier = Modifier.clickable { viewModel.toggleSimulatingFleetFeed() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (state.isSimulatingFleetFeed) HelionHighSecGreen else HelionTextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isSimulatingFleetFeed) "LIVE TELEMETRY ON" else "FLEET SIMULATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isSimulatingFleetFeed) HelionCyan else HelionTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Action confirmation banner if triggered
        if (state.actionBannerMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HelionAmber.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, HelionAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.actionBannerMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearBanner() }, modifier = Modifier.size(20.dp)) {
                            Text("✕", color = HelionAmber, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 1B. LOCAL NOTIFICATION ALERT STATUS & TEST TRIGGER
        item {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HelionCyan.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, HelionCyan.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("local_notification_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = HelionCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LOCAL NOTIFICATIONS ARMED",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Device alerts dispatched when mission progress or fleet tasks reach 100%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = HelionTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    val activeOp = state.missions.find { it.status == MissionStatus.ACTIVE }
                    if (activeOp != null) {
                        OutlinedButton(
                            onClick = { viewModel.simulateMission100Percent(activeOp.id) },
                            border = BorderStroke(1.dp, HelionCyan),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("quick_simulate_100_btn")
                        ) {
                            Text("Simulate 100% Alert", color = HelionCyan, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 2. HUD SUMMARY METRICS CARD
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.2.dp, HelionCyan.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth().testTag("tactical_hud_metrics_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE OPERATIONS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                            Text("$activeCount Deployed", style = MaterialTheme.typography.titleMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.height(28.dp).width(1.dp).background(HelionBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("POTENTIAL BOUNTY", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                            Text("${numberFormat.format(totalActiveBounty)} CR", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.height(28.dp).width(1.dp).background(HelionBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PRIORITY TARGET", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                            Text(
                                text = priorityMission?.primaryLocation?.systemName ?: "None",
                                style = MaterialTheme.typography.titleMedium,
                                color = HelionHighSecGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (priorityMission != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = HelionBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PRIORITY: ${priorityMission.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "ETA: ${priorityMission.timeRemainingMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 2B. QUICK DASHBOARD SHORTCUT (TAB 0)
        if (state.selectedTab == 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HelionSurfaceVariant,
                    border = BorderStroke(1.dp, HelionCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTab(4) }
                        .testTag("weekly_summary_shortcut_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(HelionCyan.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, HelionCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = HelionCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TACTICAL MISSIONS SUMMARY DASHBOARD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${state.dashboardMetrics.totalCompletedMissions} total ops completed • ${String.format("%.1f", state.dashboardMetrics.weeklyAverageCompleted)} ops/wk avg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HelionTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "VIEW BAR CHART →",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 3. TACTICAL TABS (Active, Fleet Tasks, Contracts, Archive, Dashboard)
        item {
            val tabs = listOf(
                "Active ($activeCount)",
                "Fleet Tasks (${state.missions.count { it.status == MissionStatus.ACTIVE }})",
                "Contracts (${state.missions.count { it.status == MissionStatus.AVAILABLE }})",
                "Archive (${state.missions.count { it.status == MissionStatus.CLAIMED }})",
                "Dashboard"
            )

            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = HelionDeepGraphite,
                contentColor = HelionCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("tactical_missions_tabs")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (state.selectedTab == index) HelionCyan else HelionTextMuted
                            )
                        }
                    )
                }
            }
        }

        // 4. CATEGORY & THEMATIC FILTER CHIPS (Only on mission list tabs)
        if (state.selectedTab != 4) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedCategoryFilter == null,
                                onClick = { viewModel.setCategoryFilter(null) },
                                label = { Text("All Operations", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HelionCyan,
                                    selectedLabelColor = HelionDeepGraphite,
                                    containerColor = HelionSurface,
                                    labelColor = HelionTextSecondary
                                )
                            )
                        }

                        items(MissionCategory.entries.toTypedArray()) { cat ->
                            FilterChip(
                                selected = state.selectedCategoryFilter == cat,
                                onClick = {
                                    viewModel.setCategoryFilter(if (state.selectedCategoryFilter == cat) null else cat)
                                },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(cat.primaryColorHex),
                                    selectedLabelColor = HelionVoidBlack,
                                    containerColor = HelionSurface,
                                    labelColor = HelionTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. MISSIONS CONTENT BY TAB
        if (state.selectedTab == 4) {
            // SUMMARY DASHBOARD VIEW WITH WEEKLY MISSIONS BAR CHART
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardKpiCard(
                        title = "TOTAL COMPLETED",
                        value = "${state.dashboardMetrics.totalCompletedMissions}",
                        unit = "Missions",
                        accentColor = HelionCyan,
                        icon = Icons.Default.AssignmentTurnedIn,
                        trendLabel = "+18% vs prev",
                        modifier = Modifier.weight(1f).testTag("kpi_total_completed")
                    )
                    DashboardKpiCard(
                        title = "WEEKLY VELOCITY",
                        value = String.format("%.1f", state.dashboardMetrics.weeklyAverageCompleted),
                        unit = "Ops / Week",
                        accentColor = HelionAmber,
                        icon = Icons.Default.Speed,
                        trendLabel = "Quota: ${state.dashboardMetrics.quotaTargetPerWeek}/wk",
                        modifier = Modifier.weight(1f).testTag("kpi_weekly_velocity")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardKpiCard(
                        title = "TOTAL BOUNTIES",
                        value = "${state.dashboardMetrics.totalCreditsEarned / 1000}k",
                        unit = "Credits (CR)",
                        accentColor = HelionHighSecGreen,
                        icon = Icons.Default.MonetizationOn,
                        trendLabel = "Authoritative",
                        modifier = Modifier.weight(1f).testTag("kpi_total_bounties")
                    )
                    DashboardKpiCard(
                        title = "SUCCESS RATE",
                        value = "${state.dashboardMetrics.averageSuccessRatePercent}%",
                        unit = "Objective Met",
                        accentColor = HelionShieldBlue,
                        icon = Icons.Default.CheckCircle,
                        trendLabel = "High-Sec Cert",
                        modifier = Modifier.weight(1f).testTag("kpi_success_rate")
                    )
                }
            }

            // Interactive Weekly Missions Bar Chart Card
            item {
                WeeklyMissionsBarChartCard(
                    records = state.dashboardMetrics.weeklyRecords,
                    quotaTarget = state.dashboardMetrics.quotaTargetPerWeek,
                    selectedIndex = state.selectedWeekIndex,
                    chartMode = state.chartMode,
                    onSelectIndex = { viewModel.selectWeek(it) },
                    onSetMode = { viewModel.setChartMode(it) }
                )
            }

            // Selected Week Detail Card
            val selectedRecord = state.dashboardMetrics.weeklyRecords.getOrNull(state.selectedWeekIndex)
                ?: state.dashboardMetrics.weeklyRecords.lastOrNull()
            if (selectedRecord != null) {
                item {
                    SelectedWeekDetailCard(
                        record = selectedRecord,
                        numberFormat = numberFormat
                    )
                }
            }

            // Category Performance Distribution
            item {
                CategoryPerformanceBreakdownCard(records = state.dashboardMetrics.weeklyRecords)
            }

            // Top Performing Fleet Sortie Craft
            item {
                FleetSortiesRosterCard(records = state.dashboardMetrics.weeklyRecords)
            }
        } else if (state.selectedTab == 1) {
            // FLEET TASK ASSIGNMENTS VIEW
            items(filteredMissions) { mission ->
                FleetTaskAssignmentCard(
                    mission = mission,
                    onUpdateTaskStatus = { status, delta ->
                        viewModel.updateFleetTaskStatus(mission.id, status, delta)
                    },
                    onNavigateToLocation = {
                        onNavigateToUniverse(mission.primaryLocation.systemId)
                    }
                )
            }
        } else {
            // ACTIVE OPERATIONS / CONTRACTS / ARCHIVE VIEW
            items(filteredMissions) { mission ->
                TacticalMissionCard(
                    mission = mission,
                    onAdvanceObjective = { objId ->
                        viewModel.advanceObjective(mission.id, objId, 1)
                    },
                    onClaimReward = {
                        viewModel.claimReward(mission.id)
                    },
                    onAcceptContract = {
                        viewModel.acceptContract(mission.id)
                    },
                    onTogglePriority = {
                        viewModel.togglePriority(mission.id)
                    },
                    onNavigateToLocation = { loc ->
                        onNavigateToUniverse(loc.systemId)
                    },
                    onUpdateFleetTask = { status ->
                        viewModel.updateFleetTaskStatus(mission.id, status, 15f)
                    }
                )
            }
        }

        // Empty State
        if (state.selectedTab != 4 && filteredMissions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HelionSurface,
                    border = BorderStroke(1.dp, HelionBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = HelionTextMuted, modifier = Modifier.size(36.dp))
                        Text("No Missions in this Category", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary)
                        Text("Check available contracts or reset active filters.", style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                        OutlinedButton(
                            onClick = {
                                viewModel.setCategoryFilter(null)
                                viewModel.setThreatFilter(null)
                            },
                            border = BorderStroke(1.dp, HelionCyan)
                        ) {
                            Text("Reset Category Filters", color = HelionCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalMissionCard(
    mission: TacticalMission,
    onAdvanceObjective: (String) -> Unit,
    onClaimReward: () -> Unit,
    onAcceptContract: () -> Unit,
    onTogglePriority: () -> Unit,
    onNavigateToLocation: (LocationMarker) -> Unit,
    onUpdateFleetTask: (FleetTaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val categoryColor = Color(mission.category.primaryColorHex)

    val threatColor = when (mission.threatLevel) {
        ThreatLevel.MINIMAL -> HelionHighSecGreen
        ThreatLevel.LOW -> HelionCyan
        ThreatLevel.MODERATE -> HelionAmber
        ThreatLevel.HIGH -> HelionLowSecOrange
        ThreatLevel.EXTREME -> HelionNullSecPurple
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactical_mission_card_${mission.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(
            if (mission.isPriorityTarget) 1.5.dp else 1.dp,
            if (mission.isPriorityTarget) HelionAmber else HelionBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // HEADER: Category Badge + Threat Level + Star Bookmark + ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        color = categoryColor.copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, categoryColor)
                    ) {
                        Text(
                            text = mission.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = threatColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, threatColor)
                    ) {
                        Text(
                            text = "${mission.threatLevel.label} Threat",
                            style = MaterialTheme.typography.labelSmall,
                            color = threatColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mission.timeRemainingMinutes > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = HelionTextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${mission.timeRemainingMinutes}m", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onTogglePriority,
                        modifier = Modifier.size(24.dp).testTag("priority_button_${mission.id}")
                    ) {
                        Icon(
                            imageVector = if (mission.isPriorityTarget) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Toggle Priority Target",
                            tint = if (mission.isPriorityTarget) HelionAmber else HelionTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TITLE & SPONSOR
            Text(
                text = mission.title,
                style = MaterialTheme.typography.titleMedium,
                color = HelionTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sponsor: ${mission.sponsorFaction}",
                style = MaterialTheme.typography.labelSmall,
                color = HelionCyan,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // BRIEFING TEXT
            Text(
                text = mission.briefing,
                style = MaterialTheme.typography.bodySmall,
                color = HelionTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // LOCATION MARKER HUD
            LocationMarkerHUD(
                location = mission.primaryLocation,
                onNavigate = { onNavigateToLocation(mission.primaryLocation) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // OVERALL MISSION PROGRESS BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OVERALL OPERATION COMPLETION",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextMuted,
                    fontSize = 9.sp,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "${(mission.overallProgressPercent * 100).toInt()}% COMPLETE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mission.isFullyCompleted) HelionHighSecGreen else HelionCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { mission.overallProgressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (mission.isFullyCompleted) HelionHighSecGreen else HelionCyan,
                trackColor = HelionDeepGraphite
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ASSIGNED FLEET TASK BAR
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HelionDeepGraphite,
                border = BorderStroke(0.8.dp, HelionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Rocket, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mission.assignedShipName,
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color(mission.assignedFleetStatus.badgeColorHex).copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, Color(mission.assignedFleetStatus.badgeColorHex))
                        ) {
                            Text(
                                text = mission.assignedFleetStatus.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(mission.assignedFleetStatus.badgeColorHex),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Fleet Task: ${mission.assignedFleetTaskLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextSecondary,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Fleet task progress bar
                    LinearProgressIndicator(
                        progress = { mission.fleetProgressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = HelionAmber,
                        trackColor = HelionSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TACTICAL OBJECTIVES LIST
            Text(
                text = "TACTICAL OBJECTIVES (${mission.objectives.count { it.isCompleted }} / ${mission.objectives.size} FULFILLED)",
                style = MaterialTheme.typography.labelSmall,
                color = HelionCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                mission.objectives.forEach { obj ->
                    ObjectiveProgressRow(
                        objective = obj,
                        onAdvance = { onAdvanceObjective(obj.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HelionBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // FOOTER: REWARDS & ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${numberFormat.format(mission.creditReward)} CR",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${mission.standingReward} Standing",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionHighSecGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (mission.bonusRewardItem != null) {
                        Text(
                            text = "Bonus: ${mission.bonusRewardItem}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionCyan,
                            fontSize = 9.sp
                        )
                    }
                }

                // Action buttons depending on status
                when (mission.status) {
                    MissionStatus.AVAILABLE -> {
                        Button(
                            onClick = onAcceptContract,
                            colors = ButtonDefaults.buttonColors(containerColor = HelionCyan, contentColor = HelionDeepGraphite),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("accept_contract_${mission.id}")
                        ) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept Contract", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    MissionStatus.COMPLETED -> {
                        Button(
                            onClick = onClaimReward,
                            colors = ButtonDefaults.buttonColors(containerColor = HelionHighSecGreen, contentColor = HelionDeepGraphite),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("claim_bounty_${mission.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claim Bounty", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    MissionStatus.CLAIMED -> {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionHighSecGreen.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, HelionHighSecGreen)
                        ) {
                            Text(
                                text = "ARCHIVED / PAID",
                                color = HelionHighSecGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    MissionStatus.ACTIVE -> {
                        OutlinedButton(
                            onClick = { onUpdateFleetTask(FleetTaskStatus.ENGAGING) },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HelionAmber),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("dispatch_fleet_${mission.id}")
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fleet Dispatch", color = HelionAmber, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationMarkerHUD(
    location: LocationMarker,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = HelionDeepGraphite,
        border = BorderStroke(0.8.dp, HelionCyan.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Location Marker",
                        tint = HelionCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${location.systemName.uppercase()}  //  ${location.celestialBodyName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Beacon: ${location.beaconCode} • Coordinates: ${location.coordinates}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextMuted,
                    fontSize = 9.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = HelionSurfaceVariant
                    ) {
                        Text(
                            text = if (location.jumpCount == 0) "LOCAL SYSTEM • 0 JUMPS" else "${location.jumpCount} JUMPS • ${location.distanceLy} LY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (location.jumpCount == 0) HelionHighSecGreen else HelionAmber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Sec Rating: +${location.securityRating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            OutlinedButton(
                onClick = onNavigate,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, HelionCyan),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("locate_marker_${location.systemId}")
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Locate", color = HelionCyan, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ObjectiveProgressRow(
    objective: TacticalObjective,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = HelionDeepGraphite
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (objective.isCompleted) Icons.Default.CheckCircle else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (objective.isCompleted) HelionHighSecGreen else HelionAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = objective.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (objective.isCompleted) HelionTextSecondary else HelionTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = objective.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${objective.currentProgress} / ${objective.targetProgress} ${objective.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (objective.isCompleted) HelionHighSecGreen else HelionAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )

                    if (!objective.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onAdvance,
                            modifier = Modifier.size(24.dp).testTag("advance_obj_${objective.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Log Telemetry Step",
                                tint = HelionCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Objective progress micro-bar
            LinearProgressIndicator(
                progress = { objective.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (objective.isCompleted) HelionHighSecGreen else HelionAmber,
                trackColor = HelionSurfaceVariant
            )

            if (objective.assignedFleetUnit != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Unit: ${objective.assignedFleetUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextMuted,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun FleetTaskAssignmentCard(
    mission: TacticalMission,
    onUpdateTaskStatus: (FleetTaskStatus, Float) -> Unit,
    onNavigateToLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fleet_task_card_${mission.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Rocket, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = mission.assignedShipName,
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Operation: ${mission.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(mission.assignedFleetStatus.badgeColorHex).copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, Color(mission.assignedFleetStatus.badgeColorHex))
                ) {
                    Text(
                        text = mission.assignedFleetStatus.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(mission.assignedFleetStatus.badgeColorHex),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = HelionDeepGraphite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TELEMETRY STATUS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                        Text("${mission.fleetProgressPercent.toInt()}% COMPLETE", style = MaterialTheme.typography.labelSmall, color = HelionAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mission.assignedFleetTaskLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextPrimary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { mission.fleetProgressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = HelionAmber,
                        trackColor = HelionSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // REASSIGNMENT / DISPATCH ACTIONS
            Text("FLEET ORDERS & DISPATCH OVERRIDE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateTaskStatus(FleetTaskStatus.ENGAGING, 15f) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionDangerRed),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Combat", color = HelionDangerRed, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = { onUpdateTaskStatus(FleetTaskStatus.HARVESTING, 15f) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionAmber),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Mine", color = HelionAmber, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = { onUpdateTaskStatus(FleetTaskStatus.SURVEYING, 15f) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionCyan),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Scan", color = HelionCyan, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToLocation,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionBorder),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Waypoint", color = HelionTextSecondary, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }
    }
}

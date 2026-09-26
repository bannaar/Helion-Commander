package com.example.helion.ui.fleet

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.helion.core.model.LiveryOption
import com.example.helion.core.model.ModuleItem
import com.example.helion.core.model.ModuleSlot
import com.example.helion.core.model.ModuleSlotCategory
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.ui.components.HelionCard
import com.example.helion.ui.components.StatBar
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionBorderGlow
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionCyanGlow
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
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
fun FleetScreen(
    viewModel: FleetViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("FLEET LIST", "LOADOUT PLANNER", "LIVERY")

    var showSavePlanDialog by remember { mutableStateOf(false) }
    var newPlanName by remember { mutableStateOf("") }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().testTag("fleet_screen")) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = HelionDeepGraphite,
            contentColor = HelionCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = HelionCyan,
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
                            color = if (selectedTab == index) HelionCyan else HelionTextSecondary
                        )
                    }
                )
            }
        }

        // Action Status Banner (Success / Failure from Server)
        if (state.actionSuccessMessage != null) {
            Surface(
                color = HelionHighSecGreen.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, HelionHighSecGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = HelionHighSecGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = state.actionSuccessMessage ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
        }

        if (state.errorMessage != null) {
            Surface(
                color = HelionDangerRed.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, HelionDangerRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = HelionDangerRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = state.errorMessage ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
        }

        when (selectedTab) {
            0 -> FleetListContent(
                state = state,
                onSelectShip = { viewModel.selectShip(it) },
                onActivateShip = { viewModel.setActiveShipOnServer(it.instanceId) },
                onFilterChanged = { viewModel.setFilter(it) },
                onSortChanged = { viewModel.setSort(it) },
                onToggleExpand = { viewModel.toggleExpandedShip(it) },
                onNavigateToOutfitting = { ship ->
                    viewModel.selectShip(ship)
                    selectedTab = 1
                },
                onNavigateToLivery = { ship ->
                    viewModel.selectShip(ship)
                    selectedTab = 2
                },
                onPerformMaintenance = { viewModel.performMaintenance(it.instanceId) },
                onSimulateWear = { viewModel.simulateWearAndDamage(it.instanceId) },
                onTestMaintenanceAlert = { viewModel.sendTestMaintenanceNotification(it) }
            )
            1 -> LoadoutPlannerContent(
                state = state,
                onSlotModuleSelected = { slotId, modId -> viewModel.setPlannedModuleForSlot(slotId, modId) },
                onOpenSavePlan = { showSavePlanDialog = true },
                onLoadPlan = { viewModel.loadSavedPlan(it) },
                onDeletePlan = { viewModel.deletePlan(it.planId) },
                onApplyLoadoutToServer = { viewModel.applyPlannedLoadoutToServer() }
            )
            2 -> LiveryCustomizerContent(
                state = state,
                onSelectLivery = { viewModel.selectLivery(it) },
                onWearChanged = { viewModel.setWearPreview(it) },
                onApplyLivery = { viewModel.applyLiveryToServer() }
            )
        }
    }

    // Dialog for saving named loadout plan
    if (showSavePlanDialog) {
        AlertDialog(
            onDismissRequest = { showSavePlanDialog = false },
            title = { Text(text = "Save Loadout Plan", color = HelionTextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Create a local saved fitting plan for ${state.selectedShip?.hullDefinition?.hullName}. Plans can be compared and fitted later when docked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPlanName,
                        onValueChange = { newPlanName = it },
                        label = { Text("Plan Name") },
                        placeholder = { Text("e.g. Combat Alpha 3A") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HelionCyan,
                            unfocusedBorderColor = HelionBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlanName.isNotBlank()) {
                            viewModel.saveCurrentPlan(newPlanName)
                            newPlanName = ""
                            showSavePlanDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HelionCyan, contentColor = HelionDeepGraphite)
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePlanDialog = false }) {
                    Text("Cancel", color = HelionTextSecondary)
                }
            },
            containerColor = HelionSurface
        )
    }
}

// =============================================================================
// 1. FLEET LIST TAB (DETAILED SHIP STATUS & STATS ROSTER)
// =============================================================================
@Composable
fun FleetListContent(
    state: FleetUiState,
    onSelectShip: (OwnedShipInstance) -> Unit,
    onActivateShip: (OwnedShipInstance) -> Unit,
    onFilterChanged: (FleetFilter) -> Unit,
    onSortChanged: (FleetSort) -> Unit,
    onToggleExpand: (String) -> Unit,
    onNavigateToOutfitting: (OwnedShipInstance) -> Unit,
    onNavigateToLivery: (OwnedShipInstance) -> Unit,
    onPerformMaintenance: (OwnedShipInstance) -> Unit = {},
    onSimulateWear: (OwnedShipInstance) -> Unit = {},
    onTestMaintenanceAlert: (OwnedShipInstance) -> Unit = {}
) {
    val activeShip = state.ownedShips.find { it.isActiveShip } ?: state.ownedShips.firstOrNull()
    val totalCargoCapacity = state.ownedShips.sumOf { it.calculatedCargoCapacity }
    val totalMass = state.ownedShips.sumOf { it.totalMassTons.toDouble() }
    val shipsRequiringMaintenance = state.ownedShips.filter { it.requiresMaintenance }

    // Filtering
    val filteredShips = state.ownedShips.filter { ship ->
        when (state.filter) {
            FleetFilter.ALL -> true
            FleetFilter.ACTIVE -> ship.isActiveShip
            FleetFilter.LOCAL -> ship.currentLocationSystemName == "Kepler"
            FleetFilter.STORED -> !ship.isActiveShip
        }
    }

    // Sorting
    val sortedShips = when (state.sort) {
        FleetSort.NAME -> filteredShips.sortedBy { it.shipName }
        FleetSort.CLASS -> filteredShips.sortedBy { it.hullDefinition.shipClass }
        FleetSort.CONDITION -> filteredShips.sortedByDescending { it.hullConditionPercent }
        FleetSort.CARGO -> filteredShips.sortedByDescending { it.calculatedCargoCapacity }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("fleet_list_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. FLEET MAINTENANCE ADVISORY BANNER (If any ship is damaged/worn)
        if (shipsRequiringMaintenance.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HelionDangerRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, HelionDangerRed),
                    modifier = Modifier.fillMaxWidth().testTag("fleet_maintenance_warning_banner")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = HelionDangerRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FLEET ENGINEERING ALERT: ${shipsRequiringMaintenance.size} SHIP(S) REQUIRE MAINTENANCE",
                                style = MaterialTheme.typography.titleSmall,
                                color = HelionDangerRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "One or more fleet craft report hull degradation (< 80%) or excessive component wear (> 35%). Local notification alert has been dispatched to commander terminal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            shipsRequiringMaintenance.forEach { damagedShip ->
                                OutlinedButton(
                                    onClick = { onPerformMaintenance(damagedShip) },
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, HelionHighSecGreen),
                                    modifier = Modifier.testTag("quick_overhaul_${damagedShip.instanceId}")
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = HelionHighSecGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Overhaul ${damagedShip.shipName}", color = HelionHighSecGreen, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. FLEET TELEMETRY & ASSET SUMMARY
        item {
            HelionCard(
                title = "Fleet Command Telemetry",
                badgeText = "${state.ownedShips.size} Hulls Registered",
                badgeColor = HelionCyan,
                accentColor = HelionCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMAND FLAGSHIP: ${activeShip?.shipName?.uppercase() ?: "NONE"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Docked at ${activeShip?.currentLocationStationName ?: "Deep Space"} (${activeShip?.currentLocationSystemName ?: "Unknown"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionCyan
                        )
                    }
                    Surface(
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        color = HelionHighSecGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, HelionHighSecGreen)
                    ) {
                        Text(
                            text = "FLEET READY",
                            color = HelionHighSecGreen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Asset metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        color = HelionSurfaceHigh
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "TOTAL SHIPS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${state.ownedShips.size}", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "1 Active • ${state.ownedShips.size - 1} Stored", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        color = HelionSurfaceHigh
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "TOTAL CARGO", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "$totalCargoCapacity TONS", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                            Text(text = "Combined Fleet Hold", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        color = HelionSurfaceHigh
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "COMBINED MASS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${String.format("%.0f", totalMass)} TONS", style = MaterialTheme.typography.titleMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                            Text(text = "Displacement", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        }
                    }
                }
            }
        }

        // 2. FILTER & SORT STRIP
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Filter chips
                Text(
                    text = "FLEET FILTER:",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FleetFilter.values()) { filterOpt ->
                        val isSelected = state.filter == filterOpt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChanged(filterOpt) },
                            label = { Text(filterOpt.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionCyan,
                                selectedLabelColor = HelionDeepGraphite
                            )
                        )
                    }
                }

                // Sort chips
                Text(
                    text = "SORT BY:",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FleetSort.values()) { sortOpt ->
                        val isSelected = state.sort == sortOpt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSortChanged(sortOpt) },
                            label = { Text(sortOpt.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionAmber,
                                selectedLabelColor = HelionDeepGraphite
                            )
                        )
                    }
                }
            }
        }

        // 3. SHIP ROSTER HEADER
        item {
            Text(
                text = "INDIVIDUAL REGISTERED SHIPS (${sortedShips.size})",
                style = MaterialTheme.typography.labelMedium,
                color = HelionTextSecondary,
                letterSpacing = 1.sp
            )
        }

        // 4. DETAILED SHIP CARDS
        items(sortedShips, key = { it.instanceId }) { ship ->
            FleetShipCard(
                ship = ship,
                isSelected = state.selectedShip?.instanceId == ship.instanceId,
                isExpanded = state.expandedShipId == ship.instanceId,
                isApplyingAction = state.isApplyingAction,
                onSelectShip = { onSelectShip(ship) },
                onToggleExpand = { onToggleExpand(ship.instanceId) },
                onActivateShip = { onActivateShip(ship) },
                onConfigureLoadout = { onNavigateToOutfitting(ship) },
                onCustomizeLivery = { onNavigateToLivery(ship) },
                onPerformMaintenance = { onPerformMaintenance(ship) },
                onSimulateWear = { onSimulateWear(ship) },
                onTestMaintenanceAlert = { onTestMaintenanceAlert(ship) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================================
// 2. LOADOUT PLANNER TAB
// =============================================================================
@Composable
fun LoadoutPlannerContent(
    state: FleetUiState,
    onSlotModuleSelected: (slotId: String, moduleId: String) -> Unit,
    onOpenSavePlan: () -> Unit,
    onLoadPlan: (com.example.helion.core.model.SavedLoadoutPlan) -> Unit,
    onDeletePlan: (com.example.helion.core.model.SavedLoadoutPlan) -> Unit,
    onApplyLoadoutToServer: () -> Unit
) {
    val ship = state.selectedShip ?: return
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    // Calculate hypothetical stats from planned modules
    val plannedInstalledModules = ship.slots.mapNotNull { s ->
        val modId = state.plannedModules[s.slotId]
        state.availableModules.find { it.moduleId == modId }
    }

    val plannedMass = ship.hullDefinition.baseMassTons + plannedInstalledModules.map { it.massTons }.sum()
    val plannedPowerDraw = plannedInstalledModules.map { it.powerDrawMw }.sum()
    val maxPower = ship.hullDefinition.basePowerOutputMw
    val isOverloaded = plannedPowerDraw > maxPower

    // Estimated refit cost compared to what's physically fitted
    var totalCostCR = 0L
    ship.slots.forEach { slot ->
        val plannedModId = state.plannedModules[slot.slotId]
        if (plannedModId != null && plannedModId != slot.installedModule?.moduleId) {
            val mod = state.availableModules.find { it.moduleId == plannedModId }
            if (mod != null) totalCostCR += mod.purchasePrice
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hull & Plan Header
        item {
            HelionCard(
                title = "Hull Outfitting Specification",
                badgeText = ship.hullDefinition.hullName,
                accentColor = HelionCyan
            ) {
                Text(
                    text = "Configuring: ${ship.shipName} [${ship.hullDefinition.role}]",
                    style = MaterialTheme.typography.titleMedium,
                    color = HelionTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manufacturer: ${ship.hullDefinition.manufacturer} • Class: ${ship.hullDefinition.shipClass}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Comparison Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = HelionSurfaceHigh,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "TOTAL MASS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${String.format("%.1f", plannedMass)} T", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "Base: ${ship.hullDefinition.baseMassTons}t", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (isOverloaded) HelionDangerRed.copy(alpha = 0.2f) else HelionSurfaceHigh,
                        border = if (isOverloaded) BorderStroke(1.dp, HelionDangerRed) else null,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "POWER USAGE", style = MaterialTheme.typography.labelSmall, color = if (isOverloaded) HelionDangerRed else HelionTextMuted)
                            Text(text = "${String.format("%.2f", plannedPowerDraw)} MW", style = MaterialTheme.typography.titleMedium, color = if (isOverloaded) HelionDangerRed else HelionCyan, fontWeight = FontWeight.Bold)
                            Text(text = "Max Gen: $maxPower MW", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        }
                    }
                }

                if (isOverloaded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "WARNING: Power grid overloaded! Fit a higher output core or downscale utility modules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionDangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "ESTIMATED REFIT COST", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = "${numberFormat.format(totalCostCR)} CR", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                    }

                    Row {
                        OutlinedButton(
                            onClick = onOpenSavePlan,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HelionCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Plan", color = HelionCyan)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onApplyLoadoutToServer,
                            enabled = !isOverloaded && !state.isApplyingAction,
                            colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            if (state.isApplyingAction) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HelionDeepGraphite, strokeWidth = 2.dp)
                            } else {
                                Text("Request Server Fit", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // SAVED PLANS CAROUSEL
        if (state.savedPlans.isNotEmpty()) {
            item {
                Text(text = "SAVED FITTING PLANS", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.savedPlans) { plan ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HelionSurfaceHigh,
                            border = BorderStroke(1.dp, HelionBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.clickable { onLoadPlan(plan) }) {
                                    Text(text = plan.name, style = MaterialTheme.typography.titleMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                                    Text(text = plan.hullName, style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onDeletePlan(plan) }, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Plan", tint = HelionTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // PHYSICAL SLOTS & MODULE SELECTOR
        item {
            Text(text = "PHYSICAL MODULE SLOTS", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
        }

        items(ship.slots) { slot ->
            val plannedModId = state.plannedModules[slot.slotId]
            val installedMod = state.availableModules.find { it.moduleId == plannedModId }
            val compatibleModules = state.availableModules.filter {
                it.category == slot.category && it.size <= slot.size
            }

            SlotFittingCard(
                slot = slot,
                currentModule = installedMod,
                compatibleModules = compatibleModules,
                onSelectModule = { mod -> onSlotModuleSelected(slot.slotId, mod.moduleId) }
            )
        }
    }
}

@Composable
fun SlotFittingCard(
    slot: ModuleSlot,
    currentModule: ModuleItem?,
    compatibleModules: List<ModuleItem>,
    onSelectModule: (ModuleItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(HelionSurfaceHigh, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${slot.size}",
                            color = HelionCyan,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = slot.name, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "${slot.category.name.replace("_", " ")} • SIZE ${slot.size}", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                    }
                }

                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Close" else "Change Module", color = HelionCyan)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Currently Fitted Module
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HelionSurfaceHigh,
                shape = RoundedCornerShape(4.dp)
            ) {
                if (currentModule != null) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = currentModule.displayName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "${currentModule.manufacturer} • Grade ${currentModule.grade} • ${currentModule.statBoostSummary}", style = MaterialTheme.typography.bodySmall, color = HelionHighSecGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${currentModule.massTons} T", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                            Text(text = "${currentModule.powerDrawMw} MW", style = MaterialTheme.typography.labelSmall, color = HelionAmber)
                        }
                    }
                } else {
                    Text(
                        text = "EMPTY SLOT",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextMuted,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Expanded Compatible Modules List
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "COMPATIBLE MODULES (SIZE ${slot.size} MAX):", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    compatibleModules.forEach { mod ->
                        val isSelected = mod.moduleId == currentModule?.moduleId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectModule(mod)
                                    expanded = false
                                },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) HelionSurfaceVariant else HelionDeepGraphite,
                            border = BorderStroke(1.dp, if (isSelected) HelionCyan else HelionBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = mod.displayName, style = MaterialTheme.typography.bodyMedium, color = HelionTextPrimary, fontWeight = FontWeight.Medium)
                                    Text(text = "Size ${mod.size} • Grade ${mod.grade} • ${mod.powerDrawMw} MW • ${mod.massTons} T", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                                }
                                Text(
                                    text = "${mod.purchasePrice} CR",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HelionAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 3. LIVERY & APPEARANCE TAB
// =============================================================================
@Composable
fun LiveryCustomizerContent(
    state: FleetUiState,
    onSelectLivery: (LiveryOption) -> Unit,
    onWearChanged: (Float) -> Unit,
    onApplyLivery: () -> Unit
) {
    val ship = state.selectedShip ?: return
    val livery = state.selectedLivery ?: ship.currentLivery

    val primaryCol = try { Color(android.graphics.Color.parseColor(livery.primaryColorHex)) } catch (e: Exception) { HelionCyan }
    val secondaryCol = try { Color(android.graphics.Color.parseColor(livery.accentColorHex)) } catch (e: Exception) { HelionDeepGraphite }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HelionCard(
                title = "Ship Hull Livery & Decals",
                badgeText = ship.registrationMark,
                accentColor = HelionAmber
            ) {
                // Procedural Sci-Fi Ship Silhouette Preview with Live Livery Colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HelionDeepGraphite)
                        .border(1.dp, HelionBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Tactical Crest with Livery Colors
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(secondaryCol, CutCornerShape(12.dp))
                                .border(3.dp, primaryCol, CutCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Rocket,
                                contentDescription = "Livery Silhouette",
                                tint = primaryCol,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = ship.shipName.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "REGISTRATION: [${ship.registrationMark}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Surface Wear Slider
                Text(
                    text = "WEATHERING & COMBAT WEAR: ${state.liveryWearPreview.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary
                )
                Slider(
                    value = state.liveryWearPreview,
                    onValueChange = onWearChanged,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = HelionAmber,
                        activeTrackColor = HelionAmber,
                        inactiveTrackColor = HelionSurfaceHigh
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onApplyLivery,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HelionCyan, contentColor = HelionDeepGraphite),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Request Livery Update from Server", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Available Livery Schemes
        item {
            Text(
                text = "AVAILABLE LIVERY PATTERNS",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary
            )
        }

        val availableLiveries = listOf(
            LiveryOption("liv-tactical-cyan", "Tactical Cyan (Standard)", "#00E5FF", "#111622", "Matte Composite"),
            LiveryOption("liv-stealth-void", "Void Ops Shadow", "#1E293B", "#334155", "Anti-Radar Carbon"),
            LiveryOption("liv-solaris-gold", "Directorate Prestige", "#FFD700", "#182032", "Ceramic Lacquer"),
            LiveryOption("liv-hazard-stripe", "Industrial Haz-Amber", "#FF9100", "#212529", "Safety Chevrons")
        )

        items(availableLiveries) { opt ->
            val isSelected = opt.liveryId == livery.liveryId
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectLivery(opt) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) HelionSurfaceVariant else HelionSurface,
                border = BorderStroke(1.dp, if (isSelected) HelionCyan else HelionBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val col1 = try { Color(android.graphics.Color.parseColor(opt.primaryColorHex)) } catch (e: Exception) { HelionCyan }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(col1)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = opt.name, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "Finish: ${opt.patternType}", style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                        }
                    }

                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = HelionCyan)
                    }
                }
            }
        }
    }
}

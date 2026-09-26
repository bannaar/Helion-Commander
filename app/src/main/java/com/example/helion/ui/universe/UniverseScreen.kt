package com.example.helion.ui.universe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.ResourceAbundance
import com.example.helion.core.model.RouteOptimizationMode
import com.example.helion.core.model.RoutePlanResult
import com.example.helion.core.model.SecurityClass
import com.example.helion.core.model.StarLaneType
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.ThreatLevel
import com.example.helion.ui.components.HelionCard
import com.example.helion.ui.components.SecurityBadge
import com.example.helion.ui.universe.StarSystemCard
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionBorderGlow
import com.example.ui.theme.HelionCyan
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
import com.example.ui.theme.HelionVoidBlack

@Composable
fun UniverseScreen(
    viewModel: UniverseViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("GALAXY MAP", "STAR SYSTEMS", "ROUTE PLANNER", "BOOKMARKS")

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().testTag("universe_screen")) {
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

        when (selectedTab) {
            0 -> GalaxyMapContent(
                state = state,
                onSelectSystem = { viewModel.selectSystem(it) },
                onSetAsOrigin = { viewModel.setOrigin(it.systemId) },
                onSetAsDestination = {
                    viewModel.setDestination(it.systemId)
                    selectedTab = 2 // jump to route planner
                },
                onOpenDrillDown = {
                    viewModel.openSystemDrillDown(it)
                    selectedTab = 1
                }
            )
            1 -> {
                val drillDown = state.drillDownSystem
                if (drillDown != null) {
                    StarSystemDrillDownScreen(
                        system = drillDown,
                        selectedPlanetTypeFilter = state.selectedPlanetTypeFilter,
                        onSelectPlanetTypeFilter = { viewModel.setPlanetTypeFilter(it) },
                        onBack = { viewModel.closeSystemDrillDown() },
                        onRouteToSystem = {
                            viewModel.setDestination(it.systemId)
                            viewModel.closeSystemDrillDown()
                            selectedTab = 2
                        },
                        onSetAsOrigin = {
                            viewModel.setOrigin(it.systemId)
                        },
                        onViewOnMap = {
                            viewModel.selectSystem(it)
                            viewModel.closeSystemDrillDown()
                            selectedTab = 0
                        }
                    )
                } else {
                    SystemDirectoryContent(
                        state = state,
                        onSearchQuery = { viewModel.setSearchQuery(it) },
                        onSelectThreatFilter = { viewModel.setThreatFilter(it) },
                        onSelectAbundanceFilter = { viewModel.setAbundanceFilter(it) },
                        onSelectSystem = { viewModel.selectSystem(it) },
                        onDrillDown = { viewModel.openSystemDrillDown(it) },
                        onViewOnMap = {
                            viewModel.selectSystem(it)
                            selectedTab = 0
                        },
                        onRouteHere = {
                            viewModel.setDestination(it.systemId)
                            selectedTab = 2
                        },
                        onSetAsOrigin = {
                            viewModel.setOrigin(it.systemId)
                        }
                    )
                }
            }
            2 -> RoutePlannerContent(
                state = state,
                onSelectOrigin = { viewModel.setOrigin(it) },
                onSelectDestination = { viewModel.setDestination(it) },
                onSelectMode = { viewModel.setRouteMode(it) },
                onBookmarkRoute = { viewModel.bookmarkCurrentRoute() }
            )
            3 -> RouteBookmarksContent(
                state = state,
                onLoadBookmark = { origin, dest ->
                    viewModel.setOrigin(origin)
                    viewModel.setDestination(dest)
                    selectedTab = 2
                },
                onDeleteBookmark = { viewModel.deleteBookmark(it) }
            )
        }
    }
}

// =============================================================================
// 1. 2D GALAXY MAP CANVAS
// =============================================================================
@Composable
fun GalaxyMapContent(
    state: UniverseUiState,
    onSelectSystem: (SystemNode) -> Unit,
    onSetAsOrigin: (SystemNode) -> Unit,
    onSetAsDestination: (SystemNode) -> Unit,
    onOpenDrillDown: (SystemNode) -> Unit = {}
) {
    val systems = state.systems.values.toList()
    val selected = state.selectedSystem ?: systems.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        // Map Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .background(HelionVoidBlack)
                .border(1.dp, HelionBorder)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(systems) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
                            // Find closest system to tap point
                            val tapped = systems.minByOrNull { sys ->
                                val px = sys.mapX * width
                                val py = sys.mapY * height
                                val dx = px - offset.x
                                val dy = py - offset.y
                                (dx * dx) + (dy * dy)
                            }
                            if (tapped != null) {
                                onSelectSystem(tapped)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw background grid lines
                val gridCols = 8
                for (i in 1..gridCols) {
                    val x = (w / gridCols) * i
                    drawLine(
                        color = HelionSurface.copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                }
                val gridRows = 8
                for (i in 1..gridRows) {
                    val y = (h / gridRows) * i
                    drawLine(
                        color = HelionSurface.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Starlane connections
                val drawnEdges = mutableSetOf<String>()
                systems.forEach { sys ->
                    val fromPos = Offset(sys.mapX * w, sys.mapY * h)
                    sys.connections.forEach { conn ->
                        val target = state.systems[conn.targetSystemId]
                        if (target != null) {
                            val edgeKey = listOf(sys.systemId, target.systemId).sorted().joinToString("-")
                            if (edgeKey !in drawnEdges) {
                                drawnEdges.add(edgeKey)
                                val toPos = Offset(target.mapX * w, target.mapY * h)

                                val (lineColor, strokeW) = when (conn.laneType) {
                                    StarLaneType.MAJOR_INTERFACTION_TRUNK -> HelionCyan to 3.5f
                                    StarLaneType.REGIONAL_PRIMARY -> HelionShieldBlue to 2.5f
                                    StarLaneType.SECONDARY -> HelionBorderGlow to 1.5f
                                    StarLaneType.FRONTIER -> HelionAmber to 1.8f
                                    StarLaneType.BACKWATER -> HelionDangerRed to 1.5f
                                }

                                drawLine(
                                    color = lineColor,
                                    start = fromPos,
                                    end = toPos,
                                    strokeWidth = strokeW
                                )
                            }
                        }
                    }
                }

                // Draw System Nodes
                systems.forEach { sys ->
                    val pos = Offset(sys.mapX * w, sys.mapY * h)
                    val isSelected = sys.systemId == selected?.systemId

                    val nodeColor = when (sys.securityClass) {
                        SecurityClass.HIGH_SECURITY -> HelionHighSecGreen
                        SecurityClass.LOW_SECURITY -> HelionLowSecOrange
                        SecurityClass.NULL_SECURITY -> HelionNullSecPurple
                    }

                    // Outer pulse ring if selected
                    if (isSelected) {
                        drawCircle(
                            color = HelionCyan.copy(alpha = 0.4f),
                            radius = 18f,
                            center = pos
                        )
                        drawCircle(
                            color = HelionCyan,
                            radius = 24f,
                            center = pos,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }

                    // Core Star dot
                    drawCircle(
                        color = nodeColor,
                        radius = if (sys.isCapital || sys.isCommerceHub) 10f else 7f,
                        center = pos
                    )
                }
            }

            // Legend Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                color = HelionDeepGraphite.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(0.8.dp, HelionBorder)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(HelionHighSecGreen, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "High-Sec", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(HelionLowSecOrange, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Low-Sec", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(HelionNullSecPurple, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "0.0 Null", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                    }
                }
            }
        }

        // Selected System Detail Drawer
        if (selected != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f),
                color = HelionSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selected.name.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = HelionTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selected.isCapital) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(2.dp), color = HelionAmber) {
                                        Text(text = "CAPITAL", color = HelionDeepGraphite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (selected.isCommerceHub) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(2.dp), color = HelionCyan) {
                                        Text(text = "COMMERCE HUB", color = HelionDeepGraphite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = "${selected.regionName} • Sovereign: ${selected.sovereignName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HelionTextSecondary
                            )
                        }

                        SecurityBadge(rating = selected.securityRating)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Primary Station: ${selected.primaryStationName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HelionCyan
                    )
                    Text(
                        text = "Active Gate Links: ${selected.connections.size} connections (${selected.connections.joinToString { it.targetSystemId.replace("sys-", "").replaceFirstChar { c -> c.uppercase() } }})",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Drill-down button
                    Button(
                        onClick = { onOpenDrillDown(selected) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("map_survey_planets_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HelionCyan,
                            contentColor = HelionDeepGraphite
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SURVEY PLANETS & MINING YIELDS (${selected.celestialBodies.size} WORLDS)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSetAsOrigin(selected) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HelionCyan)
                        ) {
                            Text("Set as Origin", color = HelionCyan)
                        }

                        Button(
                            onClick = { onSetAsDestination(selected) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite)
                        ) {
                            Text("Route Here", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 2. SYSTEM DIRECTORY TAB
// =============================================================================
@Composable
fun SystemDirectoryContent(
    state: UniverseUiState,
    onSearchQuery: (String) -> Unit,
    onSelectThreatFilter: (ThreatLevel?) -> Unit,
    onSelectAbundanceFilter: (ResourceAbundance?) -> Unit,
    onSelectSystem: (SystemNode) -> Unit,
    onDrillDown: (SystemNode) -> Unit,
    onViewOnMap: (SystemNode) -> Unit,
    onRouteHere: (SystemNode) -> Unit,
    onSetAsOrigin: (SystemNode) -> Unit
) {
    val totalPlanetsAcrossGalaxy = remember(state.systems) {
        state.systems.values.sumOf { it.celestialBodies.size }
    }
    val totalBeltsAcrossGalaxy = remember(state.systems) {
        state.systems.values.sumOf { it.asteroidBelts }
    }

    val filtered = state.systems.values.filter { sys ->
        val matchesQuery = state.searchQuery.isBlank() ||
                sys.name.contains(state.searchQuery, ignoreCase = true) ||
                sys.regionName.contains(state.searchQuery, ignoreCase = true) ||
                sys.sovereignName.contains(state.searchQuery, ignoreCase = true) ||
                sys.celestialBodies.any { planet ->
                    planet.name.contains(state.searchQuery, ignoreCase = true) ||
                            planet.type.displayName.contains(state.searchQuery, ignoreCase = true) ||
                            planet.miningYields.any { it.resourceName.contains(state.searchQuery, ignoreCase = true) }
                }

        val matchesThreat = state.selectedThreatFilter == null || sys.threatLevel == state.selectedThreatFilter
        val matchesAbundance = state.selectedAbundanceFilter == null || sys.overallAbundance == state.selectedAbundanceFilter

        matchesQuery && matchesThreat && matchesAbundance
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // GALAXY TELEMETRY OVERVIEW HEADER
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STAR SYSTEMS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                        Text("${state.systems.size} Charted", style = MaterialTheme.typography.titleMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(HelionBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SURVEYED WORLDS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                        Text("$totalPlanetsAcrossGalaxy Planets", style = MaterialTheme.typography.titleMedium, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(HelionBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MINING BELTS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 9.sp)
                        Text("$totalBeltsAcrossGalaxy Asteroids", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQuery,
                label = { Text("Search systems, regions, planets, minerals...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = HelionCyan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("systems_search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HelionCyan,
                    unfocusedBorderColor = HelionBorder
                ),
                shape = RoundedCornerShape(6.dp)
            )
        }

        // THREAT LEVEL FILTER CHIPS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "FILTER BY THREAT SEVERITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedThreatFilter == null,
                            onClick = { onSelectThreatFilter(null) },
                            label = { Text("All Threats", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionCyan,
                                selectedLabelColor = HelionDeepGraphite,
                                containerColor = HelionSurface,
                                labelColor = HelionTextSecondary
                            )
                        )
                    }
                    items(ThreatLevel.entries.toTypedArray()) { threat ->
                        FilterChip(
                            selected = state.selectedThreatFilter == threat,
                            onClick = {
                                onSelectThreatFilter(if (state.selectedThreatFilter == threat) null else threat)
                            },
                            label = { Text(threat.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionAmber,
                                selectedLabelColor = HelionDeepGraphite,
                                containerColor = HelionSurface,
                                labelColor = HelionTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // RESOURCE ABUNDANCE FILTER CHIPS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "FILTER BY RESOURCE RESERVES",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedAbundanceFilter == null,
                            onClick = { onSelectAbundanceFilter(null) },
                            label = { Text("All Reserves", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionCyan,
                                selectedLabelColor = HelionDeepGraphite,
                                containerColor = HelionSurface,
                                labelColor = HelionTextSecondary
                            )
                        )
                    }
                    items(ResourceAbundance.entries.toTypedArray()) { abundance ->
                        FilterChip(
                            selected = state.selectedAbundanceFilter == abundance,
                            onClick = {
                                onSelectAbundanceFilter(if (state.selectedAbundanceFilter == abundance) null else abundance)
                            },
                            label = { Text(abundance.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionHighSecGreen,
                                selectedLabelColor = HelionDeepGraphite,
                                containerColor = HelionSurface,
                                labelColor = HelionTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // RESULT COUNT HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHARTED STAR SYSTEMS (${filtered.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextMuted
                )
                Text(
                    text = "Tap system card to survey planets & yields",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionCyan,
                    fontSize = 10.sp
                )
            }
        }

        // STAR SYSTEM CARDS
        items(filtered) { sys ->
            StarSystemCard(
                system = sys,
                isSelected = state.selectedSystem?.systemId == sys.systemId,
                onSelectSystem = { onSelectSystem(sys) },
                onDrillDown = { onDrillDown(sys) },
                onViewOnMap = { onViewOnMap(sys) },
                onRouteHere = { onRouteHere(sys) },
                onSetAsOrigin = { onSetAsOrigin(sys) }
            )
        }

        // EMPTY STATE
        if (filtered.isEmpty()) {
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
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = HelionTextMuted, modifier = Modifier.size(36.dp))
                        Text("No Star Systems Found", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary)
                        Text("No systems match your active search terms and filters.", style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                        OutlinedButton(
                            onClick = {
                                onSearchQuery("")
                                onSelectThreatFilter(null)
                                onSelectAbundanceFilter(null)
                            },
                            border = BorderStroke(1.dp, HelionCyan)
                        ) {
                            Text("Reset All Filters", color = HelionCyan)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 3. ROUTE PLANNER TAB
// =============================================================================
@Composable
fun RoutePlannerContent(
    state: UniverseUiState,
    onSelectOrigin: (String) -> Unit,
    onSelectDestination: (String) -> Unit,
    onSelectMode: (RouteOptimizationMode) -> Unit,
    onBookmarkRoute: () -> Unit
) {
    val systems = state.systems.values.toList()
    val route = state.calculatedRoute

    var originExpanded by remember { mutableStateOf(false) }
    var destExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Selection Controls
        item {
            HelionCard(title = "Starlane Navigation Computer", accentColor = HelionCyan) {
                // Origin selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "ORIGIN SYSTEM", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(
                            text = state.systems[state.originSystemId]?.name ?: "Select Origin",
                            style = MaterialTheme.typography.titleLarge,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { originExpanded = true }
                        )
                    }
                    OutlinedButton(onClick = { originExpanded = true }) {
                        Text("Change", color = HelionCyan)
                    }
                    DropdownMenu(expanded = originExpanded, onDismissRequest = { originExpanded = false }) {
                        systems.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.securityRating})") },
                                onClick = {
                                    onSelectOrigin(s.systemId)
                                    originExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Destination selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "DESTINATION SYSTEM", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(
                            text = state.systems[state.destinationSystemId]?.name ?: "Select Destination",
                            style = MaterialTheme.typography.titleLarge,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { destExpanded = true }
                        )
                    }
                    OutlinedButton(onClick = { destExpanded = true }) {
                        Text("Change", color = HelionAmber)
                    }
                    DropdownMenu(expanded = destExpanded, onDismissRequest = { destExpanded = false }) {
                        systems.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.securityRating})") },
                                onClick = {
                                    onSelectDestination(s.systemId)
                                    destExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optimization Mode Chips
                Text(text = "ROUTING ALGORITHM / POLICY:", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RouteOptimizationMode.values()) { mode ->
                        val isSelected = state.routeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMode(mode) },
                            label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionCyan,
                                selectedLabelColor = HelionDeepGraphite
                            )
                        )
                    }
                }
            }
        }

        // Route Summary & Steps
        if (route != null) {
            item {
                HelionCard(
                    title = "Flightpath Analysis",
                    badgeText = "${route.totalJumps} Jumps",
                    badgeColor = HelionAmber,
                    accentColor = HelionAmber
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "TOTAL DISTANCE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${String.format("%.1f", route.totalDistanceLy)} LY", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "MIN SECURITY", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            SecurityBadge(rating = route.lowestSecurityRating)
                        }
                    }

                    if (route.warnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        route.warnings.forEach { warn ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HelionDangerRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = HelionDangerRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = warn, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onBookmarkRoute,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HelionCyan, contentColor = HelionDeepGraphite),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bookmark Route Plan", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text(text = "TRAVERSED STARLANES & GATES (${route.segments.size})", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
            }

            items(route.segments) { seg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = HelionSurface,
                    border = BorderStroke(1.dp, HelionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = seg.fromSystem.name, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HelionCyan, modifier = Modifier.padding(horizontal = 6.dp).size(14.dp))
                                Text(text = seg.toSystem.name, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Text(text = "${seg.laneType.label} • ${seg.distanceLy} LY", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                        }

                        SecurityBadge(rating = seg.toSystem.securityRating)
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NO ROUTE FOUND under current policy constraints. Try relaxing 'High-Sec Only' or 'Avoid Low-Sec'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HelionDangerRed
                    )
                }
            }
        }
    }
}

// =============================================================================
// 4. BOOKMARKS TAB
// =============================================================================
@Composable
fun RouteBookmarksContent(
    state: UniverseUiState,
    onLoadBookmark: (originId: String, destId: String) -> Unit,
    onDeleteBookmark: (String) -> Unit
) {
    if (state.bookmarks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = HelionTextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "No saved route bookmarks yet.", style = MaterialTheme.typography.titleMedium, color = HelionTextSecondary)
                Text(text = "Calculate flightpaths and bookmark them for rapid navigation.", style = MaterialTheme.typography.bodySmall, color = HelionTextMuted)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.bookmarks) { bm ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLoadBookmark(bm.originSystemId, bm.destinationSystemId) }
                    ) {
                        Text(text = bm.title, style = MaterialTheme.typography.titleLarge, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "${bm.originName} ➔ ${bm.destinationName} • ${bm.jumps} Jumps", style = MaterialTheme.typography.bodySmall, color = HelionCyan)
                    }

                    IconButton(onClick = { onDeleteBookmark(bm.bookmarkId) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Bookmark", tint = HelionTextMuted)
                    }
                }
            }
        }
    }
}

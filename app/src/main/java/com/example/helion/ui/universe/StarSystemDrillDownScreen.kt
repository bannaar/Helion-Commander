package com.example.helion.ui.universe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.CelestialBody
import com.example.helion.core.model.PlanetMiningYield
import com.example.helion.core.model.PlanetType
import com.example.helion.core.model.ResourceAbundance
import com.example.helion.core.model.SystemNode
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
fun StarSystemDrillDownScreen(
    system: SystemNode,
    selectedPlanetTypeFilter: PlanetType?,
    onSelectPlanetTypeFilter: (PlanetType?) -> Unit,
    onBack: () -> Unit,
    onRouteToSystem: (SystemNode) -> Unit,
    onSetAsOrigin: (SystemNode) -> Unit,
    onViewOnMap: (SystemNode) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    var actionBannerMessage by remember { mutableStateOf<String?>(null) }

    // Filter planets by selected type
    val displayedPlanets = remember(system, selectedPlanetTypeFilter) {
        if (selectedPlanetTypeFilter == null) {
            system.celestialBodies
        } else {
            system.celestialBodies.filter { it.type == selectedPlanetTypeFilter }
        }
    }

    // Unique planet types in this system
    val availablePlanetTypes = remember(system) {
        system.celestialBodies.map { it.type }.distinct()
    }

    // Calculate system-wide totals
    val totalEstimatedTonsPerHour = remember(system) {
        system.celestialBodies.flatMap { it.miningYields }.sumOf { it.estimatedTonsPerHour }
    }

    val totalFacilities = remember(system) {
        system.celestialBodies.sumOf { it.activeExtractionFacilities }
    }

    val threatColor = when (system.threatLevel) {
        ThreatLevel.MINIMAL -> HelionHighSecGreen
        ThreatLevel.LOW -> HelionCyan
        ThreatLevel.MODERATE -> HelionAmber
        ThreatLevel.HIGH -> HelionLowSecOrange
        ThreatLevel.EXTREME -> HelionNullSecPurple
    }

    val abundanceColor = when (system.overallAbundance) {
        ResourceAbundance.PRISTINE -> HelionHighSecGreen
        ResourceAbundance.RICH -> HelionCyan
        ResourceAbundance.MODERATE -> HelionAmber
        ResourceAbundance.DEPLETED -> HelionTextMuted
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HelionVoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP BREADCRUMB & BACK NAVIGATION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_to_systems_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Systems",
                        tint = HelionCyan
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Text(
                        text = "GALAXY DIRECTORY  /  DRILL-DOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${system.name.uppercase()} SYSTEM SURVEY",
                        style = MaterialTheme.typography.titleLarge,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action confirmation banner if triggered
        if (actionBannerMessage != null) {
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
                            text = actionBannerMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { actionBannerMessage = null }, modifier = Modifier.size(20.dp)) {
                            Text("✕", color = HelionAmber, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. SYSTEM TELEMETRY HERO CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_telemetry_hero_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.2.dp, HelionCyan.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = system.name.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = HelionTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (system.isCapital) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = HelionAmber.copy(alpha = 0.2f),
                                        border = BorderStroke(0.8.dp, HelionAmber)
                                    ) {
                                        Text(
                                            text = "CAPITAL",
                                            color = HelionAmber,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (system.isCommerceHub) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = HelionCyan.copy(alpha = 0.2f),
                                        border = BorderStroke(0.8.dp, HelionCyan)
                                    ) {
                                        Text(
                                            text = "COMMERCE HUB",
                                            color = HelionCyan,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${system.regionName} • Sovereign: ${system.sovereignName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HelionTextSecondary
                            )
                        }

                        SecurityBadge(rating = system.securityRating)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Threat and Abundance Matrix Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            color = HelionDeepGraphite,
                            border = BorderStroke(1.dp, threatColor.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = threatColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "THREAT LEVEL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionTextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = system.threatLevel.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = threatColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = system.threatDescription,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextSecondary,
                                    fontSize = 9.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            color = HelionDeepGraphite,
                            border = BorderStroke(1.dp, abundanceColor.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Diamond,
                                        contentDescription = null,
                                        tint = abundanceColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "RESOURCE YIELD",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionTextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = system.overallAbundance.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = abundanceColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${system.asteroidBelts} Belts • ${system.celestialBodies.size} Planets Surveyed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Aggregate Mining Statistics Banner
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HelionDeepGraphite,
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
                                Text(
                                    text = "PLANETARY BODIES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextMuted,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${system.celestialBodies.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HelionCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(modifier = Modifier.height(24.dp).width(1.dp).background(HelionBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ACTIVE FACILITIES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextMuted,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "$totalFacilities Arrays",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HelionHighSecGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(modifier = Modifier.height(24.dp).width(1.dp).background(HelionBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "MINING POTENTIAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextMuted,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${numberFormat.format(totalEstimatedTonsPerHour)} T/HR",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HelionAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Navigation & Route Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onViewOnMap(system) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HelionCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("2D Map", color = HelionCyan, style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                onSetAsOrigin(system)
                                actionBannerMessage = "${system.name} set as route origin."
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HelionBorder)
                        ) {
                            Text("Set Origin", color = HelionTextSecondary, style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { onRouteToSystem(system) },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("drill_down_plot_route_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Plot Route", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // 3. PLANET TYPE FILTERS BAR
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLANETARY BODIES & MINING EXTRACTION CATALOG",
                        style = MaterialTheme.typography.labelMedium,
                        color = HelionTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Showing ${displayedPlanets.size} of ${system.celestialBodies.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionCyan
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedPlanetTypeFilter == null,
                            onClick = { onSelectPlanetTypeFilter(null) },
                            label = { Text("All Types (${system.celestialBodies.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HelionCyan,
                                selectedLabelColor = HelionDeepGraphite,
                                containerColor = HelionSurface,
                                labelColor = HelionTextSecondary
                            )
                        )
                    }

                    items(availablePlanetTypes) { planetType ->
                        val count = system.celestialBodies.count { it.type == planetType }
                        FilterChip(
                            selected = selectedPlanetTypeFilter == planetType,
                            onClick = {
                                onSelectPlanetTypeFilter(if (selectedPlanetTypeFilter == planetType) null else planetType)
                            },
                            label = { Text("${planetType.displayName} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(planetType.secondaryColorHex),
                                selectedLabelColor = HelionDeepGraphite,
                                containerColor = HelionSurface,
                                labelColor = HelionTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // 4. DETAILED PLANET CARDS WITH MINING YIELDS
        items(displayedPlanets) { planet ->
            PlanetDetailCard(
                planet = planet,
                onTargetPlanet = {
                    actionBannerMessage = "Mining coordinates locked: ${planet.name} [Orbital Slot ${planet.orbitalRadiusAu} AU]"
                }
            )
        }

        // Empty state if filtered out
        if (displayedPlanets.isEmpty()) {
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
                        Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = HelionTextMuted, modifier = Modifier.size(36.dp))
                        Text(
                            text = "No Planets Found For Selected Type",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary
                        )
                        Text(
                            text = "Try clearing the planet type filter above to view all charted celestial bodies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary
                        )
                        OutlinedButton(
                            onClick = { onSelectPlanetTypeFilter(null) },
                            border = BorderStroke(1.dp, HelionCyan)
                        ) {
                            Text("Reset Filter", color = HelionCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanetDetailCard(
    planet: CelestialBody,
    onTargetPlanet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    var isExpanded by remember { mutableStateOf(true) }

    val planetPrimaryColor = Color(planet.type.primaryColorHex)
    val planetSecondaryColor = Color(planet.type.secondaryColorHex)

    val totalPlanetTonsPerHour = planet.miningYields.sumOf { it.estimatedTonsPerHour }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("planet_card_${planet.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // HEADER: Planet Icon Sphere + Name + Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Canvas Planet Sphere
                PlanetSphereGraphic(
                    primaryColor = planetPrimaryColor,
                    secondaryColor = planetSecondaryColor,
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = planet.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp),
                            color = planetPrimaryColor.copy(alpha = 0.2f),
                            border = BorderStroke(0.8.dp, planetSecondaryColor)
                        ) {
                            Text(
                                text = planet.type.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = planetSecondaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Orbit: ${planet.orbitalRadiusAu} AU • Radius: ${numberFormat.format(planet.radiusKm)} km • ${planet.surveyQualityPercentage}% Survey Quality",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PLANET ENVIRONMENT & SURFACE HAZARDS
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = HelionDeepGraphite,
                border = BorderStroke(0.6.dp, HelionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (planet.surfaceHazards.contains("Active") || planet.surfaceHazards.contains("Extreme") || planet.surfaceHazards.contains("War")) HelionDangerRed else HelionCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ENVIRONMENT: ${planet.surfaceHazards}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextPrimary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MINING YIELDS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MINING YIELDS & ORE EXTRACTION RATES",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = "${planet.activeExtractionFacilities} Active Facilities • ${numberFormat.format(totalPlanetTonsPerHour)} T/HR",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // List of mining deposits on this planet
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                planet.miningYields.forEach { yield ->
                    MiningYieldRow(yield = yield)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HelionBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // FOOTER ACTION: Target Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planet.type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onTargetPlanet,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionAmber),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("target_planet_${planet.id}")
                ) {
                    Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target Orbit", color = HelionAmber, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun MiningYieldRow(
    yield: PlanetMiningYield,
    modifier: Modifier = Modifier
) {
    val barColor = when (yield.abundance) {
        ResourceAbundance.PRISTINE -> HelionHighSecGreen
        ResourceAbundance.RICH -> HelionCyan
        ResourceAbundance.MODERATE -> HelionAmber
        ResourceAbundance.DEPLETED -> HelionTextMuted
    }

    val difficultyColor = when (yield.extractionDifficulty) {
        "Low Risk" -> HelionHighSecGreen
        "Moderate" -> HelionCyan
        "Hazardous" -> HelionHazardOrange
        "Extreme" -> HelionDangerRed
        else -> HelionTextSecondary
    }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = yield.resourceName,
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${yield.category}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextMuted,
                        fontSize = 9.sp
                    )
                }

                Text(
                    text = "${yield.estimatedTonsPerHour} Tons/Hr",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Gauge progress bar
            LinearProgressIndicator(
                progress = { yield.yieldPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = barColor,
                trackColor = HelionSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deposit Concentration: ${yield.yieldPercentage}% Yield (${yield.abundance.label})",
                    style = MaterialTheme.typography.labelSmall,
                    color = barColor,
                    fontSize = 9.sp
                )

                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = difficultyColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = yield.extractionDifficulty.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = difficultyColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanetSphereGraphic(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer atmospheric glow
        drawCircle(
            color = secondaryColor.copy(alpha = 0.25f),
            radius = radius * 1.15f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Planetary Body with 3D spherical gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor,
                    primaryColor,
                    HelionVoidBlack
                ),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                radius = radius * 1.25f
            ),
            radius = radius,
            center = center
        )

        // Equator ring/streak
        drawLine(
            color = secondaryColor.copy(alpha = 0.4f),
            start = Offset(center.x - radius * 0.85f, center.y + radius * 0.2f),
            end = Offset(center.x + radius * 0.85f, center.y - radius * 0.2f),
            strokeWidth = 1.2.dp.toPx()
        )
    }
}

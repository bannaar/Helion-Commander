package com.example.helion.ui.universe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun StarSystemCard(
    system: SystemNode,
    isSelected: Boolean,
    onSelectSystem: () -> Unit,
    onViewOnMap: () -> Unit,
    onRouteHere: () -> Unit,
    onSetAsOrigin: () -> Unit,
    onDrillDown: () -> Unit = onSelectSystem,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDrillDown() }
            .testTag("system_card_${system.systemId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HelionSurfaceVariant else HelionSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.8.dp,
            color = if (isSelected) HelionCyan else HelionBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. TOP HEADER: Name, Region, Security Class
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = system.name.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
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

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${system.regionName} • Sovereign: ${system.sovereignName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary
                    )
                }

                SecurityBadge(rating = system.securityRating)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. THREAT LEVEL & RESOURCE ABUNDANCE MATRIX PILLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Threat Level Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = HelionDeepGraphite,
                    border = BorderStroke(1.dp, threatColor.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (system.threatLevel.level >= 4) Icons.Default.Warning else Icons.Default.Security,
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
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = system.threatLevel.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = threatColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Level ${system.threatLevel.level}/5 Severity",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                // Resource Abundance Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = HelionDeepGraphite,
                    border = BorderStroke(1.dp, abundanceColor.copy(alpha = 0.6f))
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
                                text = "RESOURCE ABUNDANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = system.overallAbundance.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = abundanceColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${system.asteroidBelts} Belts • ${system.celestialCount} Celestial Bodies",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. THREAT & RESOURCE DESCRIPTIONS
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HelionDeepGraphite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SECURITY INTEL: ${system.threatDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextPrimary
                    )
                    Text(
                        text = "GEOLOGICAL REPORT: ${system.resourceSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. RESOURCE CONCENTRATION GAUGES
            if (system.resources.isNotEmpty()) {
                Text(
                    text = "SURVEYED RESOURCE DEPOSITS & MINING RESERVES",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    system.resources.forEach { res ->
                        val barColor = when (res.abundance) {
                            ResourceAbundance.PRISTINE -> HelionHighSecGreen
                            ResourceAbundance.RICH -> HelionCyan
                            ResourceAbundance.MODERATE -> HelionAmber
                            ResourceAbundance.DEPLETED -> HelionTextMuted
                        }
                        StatBar(
                            label = "${res.resourceName} (${res.resourceCategory})",
                            currentValue = "${res.abundance.label} • ${res.yieldScore}% Yield",
                            percent = res.yieldScore / 100f,
                            barColor = barColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 5. STARLANE NETWORK CONNECTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STARLANE GATE CONNECTIONS (${system.connections.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary
                )
                Text(
                    text = "Hub: ${system.primaryStationName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionAmber
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                system.connections.take(3).forEach { conn ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        color = HelionDeepGraphite,
                        border = BorderStroke(0.6.dp, HelionBorder)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                            Text(
                                text = conn.targetSystemId.replace("sys-", "").replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${conn.distanceLy} LY",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HelionBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // DRILL-DOWN AFFORDANCE: SURVEY PLANETS & MINING YIELDS
            Button(
                onClick = onDrillDown,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drill_down_button_${system.systemId}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HelionCyan,
                    contentColor = HelionDeepGraphite
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SURVEY PLANETS & MINING YIELDS (${system.celestialBodies.size} WORLDS)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewOnMap,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionCyan)
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Map", color = HelionCyan, style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onSetAsOrigin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionBorder)
                ) {
                    Text("Set Origin", color = HelionTextSecondary, style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onRouteHere,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("route_here_button_${system.systemId}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HelionAmber,
                        contentColor = HelionDeepGraphite
                    ),
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

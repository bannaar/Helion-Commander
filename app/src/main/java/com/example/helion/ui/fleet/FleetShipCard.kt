package com.example.helion.ui.fleet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.ModuleSlotCategory
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.ui.components.StatBar
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionBorderGlow
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionCyanGlow
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHazardOrange
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionLowSecOrange
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
fun FleetShipCard(
    ship: OwnedShipInstance,
    isSelected: Boolean,
    isExpanded: Boolean,
    isApplyingAction: Boolean,
    onSelectShip: () -> Unit,
    onToggleExpand: () -> Unit,
    onActivateShip: () -> Unit,
    onConfigureLoadout: () -> Unit,
    onCustomizeLivery: () -> Unit,
    onPerformMaintenance: () -> Unit = {},
    onSimulateWear: () -> Unit = {},
    onTestMaintenanceAlert: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val liveryColor = try {
        Color(android.graphics.Color.parseColor(ship.currentLivery.primaryColorHex))
    } catch (e: Exception) {
        HelionCyan
    }

    val powerPercent = (ship.totalPowerDrawMw / ship.hullDefinition.basePowerOutputMw).coerceIn(0f, 1.5f)
    val isPowerCritical = ship.isPowerOverloaded

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectShip() }
            .testTag("ship_card_${ship.instanceId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HelionSurfaceVariant else HelionSurface
        ),
        border = BorderStroke(
            width = if (ship.isActiveShip) 1.5.dp else if (isSelected) 1.dp else 0.8.dp,
            color = if (ship.isActiveShip) HelionAmber else if (isSelected) HelionCyan else HelionBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. TOP HEADER: Ship Identity, Class, and Active Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ship.shipName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Active Flagship or Docked Pill
                        if (ship.isActiveShip) {
                            Surface(
                                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                                color = HelionAmber.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, HelionAmber)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(HelionAmber)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "ACTIVE FLAGSHIP",
                                        color = HelionAmber,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HelionSurfaceHigh
                            ) {
                                Text(
                                    text = "STORED IN HANGAR",
                                    color = HelionTextMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ship.hullDefinition.hullName} • ${ship.hullDefinition.shipClass} • ${ship.hullDefinition.role}",
                        style = MaterialTheme.typography.titleMedium,
                        color = HelionCyan
                    )
                    Text(
                        text = "Manufacturer: ${ship.hullDefinition.manufacturer} • Reg: [${ship.registrationMark}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextSecondary
                    )
                }

                // Livery Palette Preview
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HelionDeepGraphite,
                    border = BorderStroke(1.dp, HelionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(liveryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ship.currentLivery.name.take(12),
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. LOCATION & HANGAR READOUT
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HelionDeepGraphite,
                border = BorderStroke(0.8.dp, HelionBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = if (ship.currentLocationSystemName == "Kepler") HelionHighSecGreen else HelionAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${ship.currentLocationStationName} (${ship.currentLocationSystemName} System)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (ship.currentLocationSystemName == "Kepler") "IN LOCAL DOCK" else "REMOTE DOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (ship.currentLocationSystemName == "Kepler") HelionHighSecGreen else HelionAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. TELEMETRY & VITALS GAUGES (Hull, Fuel, Cargo, Power Grid)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Hull Integrity Gauge
                val hullColor = when {
                    ship.hullConditionPercent > 85f -> HelionHighSecGreen
                    ship.hullConditionPercent > 50f -> HelionAmber
                    else -> HelionDangerRed
                }
                StatBar(
                    label = "Hull Integrity",
                    currentValue = "${ship.hullConditionPercent}% (${if (ship.hullConditionPercent >= 90f) "Nominal Armor" else "Repairs Advised"})",
                    percent = ship.hullConditionPercent / 100f,
                    barColor = hullColor
                )

                // Sub-Space Fuel Gauge
                StatBar(
                    label = "Sub-Space Fuel Reserves",
                    currentValue = "${ship.fuelPercent}% (Estimated 4 Jumps)",
                    percent = ship.fuelPercent / 100f,
                    barColor = HelionCyan
                )

                // Cargo Bay Capacity
                StatBar(
                    label = "Cargo Bay Allocation",
                    currentValue = "${ship.currentCargoTons} / ${ship.calculatedCargoCapacity} TONS",
                    percent = (ship.currentCargoTons.toFloat() / ship.calculatedCargoCapacity.toFloat()).coerceIn(0f, 1f),
                    barColor = HelionAmber
                )

                // Power Grid Utilization
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "FUSION CORE GRID DRAW",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPowerCritical) HelionDangerRed else HelionTextSecondary
                        )
                        Text(
                            text = "${String.format("%.2f", ship.totalPowerDrawMw)} / ${ship.hullDefinition.basePowerOutputMw} MW ${if (isPowerCritical) "[OVERLOAD]" else "[STABLE]"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPowerCritical) HelionDangerRed else HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { powerPercent.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (isPowerCritical) HelionDangerRed else HelionCyan,
                        trackColor = HelionSurfaceHigh
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. COMBAT, PROPULSION & PHYSICAL SPECIFICATIONS GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shield & Armor Spec Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = HelionSurfaceHigh
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = HelionShieldBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "DEFENSES", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${ship.hullDefinition.baseShieldRating} MJ Shield",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ship.hullDefinition.baseArmorRating} Armor Rating",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary
                        )
                    }
                }

                // Speed & Jump Spec Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = HelionSurfaceHigh
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "PROPULSION", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${ship.hullDefinition.baseSpeedMs} m/s",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ship.hullDefinition.maxJumpRangeLy} LY Jump Range",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary
                        )
                    }
                }

                // Total Mass & Wear Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = HelionSurfaceHigh
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "TOTAL MASS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.1f", ship.totalMassTons)} T",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${ship.wearPercent.toInt()}% Wear",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. INSTALLED MODULE QUICK-SCAN CHIPS
            Text(
                text = "INSTALLED HARDPOINTS & BAY AVIONICS (${ship.slots.count { it.installedModule != null }}/${ship.slots.size})",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val previewModules = ship.slots.mapNotNull { it.installedModule }.take(3)
                previewModules.forEach { mod ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        color = HelionDeepGraphite,
                        border = BorderStroke(0.6.dp, HelionBorder)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                            Text(
                                text = mod.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionCyan,
                                maxLines = 1,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Grade ${mod.grade} • S${mod.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionTextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. EXPANDABLE DETAILED TECH-SHEET
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp)
                    .testTag("expand_specs_button_${ship.instanceId}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Detailed Module Breakdown" else "View All Physical Slots & Modules",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionCyan,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = HelionCyan
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ship.slots.forEach { slot ->
                        val mod = slot.installedModule
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite,
                            border = BorderStroke(0.6.dp, HelionBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(HelionSurfaceHigh, RoundedCornerShape(3.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${slot.size}",
                                            color = HelionCyan,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = slot.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HelionTextMuted
                                        )
                                        Text(
                                            text = mod?.displayName ?: "Empty Mount",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (mod != null) HelionTextPrimary else HelionTextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (mod != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${mod.powerDrawMw} MW",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HelionAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${mod.massTons} T",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HelionTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6B. SHIPYARD MAINTENANCE & LOCAL NOTIFICATION CONTROLS
            if (ship.requiresMaintenance) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HelionDangerRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.2.dp, HelionDangerRed),
                    modifier = Modifier.fillMaxWidth().testTag("maintenance_alert_card_${ship.instanceId}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Maintenance Warning",
                                    tint = HelionDangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "MAINTENANCE OVERHAUL REQUIRED",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = HelionDangerRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ship.maintenanceStatusSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HelionTextPrimary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = HelionDangerRed,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "ALERT ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hull or avionics degradation exceeds safe operational threshold. Local device notification dispatched.",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onPerformMaintenance,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HelionHighSecGreen,
                                    contentColor = HelionDeepGraphite
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1.4f).testTag("repair_ship_button_${ship.instanceId}")
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Drydock Repair (100%)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onTestMaintenanceAlert,
                                border = BorderStroke(1.dp, HelionAmber),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f).testTag("test_maintenance_alert_${ship.instanceId}")
                            ) {
                                Text("Test Alert", color = HelionAmber, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else {
                // If ship is healthy, provide diagnostic simulation controls in case user wants to test maintenance alert
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HelionHighSecGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Systems Nominal • Maintenance Telemetry Armed",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onSimulateWear,
                        border = BorderStroke(0.6.dp, HelionBorder),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.testTag("simulate_wear_button_${ship.instanceId}")
                    ) {
                        Text("Simulate Wear & Alert", color = HelionAmber, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = HelionBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // 7. PRIMARY ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary action: Outfitting shortcut
                OutlinedButton(
                    onClick = onConfigureLoadout,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionCyan)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Outfitting", color = HelionCyan, style = MaterialTheme.typography.labelSmall)
                }

                // Secondary action: Livery shortcut
                OutlinedButton(
                    onClick = onCustomizeLivery,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionBorder)
                ) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = HelionTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Livery", color = HelionTextSecondary, style = MaterialTheme.typography.labelSmall)
                }

                // Primary action: Set active ship on server
                if (!ship.isActiveShip) {
                    Button(
                        onClick = onActivateShip,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("activate_ship_button_${ship.instanceId}"),
                        enabled = !isApplyingAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HelionAmber,
                            contentColor = HelionDeepGraphite
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Rocket, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deploy Ship", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

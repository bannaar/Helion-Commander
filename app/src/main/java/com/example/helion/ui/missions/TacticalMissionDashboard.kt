package com.example.helion.ui.missions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.MissionCategory
import com.example.helion.core.model.MissionChartMode
import com.example.helion.core.model.MissionDashboardMetrics
import com.example.helion.core.model.WeeklyMissionRecord
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
fun TacticalMissionDashboardContent(
    metrics: MissionDashboardMetrics,
    selectedWeekIndex: Int,
    chartMode: MissionChartMode,
    onSelectWeek: (Int) -> Unit,
    onSetChartMode: (MissionChartMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val selectedRecord = metrics.weeklyRecords.getOrNull(selectedWeekIndex)
        ?: metrics.weeklyRecords.lastOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tactical_missions_summary_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. DASHBOARD HEADER & TITLE
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HelionDeepGraphite,
                border = BorderStroke(1.dp, HelionCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(HelionCyan.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, HelionCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = HelionCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TACTICAL MISSIONS DASHBOARD",
                                style = MaterialTheme.typography.titleMedium,
                                color = HelionTextPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HelionHighSecGreen.copy(alpha = 0.2f),
                                border = BorderStroke(0.6.dp, HelionHighSecGreen)
                            ) {
                                Text(
                                    text = "LIVE TELEMETRY",
                                    color = HelionHighSecGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Historical weekly completion rate, fleet sorties velocity, and bounty yields.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary
                        )
                    }
                }
            }
        }

        // 2. PRIMARY KPI METRIC CARDS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardKpiCard(
                    title = "TOTAL COMPLETED",
                    value = "${metrics.totalCompletedMissions}",
                    unit = "Missions",
                    accentColor = HelionCyan,
                    icon = Icons.Default.AssignmentTurnedIn,
                    trendLabel = "+18% vs prev",
                    modifier = Modifier.weight(1f).testTag("kpi_total_completed")
                )
                DashboardKpiCard(
                    title = "WEEKLY VELOCITY",
                    value = String.format("%.1f", metrics.weeklyAverageCompleted),
                    unit = "Ops / Week",
                    accentColor = HelionAmber,
                    icon = Icons.Default.Speed,
                    trendLabel = "Quota: ${metrics.quotaTargetPerWeek}/wk",
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
                    value = "${metrics.totalCreditsEarned / 1000}k",
                    unit = "GSC",
                    accentColor = HelionHighSecGreen,
                    icon = Icons.Default.MonetizationOn,
                    trendLabel = "Authoritative",
                    modifier = Modifier.weight(1f).testTag("kpi_total_bounties")
                )
                DashboardKpiCard(
                    title = "SUCCESS RATE",
                    value = "${metrics.averageSuccessRatePercent}%",
                    unit = "Objective Met",
                    accentColor = HelionShieldBlue,
                    icon = Icons.Default.CheckCircle,
                    trendLabel = "High-Sec Cert",
                    modifier = Modifier.weight(1f).testTag("kpi_success_rate")
                )
            }
        }

        // 3. WEEKLY COMPLETED MISSIONS BAR CHART
        item {
            WeeklyMissionsBarChartCard(
                records = metrics.weeklyRecords,
                quotaTarget = metrics.quotaTargetPerWeek,
                selectedIndex = selectedWeekIndex,
                chartMode = chartMode,
                onSelectIndex = onSelectWeek,
                onSetMode = onSetChartMode
            )
        }

        // 4. SELECTED WEEK DETAIL CARD
        if (selectedRecord != null) {
            item {
                SelectedWeekDetailCard(
                    record = selectedRecord,
                    numberFormat = numberFormat
                )
            }
        }

        // 5. MISSION CATEGORY PERFORMANCE BREAKDOWN
        item {
            CategoryPerformanceBreakdownCard(records = metrics.weeklyRecords)
        }

        // 6. FLEET OPERATIONS LOG & TOP SHIP RECORD
        item {
            FleetSortiesRosterCard(records = metrics.weeklyRecords)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =============================================================================
// KPI CARD COMPONENT
// =============================================================================
@Composable
fun DashboardKpiCard(
    title: String,
    value: String,
    unit: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trendLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextMuted,
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HelionTextPrimary,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = trendLabel,
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// =============================================================================
// WEEKLY MISSIONS BAR CHART COMPONENT (RECHARTS STYLE FOR JETPACK COMPOSE)
// =============================================================================
@Composable
fun WeeklyMissionsBarChartCard(
    records: List<WeeklyMissionRecord>,
    quotaTarget: Int,
    selectedIndex: Int,
    chartMode: MissionChartMode,
    onSelectIndex: (Int) -> Unit,
    onSetMode: (MissionChartMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_missions_bar_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Chart Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPLETED MISSIONS PER WEEK",
                        style = MaterialTheme.typography.titleSmall,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Tactical sorties telemetry over the last 6 cycles",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Quota badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = HelionHazardOrange.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, HelionHazardOrange)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(HelionHazardOrange, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "QUOTA: $quotaTarget/WK",
                            color = HelionHazardOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Mode Filter Chips (Total Completed vs By Category vs GSC Earned)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MissionChartMode.entries.forEach { mode ->
                    FilterChip(
                        selected = chartMode == mode,
                        onClick = { onSetMode(mode) },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (chartMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionCyan,
                            selectedLabelColor = HelionDeepGraphite,
                            containerColor = HelionSurfaceVariant,
                            labelColor = HelionTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (chartMode == mode) HelionCyan else HelionBorder,
                            enabled = true,
                            selected = chartMode == mode
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend when in Category mode
            if (chartMode == MissionChartMode.BY_CATEGORY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendItem(label = "Combat", color = HelionDangerRed)
                    LegendItem(label = "Mining", color = HelionAmber)
                    LegendItem(label = "Recon", color = HelionCyan)
                    LegendItem(label = "Escort", color = HelionShieldBlue)
                    LegendItem(label = "Black Ops", color = HelionNullSecPurple)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Interactive Bar Chart Canvas
            val maxMissions = (records.maxOfOrNull { it.completedCount } ?: 12).coerceAtLeast(14)
            val maxCreditsThousands = (records.maxOfOrNull { (it.creditsEarned / 1000).toInt() } ?: 400).coerceAtLeast(400)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(HelionDeepGraphite.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .border(1.dp, HelionBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag("bar_chart_canvas_box")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(records, chartMode) {
                            detectTapGestures { offset ->
                                val availableWidth = size.width - 40f
                                val barSlotWidth = availableWidth / records.size
                                val relativeX = offset.x - 30f
                                if (relativeX >= 0) {
                                    val index = (relativeX / barSlotWidth).toInt().coerceIn(0, records.lastIndex)
                                    onSelectIndex(index)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val leftMargin = 30f
                    val bottomMargin = 26f
                    val chartWidth = w - leftMargin - 10f
                    val chartHeight = h - bottomMargin

                    // Draw 4 Horizontal Grid Lines and Y-Axis Ticks
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = chartHeight - (i.toFloat() / gridSteps.toFloat()) * chartHeight
                        drawLine(
                            color = HelionBorder.copy(alpha = 0.4f),
                            start = Offset(leftMargin, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Quota Reference Line if viewing Total or Category
                    if (chartMode != MissionChartMode.CREDITS_EARNED) {
                        val quotaRatio = (quotaTarget.toFloat() / maxMissions.toFloat()).coerceIn(0f, 1f)
                        val quotaY = chartHeight - quotaRatio * chartHeight
                        drawLine(
                            color = HelionHazardOrange,
                            start = Offset(leftMargin, quotaY),
                            end = Offset(w, quotaY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }

                    // Draw Bars for each week
                    val barCount = records.size
                    val slotWidth = chartWidth / barCount
                    val barWidth = slotWidth * 0.55f

                    records.forEachIndexed { index, record ->
                        val centerX = leftMargin + (index * slotWidth) + (slotWidth / 2f)
                        val barLeft = centerX - (barWidth / 2f)
                        val isSelected = index == selectedIndex

                        when (chartMode) {
                            MissionChartMode.TOTAL -> {
                                val valueRatio = (record.completedCount.toFloat() / maxMissions.toFloat()).coerceIn(0f, 1f)
                                val barHeight = valueRatio * chartHeight
                                val barTop = chartHeight - barHeight

                                // Bar gradient
                                val barBrush = Brush.verticalGradient(
                                    colors = if (isSelected) {
                                        listOf(HelionCyanGlow, HelionCyan, HelionCyan.copy(alpha = 0.7f))
                                    } else if (record.isCurrentWeek) {
                                        listOf(HelionHighSecGreen, HelionHighSecGreen.copy(alpha = 0.6f))
                                    } else {
                                        listOf(HelionShieldBlue, HelionShieldBlue.copy(alpha = 0.5f))
                                    }
                                )

                                // Draw bar with rounded top corners
                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                // Highlight border for selected
                                if (isSelected) {
                                    drawRoundRect(
                                        color = Color.White,
                                        topLeft = Offset(barLeft - 1.5f, barTop - 1.5f),
                                        size = Size(barWidth + 3f, barHeight + 3f),
                                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                        style = Stroke(width = 2f)
                                    )
                                }
                            }

                            MissionChartMode.BY_CATEGORY -> {
                                // Stacked segments
                                val categories = listOf(
                                    Pair(record.combatCount, HelionDangerRed),
                                    Pair(record.miningCount, HelionAmber),
                                    Pair(record.reconCount, HelionCyan),
                                    Pair(record.escortCount, HelionShieldBlue),
                                    Pair(record.blackOpsCount, HelionNullSecPurple)
                                )

                                var currentY = chartHeight
                                categories.forEach { (count, color) ->
                                    if (count > 0) {
                                        val segmentHeight = (count.toFloat() / maxMissions.toFloat()) * chartHeight
                                        val segTop = currentY - segmentHeight

                                        drawRoundRect(
                                            color = color,
                                            topLeft = Offset(barLeft, segTop),
                                            size = Size(barWidth, segmentHeight),
                                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                        )
                                        currentY = segTop
                                    }
                                }

                                if (isSelected) {
                                    val totalRatio = (record.completedCount.toFloat() / maxMissions.toFloat()).coerceIn(0f, 1f)
                                    val totalHeight = totalRatio * chartHeight
                                    val totalTop = chartHeight - totalHeight
                                    drawRoundRect(
                                        color = Color.White,
                                        topLeft = Offset(barLeft - 1.5f, totalTop - 1.5f),
                                        size = Size(barWidth + 3f, totalHeight + 3f),
                                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                        style = Stroke(width = 2f)
                                    )
                                }
                            }

                            MissionChartMode.CREDITS_EARNED -> {
                                val creditsK = (record.creditsEarned / 1000).toFloat()
                                val valueRatio = (creditsK / maxCreditsThousands.toFloat()).coerceIn(0f, 1f)
                                val barHeight = valueRatio * chartHeight
                                val barTop = chartHeight - barHeight

                                val barBrush = Brush.verticalGradient(
                                    colors = if (isSelected) {
                                        listOf(HelionAmber, HelionHazardOrange)
                                    } else {
                                        listOf(HelionAmber.copy(alpha = 0.8f), HelionAmber.copy(alpha = 0.4f))
                                    }
                                )

                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                if (isSelected) {
                                    drawRoundRect(
                                        color = Color.White,
                                        topLeft = Offset(barLeft - 1.5f, barTop - 1.5f),
                                        size = Size(barWidth + 3f, barHeight + 3f),
                                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                        style = Stroke(width = 2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // X-Axis Week Labels
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                records.forEachIndexed { index, record ->
                    val isSelected = index == selectedIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelectIndex(index) }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = record.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) HelionCyan else if (record.isCurrentWeek) HelionHighSecGreen else HelionTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (chartMode == MissionChartMode.CREDITS_EARNED) {
                                "${record.creditsEarned / 1000}k"
                            } else {
                                "${record.completedCount}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else HelionTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HelionTextSecondary,
            fontSize = 10.sp
        )
    }
}

// =============================================================================
// SELECTED WEEK DETAIL BREAKDOWN CARD
// =============================================================================
@Composable
fun SelectedWeekDetailCard(
    record: WeeklyMissionRecord,
    numberFormat: NumberFormat
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurfaceVariant),
        border = BorderStroke(1.dp, HelionCyan.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_week_detail_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = HelionCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${record.weekLabel.uppercase()} TELEMETRY DEBRIEF",
                            style = MaterialTheme.typography.titleSmall,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = record.dateRangeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = HelionCyan.copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, HelionCyan)
                ) {
                    Text(
                        text = "${record.completedCount} COMPLETED",
                        color = HelionCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HelionBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "OPERATIONAL CATEGORY BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextMuted,
                letterSpacing = 0.5.sp,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Category Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryCountBadge(label = "Combat", count = record.combatCount, color = HelionDangerRed, modifier = Modifier.weight(1f))
                CategoryCountBadge(label = "Mining", count = record.miningCount, color = HelionAmber, modifier = Modifier.weight(1f))
                CategoryCountBadge(label = "Recon", count = record.reconCount, color = HelionCyan, modifier = Modifier.weight(1f))
                CategoryCountBadge(label = "Escort", count = record.escortCount, color = HelionShieldBlue, modifier = Modifier.weight(1f))
                CategoryCountBadge(label = "Black Ops", count = record.blackOpsCount, color = HelionNullSecPurple, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Financial & Fleet Telemetry summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("GSC Payout", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 10.sp)
                    Text(
                        text = "${numberFormat.format(record.creditsEarned)} GSC",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HelionAmber
                    )
                }
                Column {
                    Text("Faction Standing Gained", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 10.sp)
                    Text(
                        text = "+${record.standingEarned} REP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HelionHighSecGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Top Operating Craft", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted, fontSize = 10.sp)
                    Text(
                        text = record.topShip,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HelionTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCountBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.7.dp, color.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 14.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary,
                fontSize = 9.sp
            )
        }
    }
}

// =============================================================================
// CATEGORY PERFORMANCE DISTRIBUTION CARD
// =============================================================================
@Composable
fun CategoryPerformanceBreakdownCard(records: List<WeeklyMissionRecord>) {
    val totalCombat = records.sumOf { it.combatCount }
    val totalMining = records.sumOf { it.miningCount }
    val totalRecon = records.sumOf { it.reconCount }
    val totalEscort = records.sumOf { it.escortCount }
    val totalBlackOps = records.sumOf { it.blackOpsCount }
    val totalAll = (totalCombat + totalMining + totalRecon + totalEscort + totalBlackOps).coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "OPERATIONAL FOCUS BY MISSION CLASS",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextMuted,
                letterSpacing = 0.8.sp,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            CategoryProgressBar(name = "Combat Interdiction", count = totalCombat, total = totalAll, color = HelionDangerRed)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryProgressBar(name = "Resource Extraction", count = totalMining, total = totalAll, color = HelionAmber)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryProgressBar(name = "Deep Recon & Survey", count = totalRecon, total = totalAll, color = HelionCyan)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryProgressBar(name = "High-Value Escort", count = totalEscort, total = totalAll, color = HelionShieldBlue)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryProgressBar(name = "Covert Black Ops", count = totalBlackOps, total = totalAll, color = HelionNullSecPurple)
        }
    }
}

@Composable
fun CategoryProgressBar(
    name: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val percentage = (fraction * 100).toInt()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodySmall, color = HelionTextPrimary, fontSize = 11.sp)
            Text(text = "$count ops ($percentage%)", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(HelionDeepGraphite, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

// =============================================================================
// FLEET SORTIES ROSTER CARD
// =============================================================================
@Composable
fun FleetSortiesRosterCard(records: List<WeeklyMissionRecord>) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelionSurface),
        border = BorderStroke(1.dp, HelionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP PERFORMING FLEET SORTIE CRAFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextMuted,
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = HelionCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val fleetEntries = listOf(
                Triple("Aegis Invictus", "Vindicator Heavy Cruiser", "19 Ops • 100% Success"),
                Triple("Aster Raptor", "Strike Interceptor", "16 Ops • 96% Success"),
                Triple("Vanguard Eclipse", "Phantasm Stealth Frigate", "8 Ops • 98% Success"),
                Triple("Kallisto Hauler", "Bulk Heavy Transport", "5 Ops • 100% Success")
            )

            fleetEntries.forEachIndexed { index, (shipName, shipClass, recordText) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HelionCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${index + 1}",
                                    color = HelionCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = shipName, style = MaterialTheme.typography.bodyMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = shipClass, style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary, fontSize = 10.sp)
                        }
                    }

                    Text(
                        text = recordText,
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionHighSecGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
                if (index < fleetEntries.lastIndex) {
                    HorizontalDivider(color = HelionBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

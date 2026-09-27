package com.example.helion.ui.market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.MarketItem
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MarketCommodityCard(
    item: MarketItem,
    isSelected: Boolean,
    isWatched: Boolean,
    regionalPrices: List<MarketItem>,
    onSelect: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onOpenTrade: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    var isExpanded by remember { mutableStateOf(false) }

    // Regional price calculations
    val otherMarkets = regionalPrices.filter { it.stationId != item.stationId }
    val maxRegionalPrice = otherMarkets.maxOfOrNull { it.buyPrice }
    val minRegionalPrice = otherMarkets.minOfOrNull { it.buyPrice }
    val bestExportMarket = otherMarkets.maxByOrNull { it.buyPrice }
    val maxProfitMargin = if (bestExportMarket != null) bestExportMarket.buyPrice - item.buyPrice else 0L

    // Trend percentage calculation
    val percentChange = if (item.buyPrice > 0) {
        (item.priceTrendDelta.toFloat() / item.buyPrice.toFloat()) * 100f
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
                isExpanded = !isExpanded
            }
            .testTag("commodity_card_${item.commodityId}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HelionSurfaceVariant else HelionSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.2.dp else 0.8.dp,
            color = if (isSelected) HelionAmber else HelionBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // TOP BAR: Commodity Name, Category Badge, Watchlist Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onToggleWatchlist,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("watchlist_button_${item.commodityId}")
                        ) {
                            Icon(
                                imageVector = if (isWatched) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isWatched) HelionAmber else HelionTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CutCornerShape(topStart = 3.dp, bottomEnd = 3.dp),
                        color = HelionSurfaceHigh
                    ) {
                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // RIGHT SIDE: Price & Trend Indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${numberFormat.format(item.buyPrice)} GSC",
                        style = MaterialTheme.typography.headlineSmall,
                        color = HelionAmber,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // SIMPLE TREND INDICATOR PILL
                    MarketTrendBadge(
                        delta = item.priceTrendDelta,
                        percentChange = percentChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LOCAL STOCK & REGIONAL SPREAD SUMMARY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LOCAL STATION INVENTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextMuted
                    )
                    Text(
                        text = "Stock: ${item.stockUnits} ${item.unit} • Demand: ${item.demandUnits} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Local Sell Offer: ${numberFormat.format(item.sellPrice)} GSC",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextSecondary
                    )
                }

                if (bestExportMarket != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (maxProfitMargin > 0) HelionHighSecGreen.copy(alpha = 0.12f) else HelionSurfaceHigh,
                        border = BorderStroke(
                            0.8.dp,
                            if (maxProfitMargin > 0) HelionHighSecGreen else HelionBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "REGIONAL SPREAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (maxProfitMargin > 0) HelionHighSecGreen else HelionTextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (maxProfitMargin > 0) "+${numberFormat.format(maxProfitMargin)} GSC/ton" else "Regional Parity",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (maxProfitMargin > 0) HelionHighSecGreen else HelionTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // EXPANDABLE REGIONAL PRICE BREAKDOWN
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CURRENT REGIONAL STATIONS COMPARISON",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionCyan,
                        fontWeight = FontWeight.Bold
                    )

                    // Current local station
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HelionDeepGraphite,
                        border = BorderStroke(0.6.dp, HelionCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(HelionCyan))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "${item.stationName} (Current)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${item.systemName} System",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionTextMuted
                                    )
                                }
                            }
                            Text(
                                text = "${numberFormat.format(item.buyPrice)} GSC",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HelionAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Other regional stations
                    otherMarkets.forEach { other ->
                        val diff = other.buyPrice - item.buyPrice
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite,
                            border = BorderStroke(0.6.dp, HelionBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = HelionTextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = other.stationName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HelionTextPrimary
                                        )
                                        Text(
                                            text = "${other.systemName} System • Stock: ${other.stockUnits} ${other.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HelionTextMuted
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${numberFormat.format(other.buyPrice)} GSC",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (diff > 0) "+${numberFormat.format(diff)} GSC Margin" else "${numberFormat.format(diff)} GSC",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (diff > 0) HelionHighSecGreen else HelionTextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACTION CONTROLS: BUY & SELL ORDER BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onOpenTrade(false) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sell_button_${item.commodityId}"),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionHighSecGreen)
                ) {
                    Text(
                        text = "Sell at Station",
                        color = HelionHighSecGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onOpenTrade(true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("buy_button_${item.commodityId}"),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HelionAmber,
                        contentColor = HelionDeepGraphite
                    )
                ) {
                    Text(
                        text = "Buy Commodity",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MarketTrendBadge(
    delta: Int,
    percentChange: Float,
    modifier: Modifier = Modifier
) {
    val isPositive = delta > 0
    val isNeutral = delta == 0
    val badgeColor = when {
        isPositive -> HelionHighSecGreen
        isNeutral -> HelionTextMuted
        else -> HelionDangerRed
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = badgeColor.copy(alpha = 0.16f),
        border = BorderStroke(0.8.dp, badgeColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isPositive -> Icons.Default.ArrowUpward
                    isNeutral -> Icons.Default.HorizontalRule
                    else -> Icons.Default.ArrowDownward
                },
                contentDescription = "Price Trend",
                tint = badgeColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = when {
                    isPositive -> "+$delta GSC (+${String.format("%.1f", percentChange)}%)"
                    isNeutral -> "0 GSC (Stable)"
                    else -> "$delta GSC (${String.format("%.1f", percentChange)}%)"
                },
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

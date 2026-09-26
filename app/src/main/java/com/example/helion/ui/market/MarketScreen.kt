package com.example.helion.ui.market

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.MarketItem
import com.example.helion.ui.components.HelionCard
import com.example.helion.ui.components.SecurityBadge
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
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
fun MarketScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("LOCAL MARKET", "PRICE ARBITRAGE", "WATCHLIST", "TRADE CALCULATOR")

    var tradeDialogOpen by remember { mutableStateOf(false) }
    var tradeIsBuy by remember { mutableStateOf(true) }
    var tradeQuantity by remember { mutableIntStateOf(1) }

    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().testTag("market_screen")) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = HelionDeepGraphite,
            contentColor = HelionAmber,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = HelionAmber,
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
                            color = if (selectedTab == index) HelionAmber else HelionTextSecondary
                        )
                    }
                )
            }
        }

        // Offline Notification banner
        if (state.isOffline) {
            Surface(color = HelionDangerRed.copy(alpha = 0.25f), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = HelionDangerRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "CLIENT OFFLINE: Market prices are cached snapshots. Live BUY/SELL orders are disabled.", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        if (state.errorMessage != null) {
            Surface(color = HelionDangerRed.copy(alpha = 0.2f), border = BorderStroke(1.dp, HelionDangerRed), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(text = state.errorMessage ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.padding(10.dp))
            }
        }

        when (selectedTab) {
            0 -> LocalMarketContent(
                state = state,
                onSelectCommodity = {
                    viewModel.selectCommodity(it)
                },
                onOpenTrade = { item, isBuy ->
                    viewModel.selectCommodity(item)
                    tradeIsBuy = isBuy
                    tradeQuantity = 1
                    tradeDialogOpen = true
                },
                onToggleWatchlist = { viewModel.toggleWatchlist(it) }
            )
            1 -> PriceArbitrageContent(
                state = state,
                onSelectCommodity = { viewModel.selectCommodity(it) }
            )
            2 -> MarketWatchlistContent(
                state = state,
                onSelectWatched = { watched ->
                    val found = state.regionalCommodities.find { it.commodityId == watched.commodityId }
                    if (found != null) {
                        viewModel.selectCommodity(found)
                        selectedTab = 1
                    }
                }
            )
            3 -> TradeCalculatorContent(state = state)
        }
    }

    // Trade Dialog (Buy / Sell) with Server-Authoritative Execution
    if (tradeDialogOpen && state.selectedCommodity != null) {
        val item = state.selectedCommodity!!
        val maxUnits = if (tradeIsBuy) item.stockUnits.coerceAtMost(64) else 16
        val unitPrice = if (tradeIsBuy) item.buyPrice else item.sellPrice
        val totalCost = unitPrice * tradeQuantity

        AlertDialog(
            onDismissRequest = { tradeDialogOpen = false },
            title = {
                Text(
                    text = if (tradeIsBuy) "BUY ${item.displayName.uppercase()}" else "SELL ${item.displayName.uppercase()}",
                    color = HelionTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Station: ${item.stationName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionCyan
                    )
                    Text(
                        text = "Category: ${item.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "QUANTITY (${item.unit.uppercase()}):", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(text = "$tradeQuantity ${item.unit}", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = tradeQuantity.toFloat(),
                        onValueChange = { tradeQuantity = it.toInt().coerceAtLeast(1) },
                        valueRange = 1f..maxUnits.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = if (tradeIsBuy) HelionAmber else HelionHighSecGreen,
                            activeTrackColor = if (tradeIsBuy) HelionAmber else HelionHighSecGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TOTAL ORDER AMOUNT:", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                        Text(
                            text = "${numberFormat.format(totalCost)} CR",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (tradeIsBuy) HelionAmber else HelionHighSecGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: Transaction commits authoritatively on the HELION Universe Server.",
                        style = MaterialTheme.typography.labelSmall,
                        color = HelionTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeTransaction(tradeQuantity, tradeIsBuy)
                        tradeDialogOpen = false
                    },
                    enabled = !state.isExecutingTransaction && !state.isOffline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tradeIsBuy) HelionAmber else HelionHighSecGreen,
                        contentColor = HelionDeepGraphite
                    )
                ) {
                    if (state.isExecutingTransaction) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HelionDeepGraphite)
                    } else {
                        Text(if (tradeIsBuy) "Confirm Buy Order" else "Confirm Sell Order", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { tradeDialogOpen = false }) {
                    Text("Cancel", color = HelionTextSecondary)
                }
            },
            containerColor = HelionSurface
        )
    }

    // Authoritative Transaction Success Dialog
    if (state.transactionResult != null) {
        val tx = state.transactionResult!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissTransactionDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = HelionHighSecGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Transaction Committed", color = HelionTextPrimary)
                }
            },
            text = {
                Column {
                    Text(text = "SERVER TX ID: ${tx.transactionId}", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Authoritative Balance: ${numberFormat.format(tx.authoritativeCredits)} CR", style = MaterialTheme.typography.bodyMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                    Text(text = "Authoritative Ship Cargo: ${tx.authoritativeCargoUnits} tons", style = MaterialTheme.typography.bodySmall, color = HelionTextPrimary)
                    Text(text = "Updated Station Stock: ${tx.authoritativeStock} units", style = MaterialTheme.typography.bodySmall, color = HelionTextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissTransactionDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = HelionCyan, contentColor = HelionDeepGraphite)
                ) {
                    Text("Done")
                }
            },
            containerColor = HelionSurface
        )
    }
}

// =============================================================================
// 1. LOCAL MARKET CONTENT
// =============================================================================
@Composable
fun LocalMarketContent(
    state: MarketUiState,
    onSelectCommodity: (MarketItem) -> Unit,
    onOpenTrade: (MarketItem, Boolean) -> Unit,
    onToggleWatchlist: (MarketItem) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "LOCAL COMMODITIES (KEPLER PRIME ORBITAL)",
                style = MaterialTheme.typography.labelMedium,
                color = HelionTextSecondary,
                letterSpacing = 1.sp
            )
        }

        items(state.localCommodities) { item ->
            val isSelected = state.selectedCommodity?.commodityId == item.commodityId
            val isWatched = state.watchlist.any { it.commodityId == item.commodityId }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCommodity(item) },
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) HelionSurfaceVariant else HelionSurface),
                border = BorderStroke(1.dp, if (isSelected) HelionAmber else HelionBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.displayName, style = MaterialTheme.typography.titleLarge, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { onToggleWatchlist(item) }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = if (isWatched) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Watchlist",
                                        tint = if (isWatched) HelionAmber else HelionTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(text = item.category.displayName, style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${numberFormat.format(item.buyPrice)} CR",
                                style = MaterialTheme.typography.titleLarge,
                                color = HelionAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (item.priceTrendDelta >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (item.priceTrendDelta >= 0) HelionHighSecGreen else HelionDangerRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${if (item.priceTrendDelta >= 0) "+" else ""}${item.priceTrendDelta} CR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.priceTrendDelta >= 0) HelionHighSecGreen else HelionDangerRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stock: ${item.stockUnits} ${item.unit} • Sell Price: ${numberFormat.format(item.sellPrice)} CR",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onOpenTrade(item, false) },
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, HelionHighSecGreen)
                            ) {
                                Text("Sell", color = HelionHighSecGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onOpenTrade(item, true) },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite)
                            ) {
                                Text("Buy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 2. REGIONAL PRICE COMPARISON (ARBITRAGE)
// =============================================================================
@Composable
fun PriceArbitrageContent(
    state: MarketUiState,
    onSelectCommodity: (MarketItem) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val selected = state.selectedCommodity ?: state.localCommodities.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HelionCard(
                title = "Regional Trade Arbitrage",
                badgeText = selected?.displayName ?: "Select Item",
                accentColor = HelionCyan
            ) {
                Text(
                    text = "Comparing buy price at Kepler (${selected?.buyPrice ?: 0} CR) against all known regional station sell rates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HelionTextSecondary
                )
            }
        }

        item {
            Text(
                text = "REGIONAL STATION RATES FOR ${selected?.displayName?.uppercase() ?: ""}:",
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary
            )
        }

        items(state.priceComparisons) { comp ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = HelionSurface,
                border = BorderStroke(1.dp, HelionBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = comp.stationName, style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "System: ${comp.systemName} (${comp.jumpsFromCurrent} jumps)", style = MaterialTheme.typography.bodySmall, color = HelionCyan)
                        }
                        SecurityBadge(rating = comp.securityRating)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "STATION BUY RATE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${numberFormat.format(comp.sellPrice)} CR", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "MARGIN / TON", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(
                                text = "${if (comp.estimatedGrossMarginPerTon >= 0) "+" else ""}${comp.estimatedGrossMarginPerTon} CR",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (comp.estimatedGrossMarginPerTon >= 0) HelionHighSecGreen else HelionDangerRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PROFIT (16t)", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(
                                text = "${numberFormat.format(comp.estimatedCargoProfit)} CR",
                                style = MaterialTheme.typography.titleMedium,
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

// =============================================================================
// 3. WATCHLIST TAB
// =============================================================================
@Composable
fun MarketWatchlistContent(
    state: MarketUiState,
    onSelectWatched: (com.example.helion.core.database.MarketWatchlistEntity) -> Unit
) {
    if (state.watchlist.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = HelionTextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "No commodities on market watchlist.", style = MaterialTheme.typography.titleMedium, color = HelionTextSecondary)
                Text(text = "Tap the bookmark icon on any commodity in the local market to watch its price spread.", style = MaterialTheme.typography.bodySmall, color = HelionTextMuted)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(state.watchlist) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWatched(item) },
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = item.displayName, style = MaterialTheme.typography.titleLarge, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = item.categoryName, style = MaterialTheme.typography.bodySmall, color = HelionCyan)
                    }
                    Text(text = "Target Buy: ${item.targetBuyPrice} CR", style = MaterialTheme.typography.labelMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// 4. TRADE CALCULATOR
// =============================================================================
@Composable
fun TradeCalculatorContent(
    state: MarketUiState
) {
    var cargoCapacity by remember { mutableIntStateOf(16) }
    var selectedCommodityIndex by remember { mutableIntStateOf(0) }
    val commodities = state.localCommodities
    val selectedItem = commodities.getOrNull(selectedCommodityIndex) ?: commodities.firstOrNull()

    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HelionCard(title = "Trade Profitability Simulator", accentColor = HelionAmber) {
                Text(text = "CARGO CAPACITY: $cargoCapacity TONS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                Slider(
                    value = cargoCapacity.toFloat(),
                    onValueChange = { cargoCapacity = it.toInt() },
                    valueRange = 8f..128f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = HelionAmber, activeTrackColor = HelionAmber)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedItem != null) {
                    val purchaseTotal = selectedItem.buyPrice * cargoCapacity
                    val bestComparison = state.priceComparisons.firstOrNull()
                    val targetSellPrice = bestComparison?.sellPrice ?: (selectedItem.buyPrice + 143L)
                    val projectedRevenue = targetSellPrice * cargoCapacity
                    val netProfit = projectedRevenue - purchaseTotal

                    Text(
                        text = "SIMULATED COMMODITY: ${selectedItem.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = HelionCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Origin: Kepler Prime Orbital ➔ Destination: Crossroads Central Citadel",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "CAPITAL REQUIRED", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${numberFormat.format(purchaseTotal)} CR", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PROJECTED PROFIT", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${numberFormat.format(netProfit)} CR", style = MaterialTheme.typography.headlineSmall, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

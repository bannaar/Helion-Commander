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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.helion.core.database.MarketPriceAlertEntity
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
    val tabTitles = listOf("LOCAL MARKET", "PRICE ARBITRAGE", "PRICE ALERTS", "WATCHLIST", "TRADE CALCULATOR")

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
                val activeAlertCount = state.priceAlerts.count { it.isActive }
                val displayTitle = if (index == 2 && activeAlertCount > 0) "$title ($activeAlertCount)" else title
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) HelionAmber else HelionTextSecondary
                        )
                    }
                )
            }
        }

        // Action Status Banner (Success / Notifications Triggered)
        if (state.actionSuccessBanner != null) {
            Surface(
                color = HelionHighSecGreen.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, HelionHighSecGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("market_action_banner")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = HelionHighSecGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = state.actionSuccessBanner ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                    IconButton(onClick = { viewModel.clearBanner() }, modifier = Modifier.size(20.dp)) {
                        Text("✕", color = HelionHighSecGreen, fontSize = 12.sp)
                    }
                }
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
                onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                onOpenSetAlert = { viewModel.openConfiguringAlert(it) }
            )
            1 -> PriceArbitrageContent(
                state = state,
                onSelectCommodity = { viewModel.selectCommodity(it) },
                onOpenSetAlert = { viewModel.openConfiguringAlert(it) }
            )
            2 -> PriceAlertsContent(
                state = state,
                onToggleAlertActive = { viewModel.toggleAlertActive(it) },
                onDeleteAlert = { viewModel.deletePriceAlert(it.alertId) },
                onSendTestNotification = { viewModel.sendTestAlertNotification(it) },
                onSimulatePriceHit = { viewModel.simulatePriceThresholdHit(it) },
                onSelectCommodity = { alert ->
                    val found = state.localCommodities.find { it.commodityId == alert.commodityId }
                        ?: state.regionalCommodities.find { it.commodityId == alert.commodityId }
                    if (found != null) {
                        viewModel.selectCommodity(found)
                        selectedTab = 0
                    }
                },
                onOpenConfigureAlert = { item ->
                    viewModel.openConfiguringAlert(item)
                }
            )
            3 -> MarketWatchlistContent(
                state = state,
                onSelectWatched = { watched ->
                    val found = state.regionalCommodities.find { it.commodityId == watched.commodityId }
                    if (found != null) {
                        viewModel.selectCommodity(found)
                        selectedTab = 1
                    }
                }
            )
            4 -> TradeCalculatorContent(state = state)
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
                            text = "${numberFormat.format(totalCost)} GSC",
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
                    Text(text = "Authoritative Balance: ${numberFormat.format(tx.authoritativeCredits)} GSC", style = MaterialTheme.typography.bodyMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
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

    // Set Price Threshold Alert Dialog
    if (state.configuringAlertCommodity != null) {
        val commodity = state.configuringAlertCommodity!!
        SetPriceThresholdDialog(
            commodity = commodity,
            onDismiss = { viewModel.dismissConfiguringAlert() },
            onSaveAlert = { targetPrice, isBuy, condition, stationId, stationName ->
                viewModel.savePriceAlert(
                    commodity = commodity,
                    targetPrice = targetPrice,
                    isBuyPrice = isBuy,
                    conditionType = condition,
                    stationId = stationId,
                    stationName = stationName
                )
            },
            onTestNotification = { targetPrice, isBuy, condition ->
                val testAlert = MarketPriceAlertEntity(
                    alertId = "test-${commodity.commodityId}",
                    commodityId = commodity.commodityId,
                    displayName = commodity.displayName,
                    categoryName = commodity.category.displayName,
                    targetPrice = targetPrice,
                    isBuyPrice = isBuy,
                    conditionType = condition,
                    stationId = commodity.stationId,
                    stationName = commodity.stationName
                )
                viewModel.sendTestAlertNotification(testAlert)
            }
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
    onToggleWatchlist: (MarketItem) -> Unit,
    onOpenSetAlert: (MarketItem) -> Unit = {}
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
            val existingAlert = state.priceAlerts.find { it.commodityId == item.commodityId }

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
                        Column(modifier = Modifier.weight(1f)) {
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
                                IconButton(
                                    onClick = { onOpenSetAlert(item) },
                                    modifier = Modifier.size(24.dp).testTag("alert_button_${item.commodityId}")
                                ) {
                                    Icon(
                                        imageVector = if (existingAlert != null && existingAlert.isActive) Icons.Default.NotificationsActive else Icons.Default.AddAlert,
                                        contentDescription = "Set Price Alert",
                                        tint = if (existingAlert != null && existingAlert.isActive) HelionAmber else HelionTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(text = item.category.displayName, style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)

                            if (existingAlert != null && existingAlert.isActive) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val condSymbol = if (existingAlert.conditionType == "AT_OR_BELOW" || existingAlert.conditionType == "<=") "≤" else "≥"
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = if (existingAlert.isTriggered) HelionHighSecGreen.copy(alpha = 0.2f) else HelionAmber.copy(alpha = 0.15f),
                                    border = BorderStroke(0.8.dp, if (existingAlert.isTriggered) HelionHighSecGreen else HelionAmber)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (existingAlert.isTriggered) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = if (existingAlert.isTriggered) HelionHighSecGreen else HelionAmber,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${if (existingAlert.isTriggered) "TARGET HIT" else "ALERT ARMED"}: ${if (existingAlert.isBuyPrice) "Buy" else "Sell"} $condSymbol ${existingAlert.targetPrice} GSC",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (existingAlert.isTriggered) HelionHighSecGreen else HelionAmber,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${numberFormat.format(item.buyPrice)} GSC",
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
                                    text = "${if (item.priceTrendDelta >= 0) "+" else ""}${item.priceTrendDelta} GSC",
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
                            text = "Stock: ${item.stockUnits} ${item.unit} • Sell: ${numberFormat.format(item.sellPrice)} GSC",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onOpenSetAlert(item) },
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, HelionAmber.copy(alpha = 0.6f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AddAlert, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Alert", color = HelionAmber, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onOpenTrade(item, false) },
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, HelionHighSecGreen),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Sell", color = HelionHighSecGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onOpenTrade(item, true) },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Buy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
    onSelectCommodity: (MarketItem) -> Unit,
    onOpenSetAlert: (MarketItem) -> Unit = {}
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
                Column {
                    Text(
                        text = "Comparing buy price at Kepler (${selected?.buyPrice ?: 0} GSC) against all known regional station sell rates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary
                    )
                    if (selected != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MONITORING RATE: ${selected.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HelionAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { onOpenSetAlert(selected) },
                                colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Arm Price Alert", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
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
                            Text(text = "${numberFormat.format(comp.sellPrice)} GSC", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "MARGIN / TON", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(
                                text = "${if (comp.estimatedGrossMarginPerTon >= 0) "+" else ""}${comp.estimatedGrossMarginPerTon} GSC",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (comp.estimatedGrossMarginPerTon >= 0) HelionHighSecGreen else HelionDangerRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PROFIT (16t)", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(
                                text = "${numberFormat.format(comp.estimatedCargoProfit)} GSC",
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
                    Text(text = "Target Buy: ${item.targetBuyPrice} GSC", style = MaterialTheme.typography.labelMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
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
                            Text(text = "${numberFormat.format(purchaseTotal)} GSC", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PROJECTED PROFIT", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${numberFormat.format(netProfit)} GSC", style = MaterialTheme.typography.headlineSmall, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 5. PRICE ALERTS TAB & THRESHOLD SURVEILLANCE
// =============================================================================
@Composable
fun PriceAlertsContent(
    state: MarketUiState,
    onToggleAlertActive: (MarketPriceAlertEntity) -> Unit,
    onDeleteAlert: (MarketPriceAlertEntity) -> Unit,
    onSendTestNotification: (MarketPriceAlertEntity) -> Unit,
    onSimulatePriceHit: (MarketPriceAlertEntity) -> Unit,
    onSelectCommodity: (MarketPriceAlertEntity) -> Unit,
    onOpenConfigureAlert: (MarketItem) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val activeCount = state.priceAlerts.count { it.isActive }
    val triggeredCount = state.priceAlerts.count { it.isTriggered }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("price_alerts_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Telemetry header
        item {
            HelionCard(
                title = "Price Threshold Telemetry Grid",
                badgeText = "$activeCount Armed Alerts",
                accentColor = HelionAmber
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Radar, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AUTOMATED COMMODITY PRICE SURVEILLANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = HelionAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "When market prices hit or breach your designated buy or sell thresholds, local notifications will be dispatched instantly to your terminal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("ARMED ALERTS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                Text("$activeCount", style = MaterialTheme.typography.titleMedium, color = HelionCyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("TARGETS HIT", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                Text("$triggeredCount", style = MaterialTheme.typography.titleMedium, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("TOTAL RULES", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                Text("${state.priceAlerts.size}", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (state.priceAlerts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HelionSurface,
                    border = BorderStroke(1.dp, HelionBorder),
                    modifier = Modifier.fillMaxWidth().testTag("empty_price_alerts_card")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(42.dp))
                        Text(
                            text = "No Price Threshold Alerts Armed",
                            style = MaterialTheme.typography.titleMedium,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Set price alerts to catch commodity market dips and surges. Select any commodity below to arm a threshold alert:",
                            style = MaterialTheme.typography.bodySmall,
                            color = HelionTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.localCommodities.take(3).forEach { item ->
                                OutlinedButton(
                                    onClick = { onOpenConfigureAlert(item) },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, HelionCyan),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.AddAlert, contentDescription = null, tint = HelionCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Set Alert for ${item.displayName} (Current: ${item.buyPrice} GSC)", color = HelionCyan, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "ACTIVE & CONFIGURED ALERTS (${state.priceAlerts.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = HelionTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            items(state.priceAlerts, key = { it.alertId }) { alert ->
                val matchingItem = state.localCommodities.find { it.commodityId == alert.commodityId }
                    ?: state.regionalCommodities.find { it.commodityId == alert.commodityId }
                val currentPrice = if (matchingItem != null) {
                    if (alert.isBuyPrice) matchingItem.buyPrice else matchingItem.sellPrice
                } else alert.lastCheckedPrice ?: alert.targetPrice

                val priceDiff = currentPrice - alert.targetPrice
                val conditionLabel = if (alert.conditionType == "AT_OR_BELOW" || alert.conditionType == "<=") "≤" else "≥"

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = HelionSurface),
                    border = BorderStroke(
                        if (alert.isTriggered) 1.5.dp else 1.dp,
                        if (alert.isTriggered) HelionHighSecGreen else if (alert.isActive) HelionAmber else HelionBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("price_alert_card_${alert.alertId}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = alert.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = HelionTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = (if (alert.isBuyPrice) HelionAmber else HelionHighSecGreen).copy(alpha = 0.2f),
                                        border = BorderStroke(0.6.dp, if (alert.isBuyPrice) HelionAmber else HelionHighSecGreen)
                                    ) {
                                        Text(
                                            text = if (alert.isBuyPrice) "BUY RATE" else "SELL RATE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = if (alert.isBuyPrice) HelionAmber else HelionHighSecGreen,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${alert.categoryName} • ${alert.stationName ?: "All Regional Stations"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HelionTextSecondary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = alert.isActive,
                                    onCheckedChange = { onToggleAlertActive(alert) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HelionAmber,
                                        checkedTrackColor = HelionAmber.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.testTag("toggle_alert_${alert.alertId}")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onDeleteAlert(alert) },
                                    modifier = Modifier.size(28.dp).testTag("delete_alert_${alert.alertId}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Alert", tint = HelionTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Target vs Current Stats Row
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HelionDeepGraphite,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TARGET THRESHOLD", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                    Text("$conditionLabel ${numberFormat.format(alert.targetPrice)} GSC", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CURRENT PRICE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                    Text("${numberFormat.format(currentPrice)} GSC", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("STATUS", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                                    Text(
                                        text = if (alert.isTriggered) "TARGET HIT" else if (alert.isActive) "${kotlin.math.abs(priceDiff)} GSC away" else "PAUSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (alert.isTriggered) HelionHighSecGreen else if (alert.isActive) HelionCyan else HelionTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (alert.isTriggered) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = HelionHighSecGreen.copy(alpha = 0.15f),
                                border = BorderStroke(0.8.dp, HelionHighSecGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HelionHighSecGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Target breached at ${numberFormat.format(currentPrice)} GSC! Device notification triggered.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HelionHighSecGreen,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Simulation & Testing Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSimulatePriceHit(alert) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HelionAmber,
                                    contentColor = HelionDeepGraphite
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1.3f).testTag("simulate_hit_${alert.alertId}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Price Hit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onSendTestNotification(alert) },
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, HelionCyan),
                                modifier = Modifier.weight(1f).testTag("test_notif_${alert.alertId}")
                            ) {
                                Text("Test Alert", color = HelionCyan, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 6. SET PRICE THRESHOLD DIALOG
// =============================================================================
@Composable
fun SetPriceThresholdDialog(
    commodity: MarketItem,
    onDismiss: () -> Unit,
    onSaveAlert: (targetPrice: Long, isBuyPrice: Boolean, conditionType: String, stationId: String?, stationName: String?) -> Unit,
    onTestNotification: (targetPrice: Long, isBuyPrice: Boolean, conditionType: String) -> Unit
) {
    var isBuyPrice by remember { mutableStateOf(true) }
    var conditionType by remember { mutableStateOf("AT_OR_BELOW") } // "AT_OR_BELOW" or "AT_OR_ABOVE"
    var targetPriceText by remember { mutableStateOf((if (isBuyPrice) commodity.buyPrice else commodity.sellPrice).toString()) }
    var scopeStationOnly by remember { mutableStateOf(false) }

    val currentPrice = if (isBuyPrice) commodity.buyPrice else commodity.sellPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddAlert, contentDescription = null, tint = HelionAmber, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "SET PRICE THRESHOLD ALERT", style = MaterialTheme.typography.titleMedium, color = HelionTextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "${commodity.displayName} • ${commodity.category.displayName}", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Current Market Info
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HelionDeepGraphite,
                    border = BorderStroke(1.dp, HelionBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "CURRENT BUY PRICE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${commodity.buyPrice} GSC", style = MaterialTheme.typography.titleMedium, color = HelionAmber, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "CURRENT SELL RATE", style = MaterialTheme.typography.labelSmall, color = HelionTextMuted)
                            Text(text = "${commodity.sellPrice} GSC", style = MaterialTheme.typography.titleMedium, color = HelionHighSecGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Type Selector: BUY vs SELL
                Text(text = "MONITOR PRICE TYPE:", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isBuyPrice,
                        onClick = {
                            isBuyPrice = true
                            conditionType = "AT_OR_BELOW"
                            targetPriceText = (commodity.buyPrice * 0.95).toLong().toString()
                        },
                        label = { Text("Buy Price", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionAmber,
                            selectedLabelColor = HelionDeepGraphite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isBuyPrice,
                        onClick = {
                            isBuyPrice = false
                            conditionType = "AT_OR_ABOVE"
                            targetPriceText = (commodity.sellPrice * 1.10).toLong().toString()
                        },
                        label = { Text("Sell Price", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionHighSecGreen,
                            selectedLabelColor = HelionDeepGraphite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Condition Selector
                Text(text = "TRIGGER CONDITION:", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = conditionType == "AT_OR_BELOW",
                        onClick = { conditionType = "AT_OR_BELOW" },
                        label = { Text("Drops to ≤ Target", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionCyan,
                            selectedLabelColor = HelionDeepGraphite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = conditionType == "AT_OR_ABOVE",
                        onClick = { conditionType = "AT_OR_ABOVE" },
                        label = { Text("Rises to ≥ Target", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionCyan,
                            selectedLabelColor = HelionDeepGraphite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Price Input
                Text(text = "TARGET THRESHOLD PRICE (CR):", style = MaterialTheme.typography.labelSmall, color = HelionTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetPriceText,
                        onValueChange = { targetPriceText = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f).testTag("target_price_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HelionAmber,
                            unfocusedBorderColor = HelionBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick percentage preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(-15, -10, -5, 5, 10, 15).forEach { pct ->
                        OutlinedButton(
                            onClick = {
                                val base = if (isBuyPrice) commodity.buyPrice else commodity.sellPrice
                                val computed = (base * (1.0 + pct / 100.0)).toLong().coerceAtLeast(10L)
                                targetPriceText = computed.toString()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.6.dp, HelionBorder),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${if (pct > 0) "+" else ""}$pct%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (pct < 0) HelionHighSecGreen else HelionAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Station Scope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (scopeStationOnly) "Station: ${commodity.stationName}" else "Scope: Regional (Any Station)",
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = scopeStationOnly,
                        onCheckedChange = { scopeStationOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HelionCyan,
                            checkedTrackColor = HelionCyan.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = targetPriceText.toLongOrNull() ?: currentPrice
                    onSaveAlert(
                        parsed,
                        isBuyPrice,
                        conditionType,
                        if (scopeStationOnly) commodity.stationId else null,
                        if (scopeStationOnly) commodity.stationName else null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = HelionAmber, contentColor = HelionDeepGraphite),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("confirm_save_price_alert")
            ) {
                Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Arm Price Alert", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        val parsed = targetPriceText.toLongOrNull() ?: currentPrice
                        onTestNotification(parsed, isBuyPrice, conditionType)
                    },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, HelionBorder)
                ) {
                    Text("Test Notification", style = MaterialTheme.typography.labelSmall, color = HelionCyan)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = HelionTextSecondary)
                }
            }
        },
        containerColor = HelionSurface
    )
}

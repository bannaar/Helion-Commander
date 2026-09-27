package com.example.helion.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.database.MarketPriceAlertEntity
import com.example.helion.core.database.MarketWatchlistEntity
import com.example.helion.core.model.CommodityCategory
import com.example.helion.core.model.CommodityPriceComparison
import com.example.helion.core.model.MarketItem
import com.example.helion.core.model.MarketTransactionRequest
import com.example.helion.core.model.MarketTransactionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class MarketSortOption(val label: String) {
    DEFAULT("Default"),
    PRICE_HIGH("Highest Price"),
    PRICE_LOW("Lowest Price"),
    TREND_SURGE("Top Surge (▲)"),
    STOCK_LEVEL("High Stock")
}

data class MarketUiState(
    val isLoading: Boolean = false,
    val localCommodities: List<MarketItem> = emptyList(),
    val regionalCommodities: List<MarketItem> = emptyList(),
    val selectedCommodity: MarketItem? = null,
    val priceComparisons: List<CommodityPriceComparison> = emptyList(),
    val watchlist: List<MarketWatchlistEntity> = emptyList(),
    val priceAlerts: List<MarketPriceAlertEntity> = emptyList(),
    val configuringAlertCommodity: MarketItem? = null,
    val isExecutingTransaction: Boolean = false,
    val transactionResult: MarketTransactionResult? = null,
    val isOffline: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: CommodityCategory? = null,
    val sortOption: MarketSortOption = MarketSortOption.DEFAULT,
    val errorMessage: String? = null,
    val actionSuccessBanner: String? = null
)

class MarketViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState(isLoading = true))
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadMarketData()
        observeWatchlist()
        observePriceAlerts()
        observeOffline()
    }

    private fun observeOffline() {
        viewModelScope.launch {
            container.settingsRepository.isOfflineSimulated.collect { off ->
                _uiState.value = _uiState.value.copy(isOffline = off)
            }
        }
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            container.marketRepository.getWatchlist().collect { list ->
                _uiState.value = _uiState.value.copy(watchlist = list)
            }
        }
    }

    private fun observePriceAlerts() {
        viewModelScope.launch {
            container.marketRepository.getAllPriceAlerts().collect { list ->
                _uiState.value = _uiState.value.copy(priceAlerts = list)
                // Check if current prices trigger any active alerts
                evaluatePriceThresholds(
                    items = _uiState.value.localCommodities + _uiState.value.regionalCommodities,
                    alerts = list
                )
            }
        }
    }

    fun loadMarketData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val localRes = container.marketRepository.refreshLocalMarket()
            val regRes = container.marketRepository.refreshRegionalMarkets()

            val localList = localRes.getOrDefault(emptyList())
            val regList = regRes.getOrDefault(emptyList())

            val selected = localList.firstOrNull()
            val comparisons = if (selected != null) {
                container.marketRepository.getPriceComparisons(selected.commodityId, selected.buyPrice)
            } else emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                localCommodities = localList,
                regionalCommodities = regList,
                selectedCommodity = selected,
                priceComparisons = comparisons
            )

            // Evaluate price alerts on fresh market data
            evaluatePriceThresholds(
                items = localList + regList,
                alerts = _uiState.value.priceAlerts
            )
        }
    }

    private fun evaluatePriceThresholds(
        items: List<MarketItem>,
        alerts: List<MarketPriceAlertEntity>
    ) {
        if (items.isEmpty() || alerts.isEmpty()) return

        for (alert in alerts) {
            if (!alert.isActive) continue

            // Find matching commodity
            val matching = items.find {
                it.commodityId == alert.commodityId && (alert.stationId == null || it.stationId == alert.stationId)
            } ?: continue

            val currentPrice = if (alert.isBuyPrice) matching.buyPrice else matching.sellPrice
            val isThresholdMet = when (alert.conditionType) {
                "AT_OR_BELOW", "<=" -> currentPrice <= alert.targetPrice
                "AT_OR_ABOVE", ">=" -> currentPrice >= alert.targetPrice
                else -> false
            }

            if (isThresholdMet) {
                // Dispatch device local notification
                container.notificationManager.notifyMarketPriceThreshold(
                    commodity = matching,
                    targetPrice = alert.targetPrice,
                    isBuyPrice = alert.isBuyPrice,
                    conditionType = alert.conditionType,
                    alertId = alert.alertId
                )

                if (!alert.isTriggered) {
                    viewModelScope.launch {
                        container.marketRepository.markAlertTriggered(
                            alertId = alert.alertId,
                            isTriggered = true,
                            triggeredAt = System.currentTimeMillis(),
                            price = currentPrice
                        )
                    }
                }
            } else if (alert.isTriggered) {
                // Reset triggered state if price moved outside the target zone so it can re-trigger
                viewModelScope.launch {
                    container.marketRepository.markAlertTriggered(
                        alertId = alert.alertId,
                        isTriggered = false,
                        triggeredAt = alert.lastTriggeredAtEpoch ?: 0L,
                        price = currentPrice
                    )
                }
            }
        }
    }

    fun selectCommodity(item: MarketItem) {
        val comparisons = container.marketRepository.getPriceComparisons(item.commodityId, item.buyPrice)
        _uiState.value = _uiState.value.copy(
            selectedCommodity = item,
            priceComparisons = comparisons,
            errorMessage = null,
            transactionResult = null
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: CommodityCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSortOption(sort: MarketSortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sort)
    }

    fun executeTransaction(quantity: Int, isBuyAction: Boolean) {
        val item = _uiState.value.selectedCommodity ?: return

        if (_uiState.value.isOffline) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "TRANSACTION BLOCKED: Client is currently OFFLINE. Authoritative trades require a live verified connection to the HELION Universe Server."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExecutingTransaction = true, errorMessage = null)

            val req = MarketTransactionRequest(
                commanderId = "cmd-bannaar",
                currentStationId = item.stationId,
                commodityId = item.commodityId,
                quantity = quantity,
                isBuyAction = isBuyAction
            )

            val res = container.marketRepository.executeTransaction(req)
            res.onSuccess { txResult ->
                _uiState.value = _uiState.value.copy(
                    isExecutingTransaction = false,
                    transactionResult = txResult
                )
                // Refresh commander balances
                container.commanderRepository.refreshCommanderProfile()
                loadMarketData()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isExecutingTransaction = false,
                    errorMessage = err.message ?: "Transaction failed validation."
                )
            }
        }
    }

    fun toggleWatchlist(item: MarketItem) {
        viewModelScope.launch {
            val isWatched = _uiState.value.watchlist.any { it.commodityId == item.commodityId }
            if (isWatched) {
                container.marketRepository.removeFromWatchlist(item.commodityId)
            } else {
                container.marketRepository.addToWatchlist(item)
            }
        }
    }

    // =========================================================================
    // PRICE THRESHOLD ALERT ACTIONS
    // =========================================================================

    fun openConfiguringAlert(item: MarketItem) {
        _uiState.value = _uiState.value.copy(configuringAlertCommodity = item)
    }

    fun dismissConfiguringAlert() {
        _uiState.value = _uiState.value.copy(configuringAlertCommodity = null)
    }

    fun savePriceAlert(
        commodity: MarketItem,
        targetPrice: Long,
        isBuyPrice: Boolean,
        conditionType: String,
        stationId: String? = null,
        stationName: String? = null
    ) {
        viewModelScope.launch {
            val alert = MarketPriceAlertEntity(
                alertId = "alert-${commodity.commodityId}-${System.currentTimeMillis() % 100000}",
                commodityId = commodity.commodityId,
                displayName = commodity.displayName,
                categoryName = commodity.category.displayName,
                targetPrice = targetPrice,
                isBuyPrice = isBuyPrice,
                conditionType = conditionType,
                stationId = stationId ?: commodity.stationId,
                stationName = stationName ?: commodity.stationName,
                isActive = true,
                isTriggered = false,
                lastCheckedPrice = if (isBuyPrice) commodity.buyPrice else commodity.sellPrice,
                createdAtEpoch = System.currentTimeMillis()
            )

            container.marketRepository.insertOrUpdatePriceAlert(alert)
            _uiState.value = _uiState.value.copy(
                configuringAlertCommodity = null,
                actionSuccessBanner = "PRICE ALERT ARMED: Target ${targetPrice} GSC set for ${commodity.displayName}."
            )

            // Immediately evaluate to check if it's already at or beyond threshold
            evaluatePriceThresholds(
                items = _uiState.value.localCommodities + _uiState.value.regionalCommodities,
                alerts = listOf(alert)
            )
        }
    }

    fun toggleAlertActive(alert: MarketPriceAlertEntity) {
        viewModelScope.launch {
            val newActive = !alert.isActive
            container.marketRepository.setAlertActive(alert.alertId, newActive)
            if (newActive) {
                container.notificationManager.resetDeduplicationForAlert(alert.alertId)
                _uiState.value = _uiState.value.copy(
                    actionSuccessBanner = "Alert enabled for ${alert.displayName} (${alert.targetPrice} GSC)."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    actionSuccessBanner = "Alert paused for ${alert.displayName}."
                )
            }
        }
    }

    fun deletePriceAlert(alertId: String) {
        viewModelScope.launch {
            container.marketRepository.deletePriceAlert(alertId)
            _uiState.value = _uiState.value.copy(
                actionSuccessBanner = "Price threshold alert removed."
            )
        }
    }

    fun sendTestAlertNotification(alert: MarketPriceAlertEntity) {
        val commodity = _uiState.value.localCommodities.find { it.commodityId == alert.commodityId }
            ?: _uiState.value.regionalCommodities.find { it.commodityId == alert.commodityId }
            ?: MarketItem(
                commodityId = alert.commodityId,
                displayName = alert.displayName,
                category = CommodityCategory.METALS,
                unit = "tons",
                buyPrice = alert.targetPrice,
                sellPrice = alert.targetPrice - 20,
                stockUnits = 450,
                demandUnits = 800,
                stationId = alert.stationId ?: "sta-kepler-prime",
                stationName = alert.stationName ?: "Kepler Prime Orbital",
                systemId = "sys-kepler",
                systemName = "Kepler",
                regionId = "reg-solari-core",
                lastUpdatedEpoch = System.currentTimeMillis(),
                priceChange24h = -8
            )

        container.notificationManager.notifyMarketPriceThreshold(
            commodity = commodity,
            targetPrice = alert.targetPrice,
            isBuyPrice = alert.isBuyPrice,
            conditionType = alert.conditionType,
            alertId = alert.alertId,
            force = true
        )

        _uiState.value = _uiState.value.copy(
            actionSuccessBanner = "Local notification dispatched for ${alert.displayName} target threshold."
        )
    }

    fun simulatePriceThresholdHit(alert: MarketPriceAlertEntity) {
        viewModelScope.launch {
            // Calculate a price that satisfies the threshold condition
            val simulatedPrice = when (alert.conditionType) {
                "AT_OR_BELOW", "<=" -> (alert.targetPrice - 15L).coerceAtLeast(10L)
                "AT_OR_ABOVE", ">=" -> alert.targetPrice + 25L
                else -> alert.targetPrice
            }

            val newBuyPrice = if (alert.isBuyPrice) simulatedPrice else simulatedPrice + 30
            val newSellPrice = if (!alert.isBuyPrice) simulatedPrice else simulatedPrice - 30

            // Development-only: mutate mock commodity price to exercise alert behavior
            container.marketRepository.updateCommodityPrice(
                commodityId = alert.commodityId,
                newBuyPrice = newBuyPrice,
                newSellPrice = newSellPrice,
                stationId = alert.stationId
            )

            // Reset deduplication so notification will fire
            container.notificationManager.resetDeduplicationForAlert(alert.alertId)

            _uiState.value = _uiState.value.copy(
                actionSuccessBanner = "MARKET FLUX SIMULATED: ${alert.displayName} price moved to $simulatedPrice GSC! Device notification triggered."
            )

            // Refresh market data and evaluate
            loadMarketData()
        }
    }

    fun dismissTransactionDialog() {
        _uiState.value = _uiState.value.copy(transactionResult = null, errorMessage = null)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(actionSuccessBanner = null, errorMessage = null)
    }
}

package com.example.helion.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
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
    val isExecutingTransaction: Boolean = false,
    val transactionResult: MarketTransactionResult? = null,
    val isOffline: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: CommodityCategory? = null,
    val sortOption: MarketSortOption = MarketSortOption.DEFAULT,
    val errorMessage: String? = null
)

class MarketViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState(isLoading = true))
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadMarketData()
        observeWatchlist()
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

    fun dismissTransactionDialog() {
        _uiState.value = _uiState.value.copy(transactionResult = null, errorMessage = null)
    }
}

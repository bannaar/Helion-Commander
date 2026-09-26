package com.example.helion.core.repository

import com.example.helion.core.database.MarketWatchlistDao
import com.example.helion.core.database.MarketWatchlistEntity
import com.example.helion.core.model.CommodityPriceComparison
import com.example.helion.core.model.MarketItem
import com.example.helion.core.model.MarketTransactionRequest
import com.example.helion.core.model.MarketTransactionResult
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarketRepository(
    private val api: CompanionApi,
    private val watchlistDao: MarketWatchlistDao
) {
    private val _localMarket = MutableStateFlow<List<MarketItem>>(emptyList())
    val localMarket: StateFlow<List<MarketItem>> = _localMarket.asStateFlow()

    private val _regionalMarket = MutableStateFlow<List<MarketItem>>(emptyList())
    val regionalMarket: StateFlow<List<MarketItem>> = _regionalMarket.asStateFlow()

    suspend fun refreshLocalMarket(stationId: String? = null): Result<List<MarketItem>> {
        val res = api.getMarketItems(stationId)
        res.onSuccess { _localMarket.value = it }
        return res
    }

    suspend fun refreshRegionalMarkets(): Result<List<MarketItem>> {
        val res = api.getAllRegionalMarkets()
        res.onSuccess { _regionalMarket.value = it }
        return res
    }

    suspend fun executeTransaction(request: MarketTransactionRequest): Result<MarketTransactionResult> {
        val res = api.executeMarketTransaction(request)
        res.onSuccess {
            // After authoritative server transaction, refresh local & regional market
            refreshLocalMarket(request.currentStationId)
            refreshRegionalMarkets()
        }
        return res
    }

    fun getPriceComparisons(commodityId: String, currentBuyPrice: Long): List<CommodityPriceComparison> {
        val allItems = _regionalMarket.value.filter { it.commodityId == commodityId }
        return allItems.map { item ->
            val margin = item.sellPrice - currentBuyPrice
            val cargoProfit = margin * 16 // based on typical 16 ton hold
            CommodityPriceComparison(
                stationId = item.stationId,
                stationName = item.stationName,
                systemName = item.systemName,
                securityRating = if (item.systemName == "Kepler") 4.8f else if (item.systemName == "Harrow") 1.8f else if (item.systemName == "Crossroads") 3.2f else -2.4f,
                buyPrice = item.buyPrice,
                sellPrice = item.sellPrice,
                stockUnits = item.stockUnits,
                jumpsFromCurrent = if (item.systemName == "Kepler") 0 else if (item.systemName == "Harrow") 1 else if (item.systemName == "Crossroads") 3 else 2,
                estimatedGrossMarginPerTon = margin,
                estimatedCargoProfit = cargoProfit
            )
        }.sortedByDescending { it.estimatedGrossMarginPerTon }
    }

    fun getWatchlist(): Flow<List<MarketWatchlistEntity>> = watchlistDao.getWatchlist()

    suspend fun addToWatchlist(item: MarketItem) {
        watchlistDao.insertWatchlist(
            MarketWatchlistEntity(
                commodityId = item.commodityId,
                displayName = item.displayName,
                categoryName = item.category.displayName,
                targetBuyPrice = item.buyPrice,
                targetSellPrice = item.sellPrice,
                addedAtEpoch = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFromWatchlist(commodityId: String) {
        watchlistDao.removeFromWatchlist(commodityId)
    }
}

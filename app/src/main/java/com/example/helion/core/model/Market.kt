package com.example.helion.core.model

enum class CommodityCategory(val displayName: String) {
    INDUSTRIAL("Industrial Components"),
    METALS("Refined Metals & Alloys"),
    CONSUMER("Consumer & Bio-Provisions"),
    MEDICAL("Pharmaceuticals & Biotech"),
    TECHNOLOGY("Advanced Avionics"),
    WEAPONRY("Munitions & Tactical")
}

data class MarketItem(
    val commodityId: String,
    val displayName: String,
    val category: CommodityCategory,
    val unit: String = "tons",
    val buyPrice: Long, // Price to purchase at this station
    val sellPrice: Long, // Price to sell at this station
    val stockUnits: Int,
    val demandUnits: Int,
    val stationId: String,
    val stationName: String,
    val systemId: String,
    val systemName: String,
    val regionId: String,
    val lastUpdatedEpoch: Long,
    val priceTrendDelta: Int = 0, // negative or positive delta
    val priceChange24h: Int = priceTrendDelta
)

data class CommodityPriceComparison(
    val stationId: String,
    val stationName: String,
    val systemName: String,
    val securityRating: Float,
    val buyPrice: Long,
    val sellPrice: Long,
    val stockUnits: Int,
    val jumpsFromCurrent: Int,
    val estimatedGrossMarginPerTon: Long,
    val estimatedCargoProfit: Long
)

data class MarketTransactionRequest(
    val commanderId: String,
    val currentStationId: String,
    val commodityId: String,
    val quantity: Int,
    val isBuyAction: Boolean // true = commander buying from station; false = selling
)

data class MarketTransactionResult(
    val transactionId: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val authoritativeCredits: Long,
    val authoritativeCargoUnits: Int,
    val authoritativeStock: Int,
    val committedPricePerUnit: Long,
    val timestampEpoch: Long
)

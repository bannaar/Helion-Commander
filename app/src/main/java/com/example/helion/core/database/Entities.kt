package com.example.helion.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_commander_profile")
data class CachedCommanderEntity(
    @PrimaryKey val commanderId: String,
    val displayName: String,
    val callSign: String,
    val credits: Long,
    val xp: Long,
    val rank: String,
    val career: String,
    val currentSystemName: String,
    val currentStationName: String,
    val activeShipId: String,
    val environment: String,
    val cachedAtEpoch: Long
)

@Entity(tableName = "saved_loadout_plans")
data class SavedLoadoutPlanEntity(
    @PrimaryKey val planId: String,
    val name: String,
    val hullId: String,
    val hullName: String,
    val plannedModulesJson: String, // JSON serialization of slotId -> moduleId
    val isFavorite: Boolean,
    val createdAtEpoch: Long
)

@Entity(tableName = "route_bookmarks")
data class RouteBookmarkEntity(
    @PrimaryKey val bookmarkId: String,
    val title: String,
    val originSystemId: String,
    val destinationSystemId: String,
    val originName: String,
    val destinationName: String,
    val jumps: Int,
    val savedAtEpoch: Long
)

@Entity(tableName = "market_watchlist")
data class MarketWatchlistEntity(
    @PrimaryKey val commodityId: String,
    val displayName: String,
    val categoryName: String,
    val targetBuyPrice: Long,
    val targetSellPrice: Long,
    val addedAtEpoch: Long
)

@Entity(tableName = "cached_galnet_articles")
data class CachedGalNetArticleEntity(
    @PrimaryKey val articleId: String,
    val headline: String,
    val summary: String,
    val body: String,
    val category: String,
    val publishedAtEpoch: Long,
    val importance: String,
    val isRead: Boolean,
    val isSaved: Boolean
)

@Entity(tableName = "tactical_missions")
data class TacticalMissionEntity(
    @PrimaryKey val missionId: String,
    val missionName: String = "",
    val status: String = "ACTIVE",
    val completionTimestamp: Long = 0L,
    val title: String = missionName,
    val briefing: String = "",
    val sponsorFaction: String = "",
    val category: String = "COMBAT_INTERDICTION",
    val threatLevel: String = "MODERATE",
    val creditReward: Long = 0L,
    val standingReward: Int = 0,
    val bonusRewardItem: String? = null,
    val primaryLocationJson: String = "{}",
    val objectivesJson: String = "[]",
    val assignedShipName: String = "",
    val assignedFleetStatus: String = "READY_IN_HANGAR",
    val assignedFleetTaskLabel: String = "Standby",
    val fleetProgressPercent: Float = 0f,
    val timeRemainingMinutes: Int = 60,
    val isPriorityTarget: Boolean = false,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
) {
    val id: String get() = missionId
    val name: String get() = missionName.ifEmpty { title }
}

@Entity(tableName = "market_price_alerts")
data class MarketPriceAlertEntity(
    @PrimaryKey val alertId: String,
    val commodityId: String,
    val displayName: String,
    val categoryName: String,
    val targetPrice: Long,
    val isBuyPrice: Boolean,
    val conditionType: String, // "AT_OR_BELOW" or "AT_OR_ABOVE"
    val stationId: String? = null,
    val stationName: String? = null,
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val lastTriggeredAtEpoch: Long? = null,
    val lastCheckedPrice: Long? = null,
    val createdAtEpoch: Long = System.currentTimeMillis()
)

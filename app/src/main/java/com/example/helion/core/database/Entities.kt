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
    val title: String,
    val briefing: String,
    val sponsorFaction: String,
    val category: String,
    val threatLevel: String,
    val creditReward: Long,
    val standingReward: Int,
    val bonusRewardItem: String?,
    val primaryLocationJson: String,
    val objectivesJson: String,
    val assignedShipName: String,
    val assignedFleetStatus: String,
    val assignedFleetTaskLabel: String,
    val fleetProgressPercent: Float,
    val timeRemainingMinutes: Int,
    val status: String,
    val isPriorityTarget: Boolean,
    val lastUpdatedEpoch: Long
)

package com.example.helion.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommanderDao {
    @Query("SELECT * FROM cached_commander_profile LIMIT 1")
    fun getCachedCommander(): Flow<CachedCommanderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedCommander(entity: CachedCommanderEntity)
}

@Dao
interface LoadoutPlanDao {
    @Query("SELECT * FROM saved_loadout_plans ORDER BY createdAtEpoch DESC")
    fun getAllSavedPlans(): Flow<List<SavedLoadoutPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: SavedLoadoutPlanEntity)

    @Query("DELETE FROM saved_loadout_plans WHERE planId = :planId")
    suspend fun deletePlanById(planId: String)
}

@Dao
interface RouteBookmarkDao {
    @Query("SELECT * FROM route_bookmarks ORDER BY savedAtEpoch DESC")
    fun getAllBookmarks(): Flow<List<RouteBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: RouteBookmarkEntity)

    @Query("DELETE FROM route_bookmarks WHERE bookmarkId = :bookmarkId")
    suspend fun deleteBookmark(bookmarkId: String)
}

@Dao
interface MarketWatchlistDao {
    @Query("SELECT * FROM market_watchlist ORDER BY addedAtEpoch DESC")
    fun getWatchlist(): Flow<List<MarketWatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: MarketWatchlistEntity)

    @Query("DELETE FROM market_watchlist WHERE commodityId = :commodityId")
    suspend fun removeFromWatchlist(commodityId: String)
}

@Dao
interface GalNetDao {
    @Query("SELECT * FROM cached_galnet_articles ORDER BY publishedAtEpoch DESC")
    fun getAllCachedArticles(): Flow<List<CachedGalNetArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<CachedGalNetArticleEntity>)

    @Query("UPDATE cached_galnet_articles SET isRead = 1 WHERE articleId = :articleId")
    suspend fun markAsRead(articleId: String)

    @Query("UPDATE cached_galnet_articles SET isSaved = :isSaved WHERE articleId = :articleId")
    suspend fun updateSavedState(articleId: String, isSaved: Boolean)
}

@Dao
interface TacticalMissionDao {
    @Query("SELECT * FROM tactical_missions ORDER BY isPriorityTarget DESC, lastUpdatedEpoch DESC")
    fun getAllMissions(): Flow<List<TacticalMissionEntity>>

    @Query("SELECT * FROM tactical_missions")
    fun getAll(): Flow<List<TacticalMissionEntity>>

    @Query("SELECT * FROM tactical_missions")
    suspend fun getAllMissionsList(): List<TacticalMissionEntity>

    @Query("SELECT * FROM tactical_missions WHERE missionId = :missionId LIMIT 1")
    fun getMissionById(missionId: String): Flow<TacticalMissionEntity?>

    @Query("SELECT * FROM tactical_missions WHERE missionId = :missionId LIMIT 1")
    suspend fun getById(missionId: String): TacticalMissionEntity?

    @Query("SELECT * FROM tactical_missions WHERE status = :status ORDER BY lastUpdatedEpoch DESC")
    fun getMissionsByStatus(status: String): Flow<List<TacticalMissionEntity>>

    @Query("SELECT * FROM tactical_missions WHERE completionTimestamp > 0 ORDER BY completionTimestamp DESC")
    fun getCompletedMissions(): Flow<List<TacticalMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<TacticalMissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: TacticalMissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: TacticalMissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(missions: List<TacticalMissionEntity>)

    @Update
    suspend fun updateMission(mission: TacticalMissionEntity)

    @Update
    suspend fun update(mission: TacticalMissionEntity)

    @Delete
    suspend fun delete(mission: TacticalMissionEntity)

    @Query("UPDATE tactical_missions SET objectivesJson = :objectivesJson, fleetProgressPercent = :fleetProgress, status = :status, lastUpdatedEpoch = :epoch WHERE missionId = :missionId")
    suspend fun updateObjectiveProgress(missionId: String, objectivesJson: String, fleetProgress: Float, status: String, epoch: Long)

    @Query("UPDATE tactical_missions SET assignedFleetStatus = :fleetStatus, fleetProgressPercent = :fleetProgress, lastUpdatedEpoch = :epoch WHERE missionId = :missionId")
    suspend fun updateFleetStatus(missionId: String, fleetStatus: String, fleetProgress: Float, epoch: Long)

    @Query("UPDATE tactical_missions SET isPriorityTarget = :isPriority WHERE missionId = :missionId")
    suspend fun setPriorityTarget(missionId: String, isPriority: Boolean)

    @Query("UPDATE tactical_missions SET status = :status, completionTimestamp = :completionTimestamp, lastUpdatedEpoch = :epoch WHERE missionId = :missionId")
    suspend fun setMissionStatus(missionId: String, status: String, completionTimestamp: Long = 0L, epoch: Long = System.currentTimeMillis())

    @Query("DELETE FROM tactical_missions WHERE missionId = :missionId")
    suspend fun deleteMission(missionId: String)

    @Query("DELETE FROM tactical_missions WHERE missionId = :missionId")
    suspend fun deleteById(missionId: String)

    @Query("DELETE FROM tactical_missions")
    suspend fun deleteAllMissions()

    @Query("DELETE FROM tactical_missions")
    suspend fun deleteAll()
}

@Dao
interface MarketPriceAlertDao {
    @Query("SELECT * FROM market_price_alerts ORDER BY createdAtEpoch DESC")
    fun getAllPriceAlerts(): Flow<List<MarketPriceAlertEntity>>

    @Query("SELECT * FROM market_price_alerts WHERE isActive = 1")
    fun getActivePriceAlerts(): Flow<List<MarketPriceAlertEntity>>

    @Query("SELECT * FROM market_price_alerts WHERE commodityId = :commodityId")
    fun getAlertsForCommodity(commodityId: String): Flow<List<MarketPriceAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAlert(alert: MarketPriceAlertEntity)

    @Query("UPDATE market_price_alerts SET isActive = :isActive WHERE alertId = :alertId")
    suspend fun setAlertActive(alertId: String, isActive: Boolean)

    @Query("UPDATE market_price_alerts SET isTriggered = :isTriggered, lastTriggeredAtEpoch = :triggeredAt, lastCheckedPrice = :price WHERE alertId = :alertId")
    suspend fun markAlertTriggered(alertId: String, isTriggered: Boolean, triggeredAt: Long, price: Long)

    @Query("DELETE FROM market_price_alerts WHERE alertId = :alertId")
    suspend fun deleteAlert(alertId: String)

    @Query("DELETE FROM market_price_alerts")
    suspend fun clearAllAlerts()
}

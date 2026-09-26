package com.example.helion.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM tactical_missions WHERE missionId = :missionId LIMIT 1")
    fun getMissionById(missionId: String): Flow<TacticalMissionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<TacticalMissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: TacticalMissionEntity)

    @Query("UPDATE tactical_missions SET objectivesJson = :objectivesJson, fleetProgressPercent = :fleetProgress, status = :status, lastUpdatedEpoch = :epoch WHERE missionId = :missionId")
    suspend fun updateObjectiveProgress(missionId: String, objectivesJson: String, fleetProgress: Float, status: String, epoch: Long)

    @Query("UPDATE tactical_missions SET assignedFleetStatus = :fleetStatus, fleetProgressPercent = :fleetProgress, lastUpdatedEpoch = :epoch WHERE missionId = :missionId")
    suspend fun updateFleetStatus(missionId: String, fleetStatus: String, fleetProgress: Float, epoch: Long)

    @Query("UPDATE tactical_missions SET isPriorityTarget = :isPriority WHERE missionId = :missionId")
    suspend fun setPriorityTarget(missionId: String, isPriority: Boolean)

    @Query("UPDATE tactical_missions SET status = :status, lastUpdatedEpoch = :epoch WHERE missionId = :missionId")
    suspend fun setMissionStatus(missionId: String, status: String, epoch: Long)

    @Query("DELETE FROM tactical_missions WHERE missionId = :missionId")
    suspend fun deleteMission(missionId: String)
}

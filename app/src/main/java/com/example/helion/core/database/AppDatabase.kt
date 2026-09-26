package com.example.helion.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedCommanderEntity::class,
        SavedLoadoutPlanEntity::class,
        RouteBookmarkEntity::class,
        MarketWatchlistEntity::class,
        CachedGalNetArticleEntity::class,
        TacticalMissionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commanderDao(): CommanderDao
    abstract fun loadoutPlanDao(): LoadoutPlanDao
    abstract fun routeBookmarkDao(): RouteBookmarkDao
    abstract fun marketWatchlistDao(): MarketWatchlistDao
    abstract fun galNetDao(): GalNetDao
    abstract fun tacticalMissionDao(): TacticalMissionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "helion_commander_cache.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

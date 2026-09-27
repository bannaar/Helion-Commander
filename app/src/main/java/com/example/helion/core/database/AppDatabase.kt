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
        TacticalMissionEntity::class,
        MarketPriceAlertEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commanderDao(): CommanderDao
    abstract fun loadoutPlanDao(): LoadoutPlanDao
    abstract fun routeBookmarkDao(): RouteBookmarkDao
    abstract fun marketWatchlistDao(): MarketWatchlistDao
    abstract fun marketPriceAlertDao(): MarketPriceAlertDao
    abstract fun galNetDao(): GalNetDao
    abstract fun tacticalMissionDao(): TacticalMissionDao

    companion object {
        private val instances = java.util.concurrent.ConcurrentHashMap<com.example.helion.core.model.ServerEnvironment, AppDatabase>()

        fun getInstance(
            context: Context,
            environment: com.example.helion.core.model.ServerEnvironment = com.example.helion.core.model.ServerEnvironment.DEMO
        ): AppDatabase {
            return instances.computeIfAbsent(environment) { env ->
                val dbName = when (env) {
                    com.example.helion.core.model.ServerEnvironment.DEMO -> "helion_commander_demo.db"
                    com.example.helion.core.model.ServerEnvironment.PRIVATE_TEST -> "helion_commander_test.db"
                    com.example.helion.core.model.ServerEnvironment.PRODUCTION -> "helion_commander_production.db"
                }
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                ).fallbackToDestructiveMigration().build()
            }
        }
    }
}

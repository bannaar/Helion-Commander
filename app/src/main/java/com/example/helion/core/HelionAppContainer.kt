package com.example.helion.core

import android.content.Context
import com.example.helion.core.database.AppDatabase
import com.example.helion.core.network.CompanionApi
import com.example.helion.core.network.FakeCompanionApi
import com.example.helion.core.notification.HelionNotificationManager
import com.example.helion.core.repository.CommanderRepository
import com.example.helion.core.repository.CommsRepository
import com.example.helion.core.repository.FleetRepository
import com.example.helion.core.repository.GalNetRepository
import com.example.helion.core.repository.GuildRepository
import com.example.helion.core.repository.MarketRepository
import com.example.helion.core.repository.SettingsRepository
import com.example.helion.core.repository.TacticalMissionsRepository
import com.example.helion.core.repository.UniverseRepository

class HelionAppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val api: CompanionApi = FakeCompanionApi()
    val notificationManager = HelionNotificationManager(context)

    val commanderRepository = CommanderRepository(api, database.commanderDao())
    val fleetRepository = FleetRepository(api, database.loadoutPlanDao())
    val universeRepository = UniverseRepository(api, database.routeBookmarkDao())
    val marketRepository = MarketRepository(api, database.marketWatchlistDao(), database.marketPriceAlertDao())
    val galNetRepository = GalNetRepository(api, database.galNetDao())
    val guildRepository = GuildRepository(api)
    val commsRepository = CommsRepository(api)
    val settingsRepository = SettingsRepository(api)
    val tacticalMissionsRepository = TacticalMissionsRepository(api, database.tacticalMissionDao())
}

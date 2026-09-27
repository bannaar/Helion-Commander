package com.example.helion.core

import android.content.Context
import com.example.helion.core.database.AppDatabase
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.network.CompanionApi
import com.example.helion.core.network.CompanionApiFactory
import com.example.helion.core.network.DefaultCompanionApiFactory
import com.example.helion.core.network.DelegatingCompanionApi
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
import com.example.helion.core.session.AuthCredentialStore
import com.example.helion.core.session.EnvironmentPreferences

class HelionAppContainer(val context: Context) {
    val environmentPreferences = EnvironmentPreferences(context)
    val credentialStore = AuthCredentialStore(context)
    val apiFactory: CompanionApiFactory = DefaultCompanionApiFactory()

    var activeEnvironment: ServerEnvironment = environmentPreferences.getSelectedEnvironment()
        private set

    val api: CompanionApi = DelegatingCompanionApi(apiFactory) { activeEnvironment }

    val database: AppDatabase
        get() = AppDatabase.getInstance(context, activeEnvironment)

    val notificationManager = HelionNotificationManager(context)

    val settingsRepository = SettingsRepository(api, environmentPreferences)
    val commanderRepository = CommanderRepository(api, database.commanderDao())
    val fleetRepository = FleetRepository(api, database.loadoutPlanDao())
    val universeRepository = UniverseRepository(api, database.routeBookmarkDao())
    val marketRepository = MarketRepository(api, database.marketWatchlistDao(), database.marketPriceAlertDao())
    val galNetRepository = GalNetRepository(api, database.galNetDao())
    val guildRepository = GuildRepository(api)
    val commsRepository = CommsRepository(api)
    val tacticalMissionsRepository = TacticalMissionsRepository(api, database.tacticalMissionDao())

    fun setActiveServerEnvironment(env: ServerEnvironment) {
        activeEnvironment = env
        environmentPreferences.setSelectedEnvironment(env)
    }
}

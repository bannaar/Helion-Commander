package com.example.helion.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.model.GalNetArticle
import com.example.helion.core.model.GuildInfo
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.MarketItem
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.core.model.RoutePlanResult
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.TacticalMission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeDashboardUiState(
    val isLoading: Boolean = true,
    val commander: CommanderProfile? = null,
    val activeShip: OwnedShipInstance? = null,
    val currentSystem: SystemNode? = null,
    val activeRoutePlan: RoutePlanResult? = null,
    val activeMissions: List<TacticalMission> = emptyList(),
    val marketWatchItems: List<MarketItem> = emptyList(),
    val latestArticles: List<GalNetArticle> = emptyList(),
    val guildInfo: GuildInfo? = null,
    val unreadCommsCount: Int = 2,
    val environment: HelionEnvironment = HelionEnvironment.DEVELOPMENT,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeEnvironment()
    }

    private fun observeEnvironment() {
        viewModelScope.launch {
            container.settingsRepository.currentEnvironment.collect { env ->
                val changed = _uiState.value.environment != env
                if (changed) {
                    val offline = _uiState.value.isOffline
                    _uiState.value = HomeDashboardUiState(
                        isLoading = true,
                        environment = env,
                        isOffline = offline
                    )
                    loadDashboardData()
                } else {
                    _uiState.value = _uiState.value.copy(environment = env)
                }
            }
        }
        viewModelScope.launch {
            container.settingsRepository.isOfflineSimulated.collect { off ->
                _uiState.value = _uiState.value.copy(isOffline = off)
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Commander Profile
            val cmdRes = container.commanderRepository.refreshCommanderProfile()
            val commander = cmdRes.getOrNull()

            // Fleet & Active Ship
            val fleetRes = container.fleetRepository.refreshFleet()
            val fleet = fleetRes.getOrDefault(emptyList())
            val activeShip = fleet.find { it.isActiveShip } ?: fleet.firstOrNull()

            // Universe Systems & Current Location
            val universeRes = container.universeRepository.refreshGalaxySystems()
            val systems = universeRes.getOrDefault(emptyMap())
            val currentSys = systems[commander?.currentSystemId ?: "sys-kepler"]

            // Route sample (Kepler -> Crossroads)
            val route = container.universeRepository.calculateRoute(
                originId = "sys-kepler",
                destinationId = "sys-crossroads",
                mode = com.example.helion.core.model.RouteOptimizationMode.FASTEST
            )

            // Markets
            val mktRes = container.marketRepository.refreshLocalMarket()
            val markets = mktRes.getOrDefault(emptyList()).take(3)

            // GalNet
            val galRes = container.galNetRepository.refreshArticles()
            val articles = galRes.getOrDefault(emptyList()).take(2)

            // Guild
            val guildRes = container.guildRepository.refreshGuild()
            val guild = guildRes.getOrNull()

            // Tactical Missions
            val missionsRes = container.tacticalMissionsRepository.refreshMissions()
            val missions = missionsRes.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                commander = commander,
                activeShip = activeShip,
                currentSystem = currentSys,
                activeRoutePlan = route,
                activeMissions = missions,
                marketWatchItems = markets,
                latestArticles = articles,
                guildInfo = guild
            )
        }
    }
}

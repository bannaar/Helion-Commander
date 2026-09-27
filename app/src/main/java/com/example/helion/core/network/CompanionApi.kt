package com.example.helion.core.network

import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.model.CommsConversation
import com.example.helion.core.model.GalNetArticle
import com.example.helion.core.model.GuildInfo
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.LiveryOption
import com.example.helion.core.model.MarketItem
import com.example.helion.core.model.MarketTransactionRequest
import com.example.helion.core.model.MarketTransactionResult
import com.example.helion.core.model.ModuleItem
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.core.model.ShipDefinition
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerStatus
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.UniverseMessage

interface CompanionApi {

    // Environment & Session
    suspend fun getCurrentEnvironment(): HelionEnvironment
    suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit>
    fun getServerEnvironment(): ServerEnvironment = ServerEnvironment.DEMO
    suspend fun getServerStatus(): Result<ServerStatus>

    // Commander
    suspend fun getCommanderProfile(): Result<CommanderProfile>

    // Fleet & Ships
    suspend fun getOwnedShips(): Result<List<OwnedShipInstance>>
    suspend fun getShipDefinition(hullId: String): Result<ShipDefinition>
    suspend fun getAllShipDefinitions(): Result<List<ShipDefinition>>
    suspend fun getAvailableModules(): Result<List<ModuleItem>>
    suspend fun setActiveShip(shipInstanceId: String): Result<OwnedShipInstance>
    suspend fun applyLoadout(shipInstanceId: String, plannedModules: Map<String, String>): Result<OwnedShipInstance>
    suspend fun requestLiveryUpdate(shipInstanceId: String, liveryId: String): Result<OwnedShipInstance>
    suspend fun performShipMaintenance(shipInstanceId: String): Result<OwnedShipInstance>

    // Universe & Navigation
    suspend fun getGalaxySystems(): Result<Map<String, SystemNode>>
    suspend fun getSystemDetails(systemId: String): Result<SystemNode>

    // Regional Market
    suspend fun getMarketItems(stationId: String? = null): Result<List<MarketItem>>
    suspend fun getAllRegionalMarkets(): Result<List<MarketItem>>
    suspend fun executeMarketTransaction(request: MarketTransactionRequest): Result<MarketTransactionResult>

    // GalNet
    suspend fun getGalNetArticles(): Result<List<GalNetArticle>>
    suspend fun markArticleRead(articleId: String): Result<Unit>

    // Guild
    suspend fun getGuildInfo(): Result<GuildInfo>
    suspend fun postGuildNotice(title: String, body: String): Result<Unit>

    // Comms
    suspend fun getConversations(): Result<List<CommsConversation>>
    suspend fun getMessages(conversationId: String): Result<List<UniverseMessage>>
    suspend fun sendMessage(conversationId: String, body: String): Result<UniverseMessage>

    // Tactical Missions & Fleet Tasks
    suspend fun getTacticalMissions(): Result<List<com.example.helion.core.model.TacticalMission>>
    suspend fun advanceMissionObjective(missionId: String, objectiveId: String, increment: Int = 1): Result<com.example.helion.core.model.TacticalMission>
    suspend fun updateFleetTaskStatus(missionId: String, status: com.example.helion.core.model.FleetTaskStatus, progressDelta: Float): Result<com.example.helion.core.model.TacticalMission>
    suspend fun acceptMissionContract(missionId: String): Result<com.example.helion.core.model.TacticalMission>
    suspend fun claimMissionReward(missionId: String): Result<com.example.helion.core.model.TacticalMission>
    suspend fun setMissionPriority(missionId: String, isPriority: Boolean): Result<com.example.helion.core.model.TacticalMission>
    suspend fun abandonMission(missionId: String): Result<Unit>
}


/**
 * Development-only controls for exercising UI behavior against mock data.
 *
 * RealCompanionApi MUST NOT implement this interface. These methods never represent
 * ordinary player authority in the persistent HELION universe.
 */
interface DevelopmentSimulationApi {
    suspend fun simulateShipWear(
        shipInstanceId: String,
        hullDamage: Float,
        wearIncrease: Float
    ): Result<OwnedShipInstance>

    suspend fun updateCommodityPrice(
        commodityId: String,
        newBuyPrice: Long,
        newSellPrice: Long,
        stationId: String? = null
    ): Result<MarketItem>
}

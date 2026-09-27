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
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerStatus
import com.example.helion.core.model.ShipDefinition
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.TacticalMission
import com.example.helion.core.model.UniverseMessage

/**
 * DelegatingCompanionApi dynamically routes calls to the active CompanionApi based on
 * the current environment supplied by the environment provider.
 */
class DelegatingCompanionApi(
    private val factory: CompanionApiFactory,
    private val environmentProvider: () -> ServerEnvironment
) : CompanionApi {

    val currentApi: CompanionApi
        get() = factory.getApi(environmentProvider())

    override fun getServerEnvironment(): ServerEnvironment = environmentProvider()

    override suspend fun getCurrentEnvironment(): HelionEnvironment =
        currentApi.getCurrentEnvironment()

    override suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit> =
        currentApi.switchEnvironment(env)

    override suspend fun getServerStatus(): Result<ServerStatus> =
        currentApi.getServerStatus()

    override suspend fun getCommanderProfile(): Result<CommanderProfile> =
        currentApi.getCommanderProfile()

    override suspend fun getOwnedShips(): Result<List<OwnedShipInstance>> =
        currentApi.getOwnedShips()

    override suspend fun getShipDefinition(hullId: String): Result<ShipDefinition> =
        currentApi.getShipDefinition(hullId)

    override suspend fun getAllShipDefinitions(): Result<List<ShipDefinition>> =
        currentApi.getAllShipDefinitions()

    override suspend fun getAvailableModules(): Result<List<ModuleItem>> =
        currentApi.getAvailableModules()

    override suspend fun setActiveShip(shipInstanceId: String): Result<OwnedShipInstance> =
        currentApi.setActiveShip(shipInstanceId)

    override suspend fun applyLoadout(
        shipInstanceId: String,
        plannedModules: Map<String, String>
    ): Result<OwnedShipInstance> = currentApi.applyLoadout(shipInstanceId, plannedModules)

    override suspend fun requestLiveryUpdate(
        shipInstanceId: String,
        liveryId: String
    ): Result<OwnedShipInstance> = currentApi.requestLiveryUpdate(shipInstanceId, liveryId)

    override suspend fun performShipMaintenance(shipInstanceId: String): Result<OwnedShipInstance> =
        currentApi.performShipMaintenance(shipInstanceId)

    override suspend fun getGalaxySystems(): Result<Map<String, SystemNode>> =
        currentApi.getGalaxySystems()

    override suspend fun getSystemDetails(systemId: String): Result<SystemNode> =
        currentApi.getSystemDetails(systemId)

    override suspend fun getMarketItems(stationId: String?): Result<List<MarketItem>> =
        currentApi.getMarketItems(stationId)

    override suspend fun getAllRegionalMarkets(): Result<List<MarketItem>> =
        currentApi.getAllRegionalMarkets()

    override suspend fun executeMarketTransaction(request: MarketTransactionRequest): Result<MarketTransactionResult> =
        currentApi.executeMarketTransaction(request)

    override suspend fun getGalNetArticles(): Result<List<GalNetArticle>> =
        currentApi.getGalNetArticles()

    override suspend fun markArticleRead(articleId: String): Result<Unit> =
        currentApi.markArticleRead(articleId)

    override suspend fun getGuildInfo(): Result<GuildInfo> =
        currentApi.getGuildInfo()

    override suspend fun postGuildNotice(title: String, body: String): Result<Unit> =
        currentApi.postGuildNotice(title, body)

    override suspend fun getConversations(): Result<List<CommsConversation>> =
        currentApi.getConversations()

    override suspend fun getMessages(conversationId: String): Result<List<UniverseMessage>> =
        currentApi.getMessages(conversationId)

    override suspend fun sendMessage(conversationId: String, body: String): Result<UniverseMessage> =
        currentApi.sendMessage(conversationId, body)

    override suspend fun getTacticalMissions(): Result<List<TacticalMission>> =
        currentApi.getTacticalMissions()

    override suspend fun advanceMissionObjective(
        missionId: String,
        objectiveId: String,
        increment: Int
    ): Result<TacticalMission> = currentApi.advanceMissionObjective(missionId, objectiveId, increment)

    override suspend fun updateFleetTaskStatus(
        missionId: String,
        status: com.example.helion.core.model.FleetTaskStatus,
        progressDelta: Float
    ): Result<TacticalMission> = currentApi.updateFleetTaskStatus(missionId, status, progressDelta)

    override suspend fun acceptMissionContract(missionId: String): Result<TacticalMission> =
        currentApi.acceptMissionContract(missionId)

    override suspend fun claimMissionReward(missionId: String): Result<TacticalMission> =
        currentApi.claimMissionReward(missionId)

    override suspend fun setMissionPriority(missionId: String, isPriority: Boolean): Result<TacticalMission> =
        currentApi.setMissionPriority(missionId, isPriority)

    override suspend fun abandonMission(missionId: String): Result<Unit> =
        currentApi.abandonMission(missionId)
}

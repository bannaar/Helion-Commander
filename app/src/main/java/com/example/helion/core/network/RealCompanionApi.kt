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
import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerStatus
import com.example.helion.core.model.ShipDefinition
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.TacticalMission
import com.example.helion.core.model.UniverseMessage

class ServerNotConfiguredException(message: String) : Exception(message)
class ServerUnavailableException(message: String) : Exception(message)

/**
 * RealCompanionApi represents the communication bridge to an authoritative HELION universe server.
 *
 * CRITICAL ARCHITECTURAL CONSTRAINTS:
 * 1. RealCompanionApi MUST NOT implement DevelopmentSimulationApi.
 * 2. Unconfigured environments must fail honestly without silently routing to FakeCompanionApi.
 * 3. Does not invent unverified endpoints or claim implemented features before verified server support.
 */
class RealCompanionApi(
    val environment: ServerEnvironment,
    val endpoint: ServerEndpoint? = null,
    private val statusProbe: NativeServerStatusProbe = TlsNativeServerStatusProbe()
) : CompanionApi {

    val isConfigured: Boolean
        get() = endpoint != null

    init {
        require(environment != ServerEnvironment.DEMO) {
            "RealCompanionApi must not be used for DEMO environment. Use FakeCompanionApi for DEMO/OFFLINE mode."
        }
    }

    private fun <T> unavailableOperation(operation: String): Result<T> {
        if (!isConfigured) {
            return Result.failure(
                ServerNotConfiguredException(
                    "SERVER NOT CONFIGURED: ${environment.displayName} has no verified endpoint."
                )
            )
        }
        return Result.failure(
            ServerUnavailableException(
                "SERVER OPERATION NOT IMPLEMENTED: '$operation' is not yet integrated for ${environment.displayName}."
            )
        )
    }

    override fun getServerEnvironment(): ServerEnvironment = environment

    override suspend fun getCurrentEnvironment(): HelionEnvironment =
        HelionEnvironment.fromServerEnvironment(environment)

    override suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getServerStatus(): Result<ServerStatus> {
        val configuredEndpoint = endpoint ?: return Result.failure(
            ServerNotConfiguredException("SERVER NOT CONFIGURED: ${environment.displayName} has no verified endpoint.")
        )
        return statusProbe.probe(environment, configuredEndpoint)
    }

    // Commander
    override suspend fun getCommanderProfile(): Result<CommanderProfile> =
        unavailableOperation("getCommanderProfile")

    // Fleet & Ships
    override suspend fun getOwnedShips(): Result<List<OwnedShipInstance>> =
        unavailableOperation("getOwnedShips")

    override suspend fun getShipDefinition(hullId: String): Result<ShipDefinition> =
        unavailableOperation("getShipDefinition")

    override suspend fun getAllShipDefinitions(): Result<List<ShipDefinition>> =
        unavailableOperation("getAllShipDefinitions")

    override suspend fun getAvailableModules(): Result<List<ModuleItem>> =
        unavailableOperation("getAvailableModules")

    override suspend fun setActiveShip(shipInstanceId: String): Result<OwnedShipInstance> =
        unavailableOperation("setActiveShip")

    override suspend fun applyLoadout(
        shipInstanceId: String,
        plannedModules: Map<String, String>
    ): Result<OwnedShipInstance> = unavailableOperation("applyLoadout")

    override suspend fun requestLiveryUpdate(
        shipInstanceId: String,
        liveryId: String
    ): Result<OwnedShipInstance> = unavailableOperation("requestLiveryUpdate")

    override suspend fun performShipMaintenance(shipInstanceId: String): Result<OwnedShipInstance> =
        unavailableOperation("performShipMaintenance")

    // Universe & Navigation
    override suspend fun getGalaxySystems(): Result<Map<String, SystemNode>> =
        unavailableOperation("getGalaxySystems")

    override suspend fun getSystemDetails(systemId: String): Result<SystemNode> =
        unavailableOperation("getSystemDetails")

    // Regional Market
    override suspend fun getMarketItems(stationId: String?): Result<List<MarketItem>> =
        unavailableOperation("getMarketItems")

    override suspend fun getAllRegionalMarkets(): Result<List<MarketItem>> =
        unavailableOperation("getAllRegionalMarkets")

    override suspend fun executeMarketTransaction(request: MarketTransactionRequest): Result<MarketTransactionResult> =
        unavailableOperation("executeMarketTransaction")

    // GalNet / UniNet
    override suspend fun getGalNetArticles(): Result<List<GalNetArticle>> =
        unavailableOperation("getGalNetArticles")

    override suspend fun markArticleRead(articleId: String): Result<Unit> =
        unavailableOperation("markArticleRead")

    // Guild
    override suspend fun getGuildInfo(): Result<GuildInfo> =
        unavailableOperation("getGuildInfo")

    override suspend fun postGuildNotice(title: String, body: String): Result<Unit> =
        unavailableOperation("postGuildNotice")

    // Comms
    override suspend fun getConversations(): Result<List<CommsConversation>> =
        unavailableOperation("getConversations")

    override suspend fun getMessages(conversationId: String): Result<List<UniverseMessage>> =
        unavailableOperation("getMessages")

    override suspend fun sendMessage(conversationId: String, body: String): Result<UniverseMessage> =
        unavailableOperation("sendMessage")

    // Tactical Missions & Fleet Tasks
    override suspend fun getTacticalMissions(): Result<List<TacticalMission>> =
        unavailableOperation("getTacticalMissions")

    override suspend fun advanceMissionObjective(
        missionId: String,
        objectiveId: String,
        increment: Int
    ): Result<TacticalMission> = unavailableOperation("advanceMissionObjective")

    override suspend fun updateFleetTaskStatus(
        missionId: String,
        status: com.example.helion.core.model.FleetTaskStatus,
        progressDelta: Float
    ): Result<TacticalMission> = unavailableOperation("updateFleetTaskStatus")

    override suspend fun acceptMissionContract(missionId: String): Result<TacticalMission> =
        unavailableOperation("acceptMissionContract")

    override suspend fun claimMissionReward(missionId: String): Result<TacticalMission> =
        unavailableOperation("claimMissionReward")

    override suspend fun setMissionPriority(missionId: String, isPriority: Boolean): Result<TacticalMission> =
        unavailableOperation("setMissionPriority")

    override suspend fun abandonMission(missionId: String): Result<Unit> =
        unavailableOperation("abandonMission")
}

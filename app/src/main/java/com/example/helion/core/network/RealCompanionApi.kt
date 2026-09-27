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
    val baseUrl: String? = null,
    val isConfigured: Boolean = false
) : CompanionApi {

    init {
        require(environment != ServerEnvironment.DEMO) {
            "RealCompanionApi must not be used for DEMO environment. Use FakeCompanionApi for DEMO/OFFLINE mode."
        }
    }

    private fun <T> notConfiguredFailure(operation: String): Result<T> {
        val msg = if (!isConfigured || baseUrl.isNullOrBlank()) {
            "SERVER NOT CONFIGURED: ${environment.displayName} has no verified endpoint."
        } else {
            "SERVER API NOT AVAILABLE: Operation '$operation' is not yet implemented on the authoritative ${environment.displayName} server."
        }
        return Result.failure(ServerNotConfiguredException(msg))
    }

    override fun getServerEnvironment(): ServerEnvironment = environment

    override suspend fun getCurrentEnvironment(): HelionEnvironment =
        HelionEnvironment.fromServerEnvironment(environment)

    override suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getServerStatus(): Result<ServerStatus> {
        if (!isConfigured || baseUrl.isNullOrBlank()) {
            return Result.failure(
                ServerNotConfiguredException("SERVER NOT CONFIGURED: ${environment.displayName} has no verified endpoint.")
            )
        }
        return Result.failure(
            ServerUnavailableException("SERVER API NOT AVAILABLE: Status probe endpoint is not yet connected for ${environment.displayName}.")
        )
    }

    // Commander
    override suspend fun getCommanderProfile(): Result<CommanderProfile> =
        notConfiguredFailure("getCommanderProfile")

    // Fleet & Ships
    override suspend fun getOwnedShips(): Result<List<OwnedShipInstance>> =
        notConfiguredFailure("getOwnedShips")

    override suspend fun getShipDefinition(hullId: String): Result<ShipDefinition> =
        notConfiguredFailure("getShipDefinition")

    override suspend fun getAllShipDefinitions(): Result<List<ShipDefinition>> =
        notConfiguredFailure("getAllShipDefinitions")

    override suspend fun getAvailableModules(): Result<List<ModuleItem>> =
        notConfiguredFailure("getAvailableModules")

    override suspend fun setActiveShip(shipInstanceId: String): Result<OwnedShipInstance> =
        notConfiguredFailure("setActiveShip")

    override suspend fun applyLoadout(
        shipInstanceId: String,
        plannedModules: Map<String, String>
    ): Result<OwnedShipInstance> = notConfiguredFailure("applyLoadout")

    override suspend fun requestLiveryUpdate(
        shipInstanceId: String,
        liveryId: String
    ): Result<OwnedShipInstance> = notConfiguredFailure("requestLiveryUpdate")

    override suspend fun performShipMaintenance(shipInstanceId: String): Result<OwnedShipInstance> =
        notConfiguredFailure("performShipMaintenance")

    // Universe & Navigation
    override suspend fun getGalaxySystems(): Result<Map<String, SystemNode>> =
        notConfiguredFailure("getGalaxySystems")

    override suspend fun getSystemDetails(systemId: String): Result<SystemNode> =
        notConfiguredFailure("getSystemDetails")

    // Regional Market
    override suspend fun getMarketItems(stationId: String?): Result<List<MarketItem>> =
        notConfiguredFailure("getMarketItems")

    override suspend fun getAllRegionalMarkets(): Result<List<MarketItem>> =
        notConfiguredFailure("getAllRegionalMarkets")

    override suspend fun executeMarketTransaction(request: MarketTransactionRequest): Result<MarketTransactionResult> =
        notConfiguredFailure("executeMarketTransaction")

    // GalNet / UniNet
    override suspend fun getGalNetArticles(): Result<List<GalNetArticle>> =
        notConfiguredFailure("getGalNetArticles")

    override suspend fun markArticleRead(articleId: String): Result<Unit> =
        notConfiguredFailure("markArticleRead")

    // Guild
    override suspend fun getGuildInfo(): Result<GuildInfo> =
        notConfiguredFailure("getGuildInfo")

    override suspend fun postGuildNotice(title: String, body: String): Result<Unit> =
        notConfiguredFailure("postGuildNotice")

    // Comms
    override suspend fun getConversations(): Result<List<CommsConversation>> =
        notConfiguredFailure("getConversations")

    override suspend fun getMessages(conversationId: String): Result<List<UniverseMessage>> =
        notConfiguredFailure("getMessages")

    override suspend fun sendMessage(conversationId: String, body: String): Result<UniverseMessage> =
        notConfiguredFailure("sendMessage")

    // Tactical Missions & Fleet Tasks
    override suspend fun getTacticalMissions(): Result<List<TacticalMission>> =
        notConfiguredFailure("getTacticalMissions")

    override suspend fun advanceMissionObjective(
        missionId: String,
        objectiveId: String,
        increment: Int
    ): Result<TacticalMission> = notConfiguredFailure("advanceMissionObjective")

    override suspend fun updateFleetTaskStatus(
        missionId: String,
        status: com.example.helion.core.model.FleetTaskStatus,
        progressDelta: Float
    ): Result<TacticalMission> = notConfiguredFailure("updateFleetTaskStatus")

    override suspend fun acceptMissionContract(missionId: String): Result<TacticalMission> =
        notConfiguredFailure("acceptMissionContract")

    override suspend fun claimMissionReward(missionId: String): Result<TacticalMission> =
        notConfiguredFailure("claimMissionReward")

    override suspend fun setMissionPriority(missionId: String, isPriority: Boolean): Result<TacticalMission> =
        notConfiguredFailure("setMissionPriority")

    override suspend fun abandonMission(missionId: String): Result<Unit> =
        notConfiguredFailure("abandonMission")
}

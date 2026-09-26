package com.example.helion.core.model

data class FactionStanding(
    val factionId: String,
    val factionName: String,
    val standing: Float, // -10.0 to +10.0
    val rankTitle: String,
    val isAllied: Boolean = standing >= 5.0f,
    val isHostile: Boolean = standing <= -3.0f
)

data class CommanderLicense(
    val licenseId: String,
    val name: String,
    val issuingAuthority: String,
    val active: Boolean
)

data class CommanderProfile(
    val commanderId: String,
    val displayName: String,
    val callSign: String,
    val credits: Long,
    val xp: Long,
    val rank: String,
    val career: String,
    val currentSystemId: String,
    val currentSystemName: String,
    val currentStationId: String,
    val currentStationName: String,
    val activeShipId: String,
    val factionStandings: List<FactionStanding>,
    val licenses: List<CommanderLicense>,
    val guildId: String?,
    val guildTicker: String?,
    val allianceId: String?,
    val environment: HelionEnvironment,
    val serverTimeEpoch: Long
)

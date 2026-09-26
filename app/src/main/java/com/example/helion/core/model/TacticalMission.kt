package com.example.helion.core.model

enum class MissionCategory(
    val displayName: String,
    val description: String,
    val primaryColorHex: Long
) {
    COMBAT_INTERDICTION("Combat Interdiction", "Neutralization of pirate raiders, hostile dreadnoughts and outlaw fleets", 0xFFD32F2F),
    MINING_EXTRACTION("Resource Extraction", "Deep planetary and asteroid mantle extraction of volatile isotopes & heavy ores", 0xFFFFA000),
    RECON_SURVEILLANCE("Deep Recon & Survey", "High-resolution orbital telemetry scanning of uncharted sub-space anomalies", 0xFF00E5FF),
    CARGO_ESCORT("High-Value Escort", "Armed convoy escort and high-priority logistics haul through volatile star lanes", 0xFF448AFF),
    COVERT_BLACK_OPS("Covert Black Ops", "Unsanctioned sovereign infiltration, signal interception and artifact retrieval", 0xFF9C27B0)
}

enum class MissionStatus(val label: String) {
    AVAILABLE("Available Contract"),
    ACTIVE("Active Operation"),
    COMPLETED("Objectives Met - Ready for Claim"),
    CLAIMED("Archived / Claimed")
}

enum class ObjectiveStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    FAILED("Failed")
}

enum class FleetTaskStatus(val label: String, val badgeColorHex: Long) {
    IDLE("Idle / Standby", 0xFF78909C),
    EN_ROUTE("In Sub-Warp Transit", 0xFF448AFF),
    ENGAGING("Combat Engagement", 0xFFD32F2F),
    HARVESTING("Planetary Drilling", 0xFFFFA000),
    SURVEYING("Telemetry Waveform Scan", 0xFF00E5FF),
    ORBITAL_PATROL("Orbital Security Grid", 0xFF00E676)
}

data class LocationMarker(
    val systemId: String,
    val systemName: String,
    val celestialBodyName: String,
    val beaconCode: String,
    val coordinates: String,
    val securityRating: Float,
    val distanceLy: Float,
    val jumpCount: Int
)

data class TacticalObjective(
    val id: String,
    val title: String,
    val description: String,
    val status: ObjectiveStatus,
    val currentProgress: Int,
    val targetProgress: Int,
    val unit: String,
    val locationMarker: LocationMarker,
    val assignedFleetUnit: String? = null
) {
    val progressPercent: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f) else 0f

    val isCompleted: Boolean
        get() = currentProgress >= targetProgress
}

data class TacticalMission(
    val id: String,
    val title: String,
    val briefing: String,
    val sponsorFaction: String,
    val category: MissionCategory,
    val threatLevel: ThreatLevel,
    val creditReward: Long,
    val standingReward: Int,
    val bonusRewardItem: String? = null,
    val primaryLocation: LocationMarker,
    val objectives: List<TacticalObjective>,
    val assignedShipName: String,
    val assignedFleetStatus: FleetTaskStatus,
    val assignedFleetTaskLabel: String,
    val fleetProgressPercent: Float,
    val timeRemainingMinutes: Int,
    val status: MissionStatus,
    val isPriorityTarget: Boolean = false
) {
    val overallProgressPercent: Float
        get() {
            if (objectives.isEmpty()) return fleetProgressPercent / 100f
            val completedCount = objectives.count { it.isCompleted }
            val fractional = objectives.sumOf { it.progressPercent.toDouble() } / objectives.size.toDouble()
            return fractional.toFloat().coerceIn(0f, 1f)
        }

    val isFullyCompleted: Boolean
        get() = objectives.all { it.isCompleted } || status == MissionStatus.COMPLETED || status == MissionStatus.CLAIMED
}

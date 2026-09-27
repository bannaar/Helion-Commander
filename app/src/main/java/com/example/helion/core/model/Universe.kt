package com.example.helion.core.model

enum class SecurityClass(val label: String, val minRating: Float, val maxRating: Float) {
    HIGH_SECURITY("HIGH SEC", 0.2f, 5.0f),
    LOW_SECURITY("LOW SEC", -5.0f, -0.2f),
    NULL_SECURITY("ZERO SPACE", 0.0f, 0.0f);

    companion object {
        fun fromRating(rating: Float): SecurityClass {
            return when {
                rating >= 0.2f -> HIGH_SECURITY
                rating <= -0.2f -> LOW_SECURITY
                else -> NULL_SECURITY // legacy identifier; player-facing term for 0.0 is Zero Space
            }
        }
    }
}

enum class ThreatLevel(val label: String, val level: Int, val description: String) {
    MINIMAL("Threat Level I - Secure", 1, "Heavily patrolled navy presence, near-zero pirate skirmishes"),
    LOW("Threat Level II - Guarded", 2, "Station defense grid active, minor pirate interception risks"),
    MODERATE("Threat Level III - Contested", 3, "Buffer border system, frequent raider reconnaissance"),
    HIGH("Threat Level IV - Hostile", 4, "Unpatrolled low-sec rim, organized pirate ambushes"),
    EXTREME("Threat Level V - Lethal", 5, "Lawless 0.0 Zero Space, fleet warfare & drone swarms");

    companion object {
        fun fromRating(rating: Float): ThreatLevel {
            return when {
                rating >= 3.5f -> MINIMAL
                rating >= 0.5f -> LOW
                rating >= -0.2f && rating < 0.5f -> MODERATE
                rating <= -0.2f && rating > -5.0f -> HIGH
                else -> EXTREME
            }
        }
    }
}

enum class ResourceAbundance(val label: String, val yieldMultiplier: Float) {
    PRISTINE("Pristine Reserves", 1.8f),
    RICH("Rich Abundance", 1.4f),
    MODERATE("Moderate Yields", 1.0f),
    DEPLETED("Depleted Crust", 0.6f)
}

data class SystemResource(
    val resourceName: String,
    val resourceCategory: String, // "Heavy Metals", "Volatiles & Fuel", "Rare Rare Earths", "Radioactives"
    val abundance: ResourceAbundance,
    val yieldScore: Int // 0 - 100
)

enum class SovereigntyType(val label: String) {
    NPC_FACTION("National Sovereign"),
    UNCLAIMED("Unclaimed Space"),
    PLAYER_GUILD("Guild Territory"),
    PLAYER_ALLIANCE("Alliance Territory"),
    NPC_NULLSEC_ENTITY("Zero Space Entity")
}

enum class StarLaneType(val label: String, val speedMultiplier: Float) {
    MAJOR_INTERFACTION_TRUNK("Inter-Faction Trunk Gate", 1.5f),
    REGIONAL_PRIMARY("Regional Primary Lane", 1.2f),
    SECONDARY("Secondary Starlane", 1.0f),
    FRONTIER("Frontier Gate Corridor", 0.8f),
    BACKWATER("Backwater Bypass", 0.6f)
}

enum class PlanetType(
    val displayName: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    TERRESTRIAL("Terrestrial World", "Temperate crust rich in silicates, water tables & heavy metals", 0xFF2E7D32, 0xFF81C784),
    GAS_GIANT("Gas Giant", "Supermassive planetary atmosphere dense with hydrogen volatiles & fuel isotopes", 0xFFEF6C00, 0xFFFFB74D),
    ICE_GIANT("Cryo Ice Giant", "Sub-zero methane/ammonia atmosphere with deep frozen mantle deposits", 0xFF0288D1, 0xFF81D4FA),
    VOLCANIC("Volcanic World", "Hyperactive magma crust yielding rare obsidian and igneous ore veins", 0xFFC62828, 0xFFFF8A65),
    BARREN_ROCK("Barren Planetoid", "Airless celestial rock ideal for deep planetary strip mining", 0xFF78909C, 0xFFCFD8DC),
    OCEANIC("Oceanic Biosphere", "Global saline oceans harboring organic compounds and dissolved lithium", 0xFF00838F, 0xFF4DD0E1),
    METALLIC("Metallic Core", "Exposed primordial planetary core with dense titanium and tritanium", 0xFFF57F17, 0xFFFFF176),
    SHATTERED_WORLD("Shattered Core", "Fractured tectonic fragments exposing pristine raw morphite anomalies", 0xFF6A1B9A, 0xFFCE93D8)
}

data class PlanetMiningYield(
    val resourceName: String,
    val category: String, // e.g., "Heavy Metals", "Volatiles & Fuel", "Precious Ore", "Radioactives"
    val yieldPercentage: Int, // 0 - 100%
    val estimatedTonsPerHour: Int, // e.g. 450 tons/hr
    val extractionDifficulty: String, // "Low Risk", "Moderate", "Hazardous", "Extreme"
    val abundance: ResourceAbundance
)

data class CelestialBody(
    val id: String,
    val name: String,
    val type: PlanetType,
    val orbitalRadiusAu: Float, // e.g., 0.4 AU, 1.2 AU, 5.2 AU
    val radiusKm: Int, // e.g., 6371 km
    val surfaceHazards: String, // "High Radiation", "Superheated Atmosphere", "Stable Orbit", "Violent Ion Storms"
    val miningYields: List<PlanetMiningYield>,
    val activeExtractionFacilities: Int = 1,
    val surveyQualityPercentage: Int = 95
)

data class StarLaneConnection(
    val targetSystemId: String,
    val distanceLy: Float,
    val laneType: StarLaneType
)

data class SystemNode(
    val systemId: String,
    val name: String,
    val regionId: String,
    val regionName: String,
    val securityRating: Float, // e.g. +3.2, -1.5, 0.0
    val securityClass: SecurityClass = SecurityClass.fromRating(securityRating),
    val threatLevel: ThreatLevel = ThreatLevel.fromRating(securityRating),
    val overallAbundance: ResourceAbundance = ResourceAbundance.RICH,
    val resources: List<SystemResource> = emptyList(),
    val asteroidBelts: Int = 3,
    val celestialCount: Int = 8,
    val threatDescription: String = threatLevel.description,
    val resourceSummary: String = "Rich ore deposits with high concentration of industrial metals.",
    val sovereigntyType: SovereigntyType,
    val sovereignId: String,
    val sovereignName: String,
    val isDiscovered: Boolean = true,
    val isCharted: Boolean = true,
    val isCapital: Boolean = false,
    val isCommerceHub: Boolean = false,
    val isBorderGateway: Boolean = false,
    val mapX: Float, // Normalized coordinates for 2D galaxy presentation
    val mapY: Float,
    val connections: List<StarLaneConnection> = emptyList(),
    val primaryStationName: String = "Orbital Starbase",
    val celestialBodies: List<CelestialBody> = emptyList()
)

enum class RouteOptimizationMode(val label: String, val description: String) {
    FASTEST("Fastest Route", "Fewest Gate jumps across the starlane network"),
    SAFEST("Safest Route", "Prioritizes high-security and heavily patrolled systems"),
    HIGH_SEC_ONLY("High Sec Only", "Strictly avoids Low Security and Zero Space systems"),
    AVOID_LOW_SEC("Avoid Low-Sec", "Reroutes around pirate and unpatrolled faction low-sec"),
    AVOID_NULLSEC("Avoid Zero Space", "Avoids all 0.0 Zero Space systems"),
    TRADE_ROUTE("Trade Corridor", "Favors inter-faction trunk lanes and commerce hubs")
}

data class RouteSegment(
    val fromSystem: SystemNode,
    val toSystem: SystemNode,
    val laneType: StarLaneType,
    val distanceLy: Float
)

data class RoutePlanResult(
    val origin: SystemNode,
    val destination: SystemNode,
    val segments: List<RouteSegment>,
    val totalJumps: Int,
    val totalDistanceLy: Float,
    val lowestSecurityRating: Float,
    val sovereigntyTransitions: List<String>,
    val warnings: List<String>,
    val modeUsed: RouteOptimizationMode
)

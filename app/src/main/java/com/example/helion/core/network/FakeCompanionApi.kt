package com.example.helion.core.network

import com.example.helion.core.model.CelestialBody
import com.example.helion.core.model.CommanderLicense
import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.model.CommodityCategory
import com.example.helion.core.model.CommsConversation
import com.example.helion.core.model.ConversationType
import com.example.helion.core.model.DeliveryState
import com.example.helion.core.model.FactionStanding
import com.example.helion.core.model.GalNetArticle
import com.example.helion.core.model.GalNetChannel
import com.example.helion.core.model.GuildInfo
import com.example.helion.core.model.GuildMember
import com.example.helion.core.model.GuildNotice
import com.example.helion.core.model.GuildTerritory
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.LiveryOption
import com.example.helion.core.model.MarketItem
import com.example.helion.core.model.MarketTransactionRequest
import com.example.helion.core.model.MarketTransactionResult
import com.example.helion.core.model.ModuleItem
import com.example.helion.core.model.ModuleSlot
import com.example.helion.core.model.ModuleSlotCategory
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.core.model.PlanetMiningYield
import com.example.helion.core.model.PlanetType
import com.example.helion.core.model.ResourceAbundance
import com.example.helion.core.model.ShipDefinition
import com.example.helion.core.model.SovereigntyType
import com.example.helion.core.model.StarLaneConnection
import com.example.helion.core.model.StarLaneType
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.SystemResource
import com.example.helion.core.model.TacticalMission
import com.example.helion.core.model.TacticalObjective
import com.example.helion.core.model.LocationMarker
import com.example.helion.core.model.MissionCategory
import com.example.helion.core.model.MissionStatus
import com.example.helion.core.model.ObjectiveStatus
import com.example.helion.core.model.FleetTaskStatus
import com.example.helion.core.model.ThreatLevel
import com.example.helion.core.model.UniverseMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class FakeCompanionApi : CompanionApi {

    private val mutex = Mutex()

    private var activeEnvironment = HelionEnvironment.PRODUCTION

    // Server authoritative state
    private var commanderCredits: Long = 42_580L
    private var activeShipInstanceId = "ship-raptor-01"
    private var activeStationId = "sta-kepler-prime"
    private var activeSystemId = "sys-kepler"

    // Livery options
    val liveryCatalog = listOf(
        LiveryOption("liv-tactical-cyan", "Tactical Cyan (Standard)", "#00E5FF", "#111622", "Matte Composite"),
        LiveryOption("liv-stealth-void", "Void Ops Shadow", "#1E293B", "#334155", "Anti-Radar Carbon"),
        LiveryOption("liv-solaris-gold", "Directorate Prestige", "#FFD700", "#182032", "Ceramic Lacquer"),
        LiveryOption("liv-hazard-stripe", "Industrial Haz-Amber", "#FF9100", "#212529", "Safety Chevrons")
    )

    // Module Catalog
    val moduleCatalog = listOf(
        ModuleItem(
            moduleId = "mod-pulse-laser-2",
            displayName = "Helios Pulse Laser Mk II",
            manufacturer = "Aurelia Armaments",
            category = ModuleSlotCategory.WEAPON_HARDPOINT,
            size = 2,
            grade = "A",
            purchasePrice = 8500L,
            massTons = 4.0f,
            powerDrawMw = 1.25f,
            integrity = 120,
            statBoostSummary = "+18% Thermal Shield Damage"
        ),
        ModuleItem(
            moduleId = "mod-railgun-2",
            displayName = "Hyper-Velocity Railgun",
            manufacturer = "Titan Forge Ordnance",
            category = ModuleSlotCategory.WEAPON_HARDPOINT,
            size = 2,
            grade = "B",
            purchasePrice = 14200L,
            massTons = 6.5f,
            powerDrawMw = 2.10f,
            integrity = 95,
            statBoostSummary = "+45% Kinetic Armor Piercing"
        ),
        ModuleItem(
            moduleId = "mod-chaff-1",
            displayName = "Automated Chaff Launcher",
            manufacturer = "Vantage Defensive Systems",
            category = ModuleSlotCategory.UTILITY,
            size = 1,
            grade = "C",
            purchasePrice = 2100L,
            massTons = 1.5f,
            powerDrawMw = 0.40f,
            integrity = 80,
            statBoostSummary = "Gimbal/Turret Lock Disruption"
        ),
        ModuleItem(
            moduleId = "mod-shield-booster-1",
            displayName = "Aegis Shield Booster",
            manufacturer = "Compact Dynamics",
            category = ModuleSlotCategory.UTILITY,
            size = 1,
            grade = "A",
            purchasePrice = 9800L,
            massTons = 2.0f,
            powerDrawMw = 1.20f,
            integrity = 100,
            statBoostSummary = "+20% Shield Capacity"
        ),
        ModuleItem(
            moduleId = "mod-overcharged-core-3",
            displayName = "Overcharged Fusion Core 3A",
            manufacturer = "Aster Dynamics",
            category = ModuleSlotCategory.CORE_INTERNAL,
            size = 3,
            grade = "A",
            purchasePrice = 32000L,
            massTons = 5.0f,
            powerDrawMw = 0.0f, // Generator
            integrity = 140,
            statBoostSummary = "+22.5 MW Stable Power Output"
        ),
        ModuleItem(
            moduleId = "mod-warp-drive-3a",
            displayName = "Sub-Space Warp Drive 3A",
            manufacturer = "Crossroads Propulsion",
            category = ModuleSlotCategory.CORE_INTERNAL,
            size = 3,
            grade = "A",
            purchasePrice = 28000L,
            massTons = 5.0f,
            powerDrawMw = 1.8f,
            integrity = 110,
            statBoostSummary = "+4.2 LY Jump Range"
        ),
        ModuleItem(
            moduleId = "mod-cargo-rack-3",
            displayName = "Reinforced Cargo Container 3E",
            manufacturer = "Titan Forge Freight",
            category = ModuleSlotCategory.OPTIONAL_INTERNAL,
            size = 3,
            grade = "E",
            purchasePrice = 3400L,
            massTons = 2.0f,
            powerDrawMw = 0.20f,
            integrity = 90,
            statBoostSummary = "+16 Cargo Capacity Units"
        ),
        ModuleItem(
            moduleId = "mod-shield-gen-3a",
            displayName = "Bi-Weave Shield Generator 3A",
            manufacturer = "Solari Avionics",
            category = ModuleSlotCategory.OPTIONAL_INTERNAL,
            size = 3,
            grade = "A",
            purchasePrice = 24500L,
            massTons = 4.0f,
            powerDrawMw = 2.40f,
            integrity = 130,
            statBoostSummary = "+140 MJ Shielding / Fast Regen"
        )
    )

    // Ship Definitions
    val raptorDefinition = ShipDefinition(
        hullId = "hull-aster-raptor",
        hullName = "Aster Raptor",
        manufacturer = "Aster Dynamics",
        shipClass = "Interceptor",
        role = "Combat Escort & Fast Recon",
        baseMassTons = 45.0f,
        baseCargoCapacity = 16,
        basePowerOutputMw = 18.0f,
        baseSpeedMs = 380,
        baseShieldRating = 210,
        baseArmorRating = 180,
        maxJumpRangeLy = 22.4f,
        defaultSlots = listOf(
            ModuleSlot("slot-wpn-1", "Hardpoint Alpha", ModuleSlotCategory.WEAPON_HARDPOINT, 2),
            ModuleSlot("slot-wpn-2", "Hardpoint Beta", ModuleSlotCategory.WEAPON_HARDPOINT, 2),
            ModuleSlot("slot-util-1", "Utility Mount 1", ModuleSlotCategory.UTILITY, 1),
            ModuleSlot("slot-util-2", "Utility Mount 2", ModuleSlotCategory.UTILITY, 1),
            ModuleSlot("slot-core-pwr", "Primary Fusion Core", ModuleSlotCategory.CORE_INTERNAL, 3),
            ModuleSlot("slot-core-warp", "Sub-Space FSD/Warp", ModuleSlotCategory.CORE_INTERNAL, 3),
            ModuleSlot("slot-opt-1", "Internal Bay 1", ModuleSlotCategory.OPTIONAL_INTERNAL, 3),
            ModuleSlot("slot-opt-2", "Internal Bay 2", ModuleSlotCategory.OPTIONAL_INTERNAL, 2)
        )
    )

    val muleDefinition = ShipDefinition(
        hullId = "hull-titan-mule",
        hullName = "Titan Mule",
        manufacturer = "Titan Forge",
        shipClass = "Freighter",
        role = "Heavy Bulk Cargo & Ore Transport",
        baseMassTons = 120.0f,
        baseCargoCapacity = 128,
        basePowerOutputMw = 24.0f,
        baseSpeedMs = 180,
        baseShieldRating = 150,
        baseArmorRating = 340,
        maxJumpRangeLy = 16.8f,
        defaultSlots = listOf(
            ModuleSlot("slot-mule-wpn-1", "Hardpoint Turret", ModuleSlotCategory.WEAPON_HARDPOINT, 1),
            ModuleSlot("slot-mule-util-1", "Countermeasure Mount", ModuleSlotCategory.UTILITY, 1),
            ModuleSlot("slot-mule-core-pwr", "Industrial Generator", ModuleSlotCategory.CORE_INTERNAL, 4),
            ModuleSlot("slot-mule-core-warp", "Heavy Warp Engine", ModuleSlotCategory.CORE_INTERNAL, 4),
            ModuleSlot("slot-mule-opt-1", "Bulk Cargo Bay A", ModuleSlotCategory.OPTIONAL_INTERNAL, 4),
            ModuleSlot("slot-mule-opt-2", "Bulk Cargo Bay B", ModuleSlotCategory.OPTIONAL_INTERNAL, 4)
        )
    )

    val valiantDefinition = ShipDefinition(
        hullId = "hull-commonwealth-valiant",
        hullName = "Commonwealth Valiant",
        manufacturer = "Commonwealth Shipyards",
        shipClass = "Corvette",
        role = "System Patrol & Multi-Role Flagship",
        baseMassTons = 210.0f,
        baseCargoCapacity = 48,
        basePowerOutputMw = 34.0f,
        baseSpeedMs = 260,
        baseShieldRating = 420,
        baseArmorRating = 480,
        maxJumpRangeLy = 26.2f,
        defaultSlots = listOf(
            ModuleSlot("slot-val-wpn-1", "Heavy Dorsal Battery", ModuleSlotCategory.WEAPON_HARDPOINT, 3),
            ModuleSlot("slot-val-wpn-2", "Ventral Rail Turret", ModuleSlotCategory.WEAPON_HARDPOINT, 3),
            ModuleSlot("slot-val-util-1", "ECM Suite", ModuleSlotCategory.UTILITY, 2),
            ModuleSlot("slot-val-core-pwr", "Military Reactor", ModuleSlotCategory.CORE_INTERNAL, 5),
            ModuleSlot("slot-val-core-warp", "Mil-Spec Warp Core", ModuleSlotCategory.CORE_INTERNAL, 5),
            ModuleSlot("slot-val-opt-1", "Modular Bay 1", ModuleSlotCategory.OPTIONAL_INTERNAL, 4)
        )
    )

    // Owned Ships
    private val ownedShips = mutableListOf(
        OwnedShipInstance(
            instanceId = "ship-raptor-01",
            shipName = "Nightfall",
            hullDefinition = raptorDefinition,
            currentLocationSystemId = "sys-kepler",
            currentLocationSystemName = "Kepler",
            currentLocationStationName = "Kepler Prime Orbital",
            isActiveShip = true,
            hullConditionPercent = 94.5f,
            fuelPercent = 88.0f,
            currentCargoTons = 8,
            wearPercent = 12.0f,
            slots = listOf(
                ModuleSlot("slot-wpn-1", "Hardpoint Alpha", ModuleSlotCategory.WEAPON_HARDPOINT, 2, moduleCatalog[0]),
                ModuleSlot("slot-wpn-2", "Hardpoint Beta", ModuleSlotCategory.WEAPON_HARDPOINT, 2, moduleCatalog[1]),
                ModuleSlot("slot-util-1", "Utility Mount 1", ModuleSlotCategory.UTILITY, 1, moduleCatalog[2]),
                ModuleSlot("slot-util-2", "Utility Mount 2", ModuleSlotCategory.UTILITY, 1, moduleCatalog[3]),
                ModuleSlot("slot-core-pwr", "Primary Fusion Core", ModuleSlotCategory.CORE_INTERNAL, 3, moduleCatalog[4]),
                ModuleSlot("slot-core-warp", "Sub-Space FSD/Warp", ModuleSlotCategory.CORE_INTERNAL, 3, moduleCatalog[5]),
                ModuleSlot("slot-opt-1", "Internal Bay 1", ModuleSlotCategory.OPTIONAL_INTERNAL, 3, moduleCatalog[7]),
                ModuleSlot("slot-opt-2", "Internal Bay 2", ModuleSlotCategory.OPTIONAL_INTERNAL, 2, moduleCatalog[6])
            ),
            currentLivery = liveryCatalog[0],
            registrationMark = "HLN-092-XR"
        ),
        OwnedShipInstance(
            instanceId = "ship-mule-02",
            shipName = "Gilded Ox",
            hullDefinition = muleDefinition,
            currentLocationSystemId = "sys-harrow",
            currentLocationSystemName = "Harrow",
            currentLocationStationName = "Harrow Deep Foundry",
            isActiveShip = false,
            hullConditionPercent = 100.0f,
            fuelPercent = 100.0f,
            currentCargoTons = 0,
            wearPercent = 4.0f,
            slots = muleDefinition.defaultSlots,
            currentLivery = liveryCatalog[3],
            registrationMark = "HLN-441-TF"
        ),
        OwnedShipInstance(
            instanceId = "ship-valiant-03",
            shipName = "Aegis Vanguard",
            hullDefinition = valiantDefinition,
            currentLocationSystemId = "sys-crossroads",
            currentLocationSystemName = "Crossroads",
            currentLocationStationName = "Crossroads Central Citadel",
            isActiveShip = false,
            hullConditionPercent = 82.0f,
            fuelPercent = 65.0f,
            currentCargoTons = 0,
            wearPercent = 25.0f,
            slots = valiantDefinition.defaultSlots,
            currentLivery = liveryCatalog[1],
            registrationMark = "HLN-880-CV"
        )
    )

    private fun buildSystemCelestialBodies(systemId: String): List<CelestialBody> {
        return when (systemId) {
            "sys-kepler" -> listOf(
                CelestialBody(
                    id = "kep-1",
                    name = "Kepler I (Hephaestus)",
                    type = PlanetType.VOLCANIC,
                    orbitalRadiusAu = 0.42f,
                    radiusKm = 4820,
                    surfaceHazards = "Active Pyroclastic Magma • 780°C",
                    miningYields = listOf(
                        PlanetMiningYield("Pyroxene Crystals", "Minerals", 78, 380, "Hazardous", ResourceAbundance.RICH),
                        PlanetMiningYield("Obsidian Silicates", "Industrial", 65, 290, "Moderate", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 2,
                    surveyQualityPercentage = 94
                ),
                CelestialBody(
                    id = "kep-2",
                    name = "Kepler Prime (Gaea Minor)",
                    type = PlanetType.TERRESTRIAL,
                    orbitalRadiusAu = 1.05f,
                    radiusKm = 6378,
                    surfaceHazards = "Temperate Biosphere • N2/O2 Atmosphere",
                    miningYields = listOf(
                        PlanetMiningYield("High-Grade Titanium Veins", "Heavy Metals", 82, 540, "Low Risk", ResourceAbundance.RICH),
                        PlanetMiningYield("Silicate Crystals", "Minerals", 75, 410, "Low Risk", ResourceAbundance.MODERATE),
                        PlanetMiningYield("Lithium Salts", "Industrial", 68, 320, "Low Risk", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 6,
                    surveyQualityPercentage = 99
                ),
                CelestialBody(
                    id = "kep-3",
                    name = "Kepler Majoris",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 4.80f,
                    radiusKm = 69911,
                    surfaceHazards = "Atmospheric Siphon Vortex • Storm Bands",
                    miningYields = listOf(
                        PlanetMiningYield("Hydrocarbon Volatiles", "Volatiles & Fuel", 94, 980, "Moderate", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Helium-3 Isotopes", "Volatiles & Fuel", 88, 720, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 4,
                    surveyQualityPercentage = 96
                ),
                CelestialBody(
                    id = "kep-4",
                    name = "Kepler Cryos",
                    type = PlanetType.ICE_GIANT,
                    orbitalRadiusAu = 9.40f,
                    radiusKm = 25362,
                    surfaceHazards = "Cryogenic Methane Blizzards • -210°C",
                    miningYields = listOf(
                        PlanetMiningYield("Sub-Space Fuel Isotopes", "Volatiles & Fuel", 86, 610, "Hazardous", ResourceAbundance.RICH),
                        PlanetMiningYield("Cryogenic Deuterium", "Volatiles & Fuel", 80, 480, "Hazardous", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 1,
                    surveyQualityPercentage = 91
                )
            )
            "sys-harrow" -> listOf(
                CelestialBody(
                    id = "har-1",
                    name = "Harrow Core (Vulcan Prime)",
                    type = PlanetType.METALLIC,
                    orbitalRadiusAu = 0.55f,
                    radiusKm = 5240,
                    surfaceHazards = "Solar Particle Radiation • Heavy Micro-crust",
                    miningYields = listOf(
                        PlanetMiningYield("Titanium Core Veins", "Heavy Metals", 92, 820, "Hazardous", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Tritanium Ore", "Industrial Metals", 88, 740, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 8,
                    surveyQualityPercentage = 97
                ),
                CelestialBody(
                    id = "har-2",
                    name = "Harrow Deep",
                    type = PlanetType.BARREN_ROCK,
                    orbitalRadiusAu = 1.60f,
                    radiusKm = 6800,
                    surfaceHazards = "Airless Tectonic Crevasses • Zero Atmosphere",
                    miningYields = listOf(
                        PlanetMiningYield("Ferric Bauxite Ore", "Minerals", 84, 590, "Low Risk", ResourceAbundance.RICH),
                        PlanetMiningYield("Heavy Tungsten Composites", "Heavy Metals", 79, 440, "Low Risk", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 5,
                    surveyQualityPercentage = 95
                ),
                CelestialBody(
                    id = "har-3",
                    name = "Harrow Colossus",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 5.60f,
                    radiusKm = 58200,
                    surfaceHazards = "Corrosive Acid Clouds • Lightning Storms",
                    miningYields = listOf(
                        PlanetMiningYield("Refinery Feedstock Gas", "Industrial", 89, 950, "Moderate", ResourceAbundance.RICH),
                        PlanetMiningYield("Liquid Xenon Gas", "Rare Earths", 76, 380, "Hazardous", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 3,
                    surveyQualityPercentage = 92
                )
            )
            "sys-vantage" -> listOf(
                CelestialBody(
                    id = "van-1",
                    name = "Vantage Prime",
                    type = PlanetType.BARREN_ROCK,
                    orbitalRadiusAu = 0.88f,
                    radiusKm = 4920,
                    surfaceHazards = "Low Gravity Crust • Solar Wind Exposure",
                    miningYields = listOf(
                        PlanetMiningYield("Bauxite Reserves", "Minerals", 76, 420, "Low Risk", ResourceAbundance.MODERATE),
                        PlanetMiningYield("Industrial Silicates", "Minerals", 70, 360, "Low Risk", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 2,
                    surveyQualityPercentage = 90
                ),
                CelestialBody(
                    id = "van-2",
                    name = "Vantage Vortex",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 3.90f,
                    radiusKm = 64500,
                    surfaceHazards = "Interstellar Hydrogen Boundary Current",
                    miningYields = listOf(
                        PlanetMiningYield("Sub-Space Fuel Isotopes", "Volatiles & Fuel", 88, 790, "Low Risk", ResourceAbundance.RICH),
                        PlanetMiningYield("Hydrogen Plasma Precursors", "Energy", 82, 610, "Low Risk", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 4,
                    surveyQualityPercentage = 94
                )
            )
            "sys-crossroads" -> listOf(
                CelestialBody(
                    id = "crs-1",
                    name = "Crossroads Central",
                    type = PlanetType.TERRESTRIAL,
                    orbitalRadiusAu = 1.12f,
                    radiusKm = 6600,
                    surfaceHazards = "Heavily Trafficked Orbital Corridor • Planetary Grid",
                    miningYields = listOf(
                        PlanetMiningYield("Silica Composites", "Manufactured", 82, 580, "Low Risk", ResourceAbundance.MODERATE),
                        PlanetMiningYield("Synthetic Rare Compounds", "Rare Earths", 78, 460, "Low Risk", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 7,
                    surveyQualityPercentage = 98
                ),
                CelestialBody(
                    id = "crs-2",
                    name = "Crossroads Ironheart",
                    type = PlanetType.METALLIC,
                    orbitalRadiusAu = 2.45f,
                    radiusKm = 5100,
                    surfaceHazards = "Exposed Metallic Bedrock • Asteroid Bombardment",
                    miningYields = listOf(
                        PlanetMiningYield("Rare Earth Elements", "Rare Earths", 91, 780, "Low Risk", ResourceAbundance.RICH),
                        PlanetMiningYield("Dense Nickel Alloy", "Heavy Metals", 86, 620, "Low Risk", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 6,
                    surveyQualityPercentage = 96
                ),
                CelestialBody(
                    id = "crs-3",
                    name = "Crossroads Mist",
                    type = PlanetType.ICE_GIANT,
                    orbitalRadiusAu = 8.10f,
                    radiusKm = 24100,
                    surfaceHazards = "Ammonia Cryo Ice Fields",
                    miningYields = listOf(
                        PlanetMiningYield("Liquid Nitrogen Salts", "Industrial", 81, 490, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 2,
                    surveyQualityPercentage = 92
                )
            )
            "sys-cinder" -> listOf(
                CelestialBody(
                    id = "cin-1",
                    name = "Cinder Hellmouth",
                    type = PlanetType.VOLCANIC,
                    orbitalRadiusAu = 0.38f,
                    radiusKm = 4100,
                    surfaceHazards = "Extreme Geothermal Radiation • Raider Ambush Zone",
                    miningYields = listOf(
                        PlanetMiningYield("Illicit Hyper-Crystals", "Radioactives", 96, 840, "Extreme", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Molten Core Heavy Slag", "Industrial", 85, 590, "Hazardous", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 1,
                    surveyQualityPercentage = 88
                ),
                CelestialBody(
                    id = "cin-2",
                    name = "Cinder Breakup",
                    type = PlanetType.SHATTERED_WORLD,
                    orbitalRadiusAu = 1.85f,
                    radiusKm = 3800,
                    surfaceHazards = "Fragmented Planetary Crust • Zero Navigational Beacons",
                    miningYields = listOf(
                        PlanetMiningYield("Dark Ore Fragments", "Heavy Metals", 92, 730, "Extreme", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Unrefined Tritanium Clustered", "Heavy Metals", 84, 580, "Hazardous", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 2,
                    surveyQualityPercentage = 84
                ),
                CelestialBody(
                    id = "cin-3",
                    name = "Cinder Goliath",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 6.20f,
                    radiusKm = 72000,
                    surfaceHazards = "High-Density Methane Shrouds • Pirate Outposts",
                    miningYields = listOf(
                        PlanetMiningYield("Smuggler Volatile Fuel", "Volatiles & Fuel", 88, 860, "Hazardous", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 1,
                    surveyQualityPercentage = 82
                )
            )
            "sys-aurelia" -> listOf(
                CelestialBody(
                    id = "aur-1",
                    name = "Aurelia Nexus (Shattered Throne)",
                    type = PlanetType.SHATTERED_WORLD,
                    orbitalRadiusAu = 1.30f,
                    radiusKm = 5900,
                    surfaceHazards = "0.0 Sovereign War Zone • Tachyon Anomalies",
                    miningYields = listOf(
                        PlanetMiningYield("Raw Morphite Veins", "Precious Ore", 99, 1100, "Extreme", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Antimatter Condensate", "Volatiles & Fuel", 97, 920, "Extreme", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Tritanium Super-Clusters", "Heavy Metals", 95, 870, "Extreme", ResourceAbundance.PRISTINE)
                    ),
                    activeExtractionFacilities = 5,
                    surveyQualityPercentage = 95
                ),
                CelestialBody(
                    id = "aur-2",
                    name = "Aurelia Sovereign Giant",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 5.80f,
                    radiusKm = 83000,
                    surfaceHazards = "Dreadnought Fleet Patrols • Heavy Graviton Storms",
                    miningYields = listOf(
                        PlanetMiningYield("Exotic Fuel Isotopes", "Volatiles & Fuel", 96, 1250, "Extreme", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Antimatter Precursors", "Energy", 93, 880, "Extreme", ResourceAbundance.PRISTINE)
                    ),
                    activeExtractionFacilities = 4,
                    surveyQualityPercentage = 93
                ),
                CelestialBody(
                    id = "aur-3",
                    name = "Aurelia Cryo Fortress",
                    type = PlanetType.ICE_GIANT,
                    orbitalRadiusAu = 12.50f,
                    radiusKm = 28900,
                    surfaceHazards = "Sub-Kelvin Absolute Cold • Automated Defense Grids",
                    miningYields = listOf(
                        PlanetMiningYield("Cryo-Superconductors", "Rare Earths", 94, 760, "Extreme", ResourceAbundance.PRISTINE)
                    ),
                    activeExtractionFacilities = 3,
                    surveyQualityPercentage = 90
                )
            )
            "sys-concordia" -> listOf(
                CelestialBody(
                    id = "con-1",
                    name = "Concordia Prime",
                    type = PlanetType.TERRESTRIAL,
                    orbitalRadiusAu = 1.00f,
                    radiusKm = 6420,
                    surfaceHazards = "Planetary Defense Shield • Megacity Sprawls",
                    miningYields = listOf(
                        PlanetMiningYield("Recycled Scrap Metals", "Industrial", 42, 260, "Low Risk", ResourceAbundance.DEPLETED),
                        PlanetMiningYield("Purified Silicates", "Minerals", 55, 310, "Low Risk", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 12,
                    surveyQualityPercentage = 100
                ),
                CelestialBody(
                    id = "con-2",
                    name = "Concordia Titan",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 6.40f,
                    radiusKm = 62000,
                    surfaceHazards = "Regulated Directorate Siphon Ring",
                    miningYields = listOf(
                        PlanetMiningYield("Solar Radiation Flux", "Energy", 84, 690, "Low Risk", ResourceAbundance.RICH),
                        PlanetMiningYield("Refined Hydrogen Feed", "Volatiles & Fuel", 79, 580, "Low Risk", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 8,
                    surveyQualityPercentage = 98
                )
            )
            "sys-solaris" -> listOf(
                CelestialBody(
                    id = "sol-1",
                    name = "Solaris Grand Corona",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 0.65f,
                    radiusKm = 74000,
                    surfaceHazards = "Extreme Thermonuclear Flux • Solar Flares",
                    miningYields = listOf(
                        PlanetMiningYield("Hyper-Plasma Fuel", "Volatiles & Fuel", 97, 1380, "Moderate", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Thermonuclear Isotopes", "Radioactives", 89, 720, "Hazardous", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 6,
                    surveyQualityPercentage = 97
                ),
                CelestialBody(
                    id = "sol-2",
                    name = "Solaris Forge",
                    type = PlanetType.METALLIC,
                    orbitalRadiusAu = 1.40f,
                    radiusKm = 5600,
                    surfaceHazards = "High Solar Irradiation • Heavy Magnetic Fields",
                    miningYields = listOf(
                        PlanetMiningYield("Heavy Tungsten Alloy", "Heavy Metals", 86, 640, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 4,
                    surveyQualityPercentage = 95
                )
            )
            "sys-meridian" -> listOf(
                CelestialBody(
                    id = "mer-1",
                    name = "Meridian Vault (Ring World)",
                    type = PlanetType.METALLIC,
                    orbitalRadiusAu = 1.55f,
                    radiusKm = 6900,
                    surfaceHazards = "Massive Dense Asteroid Rings • Debris Collisions",
                    miningYields = listOf(
                        PlanetMiningYield("Platinum Core Asteroids", "Precious Ore", 97, 850, "Moderate", ResourceAbundance.PRISTINE),
                        PlanetMiningYield("Heavy Nickel Veins", "Heavy Metals", 90, 710, "Moderate", ResourceAbundance.RICH),
                        PlanetMiningYield("Chromium Deposits", "Heavy Metals", 84, 590, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 7,
                    surveyQualityPercentage = 96
                ),
                CelestialBody(
                    id = "mer-2",
                    name = "Meridian Oceanus",
                    type = PlanetType.OCEANIC,
                    orbitalRadiusAu = 2.60f,
                    radiusKm = 7800,
                    surfaceHazards = "Global Liquid Oceans • Megastorms",
                    miningYields = listOf(
                        PlanetMiningYield("Bio-Lithium Brine", "Minerals", 85, 620, "Moderate", ResourceAbundance.RICH),
                        PlanetMiningYield("Hydrocarbon Precursors", "Volatiles & Fuel", 80, 540, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 3,
                    surveyQualityPercentage = 93
                ),
                CelestialBody(
                    id = "mer-3",
                    name = "Meridian Leviathan",
                    type = PlanetType.GAS_GIANT,
                    orbitalRadiusAu = 7.10f,
                    radiusKm = 71000,
                    surfaceHazards = "Cryogenic Jet Streams",
                    miningYields = listOf(
                        PlanetMiningYield("Sub-Space Fuel Isotopes", "Volatiles & Fuel", 89, 810, "Moderate", ResourceAbundance.RICH)
                    ),
                    activeExtractionFacilities = 2,
                    surveyQualityPercentage = 91
                )
            )
            else -> listOf(
                CelestialBody(
                    id = "$systemId-pl-1",
                    name = "Planetary Body Alpha",
                    type = PlanetType.TERRESTRIAL,
                    orbitalRadiusAu = 1.0f,
                    radiusKm = 6200,
                    surfaceHazards = "Standard Planetary Crust",
                    miningYields = listOf(
                        PlanetMiningYield("Titanium Veins", "Heavy Metals", 75, 420, "Low Risk", ResourceAbundance.MODERATE)
                    ),
                    activeExtractionFacilities = 2,
                    surveyQualityPercentage = 90
                )
            )
        }
    }

    // Universe Graph Systems (9 prominent systems with distinct security, sovereignty, starlane types)
    private val galaxySystems = mapOf(
        "sys-kepler" to SystemNode(
            systemId = "sys-kepler",
            name = "Kepler",
            regionId = "reg-solari-core",
            regionName = "Solari Core Sector",
            securityRating = 4.8f,
            threatLevel = ThreatLevel.MINIMAL,
            overallAbundance = ResourceAbundance.MODERATE,
            resources = listOf(
                SystemResource("Hydrocarbon Volatiles", "Volatiles & Fuel", ResourceAbundance.RICH, 78),
                SystemResource("Silicate Crystals", "Minerals", ResourceAbundance.MODERATE, 55)
            ),
            asteroidBelts = 2,
            celestialCount = 9,
            threatDescription = "Directorate Navy battlegroup stationed. Law enforcement response time < 30s.",
            resourceSummary = "Active gas harvesting platforms in upper atmosphere; depleted inner terrestrial crust.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-solari-dir",
            sovereignName = "Solari Directorate",
            isCapital = false,
            isCommerceHub = true,
            isBorderGateway = false,
            mapX = 0.25f,
            mapY = 0.35f,
            primaryStationName = "Kepler Prime Orbital",
            connections = listOf(
                StarLaneConnection("sys-harrow", 8.4f, StarLaneType.MAJOR_INTERFACTION_TRUNK),
                StarLaneConnection("sys-solaris", 14.2f, StarLaneType.REGIONAL_PRIMARY)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-kepler")
        ),
        "sys-harrow" to SystemNode(
            systemId = "sys-harrow",
            name = "Harrow",
            regionId = "reg-solari-core",
            regionName = "Solari Core Sector",
            securityRating = 1.8f,
            threatLevel = ThreatLevel.LOW,
            overallAbundance = ResourceAbundance.RICH,
            resources = listOf(
                SystemResource("Titanium Core Veins", "Heavy Metals", ResourceAbundance.RICH, 88),
                SystemResource("Tritanium Ore", "Industrial Metals", ResourceAbundance.RICH, 82)
            ),
            asteroidBelts = 5,
            celestialCount = 11,
            threatDescription = "Heavy industrial defense fleet. Sporadic pirate skirmishes in outer asteroid rings.",
            resourceSummary = "Dense metallic asteroid belt supplying planetary foundries and heavy shipyards.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-solari-dir",
            sovereignName = "Solari Directorate",
            isCapital = false,
            isCommerceHub = false,
            isBorderGateway = true,
            mapX = 0.38f,
            mapY = 0.42f,
            primaryStationName = "Harrow Deep Foundry",
            connections = listOf(
                StarLaneConnection("sys-kepler", 8.4f, StarLaneType.MAJOR_INTERFACTION_TRUNK),
                StarLaneConnection("sys-vantage", 11.2f, StarLaneType.MAJOR_INTERFACTION_TRUNK),
                StarLaneConnection("sys-cinder", 9.6f, StarLaneType.FRONTIER)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-harrow")
        ),
        "sys-vantage" to SystemNode(
            systemId = "sys-vantage",
            name = "Vantage",
            regionId = "reg-neutral-buffer",
            regionName = "Neutral Frontier Buffer",
            securityRating = 0.8f,
            threatLevel = ThreatLevel.LOW,
            overallAbundance = ResourceAbundance.MODERATE,
            resources = listOf(
                SystemResource("Sub-Space Fuel Isotopes", "Volatiles & Fuel", ResourceAbundance.RICH, 74),
                SystemResource("Bauxite Reserves", "Minerals", ResourceAbundance.MODERATE, 60)
            ),
            asteroidBelts = 3,
            celestialCount = 7,
            threatDescription = "Buffer zone monitored by automated defense relays. Neutral merchant escort convoys active.",
            resourceSummary = "Orbital relay tapping interstellar hydrogen streams for warp fuel synthesis.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-free-coalition",
            sovereignName = "Free Star Coalition",
            isCapital = false,
            isCommerceHub = false,
            isBorderGateway = true,
            mapX = 0.52f,
            mapY = 0.50f,
            primaryStationName = "Vantage Orbital Relay",
            connections = listOf(
                StarLaneConnection("sys-harrow", 11.2f, StarLaneType.MAJOR_INTERFACTION_TRUNK),
                StarLaneConnection("sys-crossroads", 7.8f, StarLaneType.MAJOR_INTERFACTION_TRUNK),
                StarLaneConnection("sys-concordia", 15.0f, StarLaneType.SECONDARY)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-vantage")
        ),
        "sys-crossroads" to SystemNode(
            systemId = "sys-crossroads",
            name = "Crossroads",
            regionId = "reg-neutral-buffer",
            regionName = "The Hub Worlds",
            securityRating = 3.2f,
            threatLevel = ThreatLevel.LOW,
            overallAbundance = ResourceAbundance.RICH,
            resources = listOf(
                SystemResource("Rare Earth Elements", "Rare Earths", ResourceAbundance.RICH, 85),
                SystemResource("Silica Composites", "Manufactured", ResourceAbundance.MODERATE, 68)
            ),
            asteroidBelts = 4,
            celestialCount = 8,
            threatDescription = "Citadel defense fleet patrols main gate corridor; highly secured trade hub.",
            resourceSummary = "Extensive asteroid processing stations and central galactic commodity exchange.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-free-coalition",
            sovereignName = "Free Star Coalition",
            isCapital = false,
            isCommerceHub = true,
            isBorderGateway = false,
            mapX = 0.62f,
            mapY = 0.44f,
            primaryStationName = "Crossroads Central Citadel",
            connections = listOf(
                StarLaneConnection("sys-vantage", 7.8f, StarLaneType.MAJOR_INTERFACTION_TRUNK),
                StarLaneConnection("sys-concordia", 10.4f, StarLaneType.REGIONAL_PRIMARY),
                StarLaneConnection("sys-meridian", 18.5f, StarLaneType.SECONDARY)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-crossroads")
        ),
        "sys-cinder" to SystemNode(
            systemId = "sys-cinder",
            name = "Cinder",
            regionId = "reg-outer-rim",
            regionName = "Outer Badlands",
            securityRating = -2.4f, // LOW SEC!
            threatLevel = ThreatLevel.HIGH,
            overallAbundance = ResourceAbundance.RICH,
            resources = listOf(
                SystemResource("Illicit Hyper-Crystals", "Radioactives", ResourceAbundance.PRISTINE, 92),
                SystemResource("Dark Ore Fragments", "Heavy Metals", ResourceAbundance.RICH, 80)
            ),
            asteroidBelts = 6,
            celestialCount = 14,
            threatDescription = "Hostile lawless space. Outer Rim Syndicate raiders, pirate ambushes, no sovereign navy presence.",
            resourceSummary = "Unregulated extraction fields yielding highly valuable forbidden hyper-crystals.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-outer-syndicate",
            sovereignName = "Outer Rim Syndicate",
            isCapital = false,
            isCommerceHub = false,
            isBorderGateway = false,
            mapX = 0.35f,
            mapY = 0.65f,
            primaryStationName = "Cinder Smuggler Port",
            connections = listOf(
                StarLaneConnection("sys-harrow", 9.6f, StarLaneType.FRONTIER),
                StarLaneConnection("sys-aurelia", 13.0f, StarLaneType.BACKWATER)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-cinder")
        ),
        "sys-aurelia" to SystemNode(
            systemId = "sys-aurelia",
            name = "Aurelia",
            regionId = "reg-deep-null",
            regionName = "Dark Horizon Expanse",
            securityRating = 0.0f, // 0.0 NULL SEC!
            threatLevel = ThreatLevel.EXTREME,
            overallAbundance = ResourceAbundance.PRISTINE,
            resources = listOf(
                SystemResource("Raw Morphite Veins", "Precious Ore", ResourceAbundance.PRISTINE, 98),
                SystemResource("Antimatter Condensate", "Volatiles & Fuel", ResourceAbundance.PRISTINE, 95),
                SystemResource("Tritanium Super-Clusters", "Heavy Metals", ResourceAbundance.PRISTINE, 94)
            ),
            asteroidBelts = 8,
            celestialCount = 16,
            threatDescription = "Lethal 0.0 Sovereign null-sec. Guild fleet dreadnoughts, automated pirate drone swarms, open warfare.",
            resourceSummary = "Untouched pristine deep-space asteroid fields with immense wealth of exotic materials.",
            sovereigntyType = SovereigntyType.PLAYER_GUILD,
            sovereignId = "guild-iron-vanguard",
            sovereignName = "Iron Vanguard [IVG]",
            isCapital = false,
            isCommerceHub = false,
            isBorderGateway = false,
            mapX = 0.45f,
            mapY = 0.85f,
            primaryStationName = "Vanguard Null Fortress",
            connections = listOf(
                StarLaneConnection("sys-cinder", 13.0f, StarLaneType.BACKWATER),
                StarLaneConnection("sys-meridian", 16.0f, StarLaneType.BACKWATER)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-aurelia")
        ),
        "sys-concordia" to SystemNode(
            systemId = "sys-concordia",
            name = "Concordia",
            regionId = "reg-concordat-core",
            regionName = "Concordat Expanse",
            securityRating = 4.5f,
            threatLevel = ThreatLevel.MINIMAL,
            overallAbundance = ResourceAbundance.DEPLETED,
            resources = listOf(
                SystemResource("Recycled Scrap Metals", "Industrial", ResourceAbundance.DEPLETED, 35),
                SystemResource("Solar Radiation Flux", "Energy", ResourceAbundance.RICH, 80)
            ),
            asteroidBelts = 1,
            celestialCount = 6,
            threatDescription = "Maximum security national core. Concordat high fleet flagships orbiting all gates.",
            resourceSummary = "Centuries of heavy urban extraction have depleted natural crusts; primary hub for high-tech.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-concordat",
            sovereignName = "Concordat of Worlds",
            isCapital = true,
            isCommerceHub = true,
            isBorderGateway = false,
            mapX = 0.75f,
            mapY = 0.35f,
            primaryStationName = "Concordia Prime Ring",
            connections = listOf(
                StarLaneConnection("sys-crossroads", 10.4f, StarLaneType.REGIONAL_PRIMARY),
                StarLaneConnection("sys-vantage", 15.0f, StarLaneType.SECONDARY),
                StarLaneConnection("sys-meridian", 12.1f, StarLaneType.REGIONAL_PRIMARY)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-concordia")
        ),
        "sys-solaris" to SystemNode(
            systemId = "sys-solaris",
            name = "Solaris",
            regionId = "reg-solari-core",
            regionName = "Solari Core Sector",
            securityRating = 5.0f,
            threatLevel = ThreatLevel.MINIMAL,
            overallAbundance = ResourceAbundance.RICH,
            resources = listOf(
                SystemResource("Hyper-Plasma Fuel", "Volatiles & Fuel", ResourceAbundance.PRISTINE, 94),
                SystemResource("Thermonuclear Isotopes", "Radioactives", ResourceAbundance.RICH, 82)
            ),
            asteroidBelts = 2,
            celestialCount = 5,
            threatDescription = "Directorate research garrison and heavy solar defense grid.",
            resourceSummary = "Solar corona platforms provide massive clean plasma fuel for the core systems.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-solari-dir",
            sovereignName = "Solari Directorate",
            isCapital = true,
            isCommerceHub = true,
            isBorderGateway = false,
            mapX = 0.15f,
            mapY = 0.25f,
            primaryStationName = "Solaris Grand Spire",
            connections = listOf(
                StarLaneConnection("sys-kepler", 14.2f, StarLaneType.REGIONAL_PRIMARY)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-solaris")
        ),
        "sys-meridian" to SystemNode(
            systemId = "sys-meridian",
            name = "Meridian Prime",
            regionId = "reg-concordat-core",
            regionName = "Concordat Expanse",
            securityRating = 3.8f,
            threatLevel = ThreatLevel.MODERATE,
            overallAbundance = ResourceAbundance.PRISTINE,
            resources = listOf(
                SystemResource("Platinum Core Asteroids", "Precious Ore", ResourceAbundance.PRISTINE, 96),
                SystemResource("Heavy Nickel Veins", "Heavy Metals", ResourceAbundance.RICH, 86)
            ),
            asteroidBelts = 7,
            celestialCount = 12,
            threatDescription = "Contested mining frontier. Freelance security contractors competing with rogue salvagers.",
            resourceSummary = "Vast untapped metallic planetary rings yielding pristine precious metals.",
            sovereigntyType = SovereigntyType.NPC_FACTION,
            sovereignId = "fac-concordat",
            sovereignName = "Concordat of Worlds",
            isCapital = false,
            isCommerceHub = false,
            isBorderGateway = true,
            mapX = 0.82f,
            mapY = 0.60f,
            primaryStationName = "Meridian Gateway Depot",
            connections = listOf(
                StarLaneConnection("sys-concordia", 12.1f, StarLaneType.REGIONAL_PRIMARY),
                StarLaneConnection("sys-crossroads", 18.5f, StarLaneType.SECONDARY),
                StarLaneConnection("sys-aurelia", 16.0f, StarLaneType.BACKWATER)
            ),
            celestialBodies = buildSystemCelestialBodies("sys-meridian")
        )
    )

    // Regional Market Items (Dynamic per station)
    private val marketDatabase = mutableListOf(
        // Kepler Prime Orbital (Local Station)
        MarketItem("com-refined-metals", "Refined Titanium", CommodityCategory.METALS, "tons", 642L, 610L, 420, 800, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), -14),
        MarketItem("com-superconductors", "Superconductors", CommodityCategory.TECHNOLOGY, "units", 1450L, 1380L, 180, 450, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), 35),
        MarketItem("com-bio-rations", "Nutrient Paste", CommodityCategory.CONSUMER, "crates", 110L, 95L, 1200, 1500, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), 0),
        MarketItem("com-med-supplies", "Broad-Spectrum Antivirals", CommodityCategory.MEDICAL, "cases", 890L, 820L, 95, 320, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), 12),
        MarketItem("com-plasma-cells", "Containment Plasma Cells", CommodityCategory.INDUSTRIAL, "tons", 520L, 480L, 340, 600, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), -5),
        MarketItem("com-tritanium-ore", "Raw Tritanium Ore", CommodityCategory.METALS, "tons", 320L, 295L, 680, 1100, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), 24),
        MarketItem("com-hyper-fuel", "Sub-Space Hyper-Fuel", CommodityCategory.INDUSTRIAL, "tons", 980L, 915L, 240, 750, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), 48),
        MarketItem("com-quantum-cores", "Quantum Logic Cores", CommodityCategory.TECHNOLOGY, "units", 3400L, 3180L, 65, 150, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), -85),
        MarketItem("com-munitions", "Proximity Rail Munitions", CommodityCategory.WEAPONRY, "cases", 760L, 710L, 310, 420, "sta-kepler-prime", "Kepler Prime Orbital", "sys-kepler", "Kepler", "reg-solari-core", System.currentTimeMillis(), 8),

        // Comparison markets for Refined Titanium
        MarketItem("com-refined-metals", "Refined Titanium", CommodityCategory.METALS, "tons", 711L, 680L, 210, 920, "sta-harrow-foundry", "Harrow Deep Foundry", "sys-harrow", "Harrow", "reg-solari-core", System.currentTimeMillis(), 18),
        MarketItem("com-refined-metals", "Refined Titanium", CommodityCategory.METALS, "tons", 785L, 750L, 110, 1400, "sta-crossroads-citadel", "Crossroads Central Citadel", "sys-crossroads", "Crossroads", "reg-neutral-buffer", System.currentTimeMillis(), 42),
        MarketItem("com-refined-metals", "Refined Titanium", CommodityCategory.METALS, "tons", 836L, 810L, 60, 1800, "sta-cinder-smuggler", "Cinder Smuggler Port", "sys-cinder", "Cinder", "reg-outer-rim", System.currentTimeMillis(), 60),

        // Regional comparisons for Superconductors
        MarketItem("com-superconductors", "Superconductors", CommodityCategory.TECHNOLOGY, "units", 1720L, 1650L, 45, 600, "sta-cinder-smuggler", "Cinder Smuggler Port", "sys-cinder", "Cinder", "reg-outer-rim", System.currentTimeMillis(), 85),
        MarketItem("com-superconductors", "Superconductors", CommodityCategory.TECHNOLOGY, "units", 1580L, 1510L, 90, 520, "sta-crossroads-citadel", "Crossroads Central Citadel", "sys-crossroads", "Crossroads", "reg-neutral-buffer", System.currentTimeMillis(), 20),

        // Regional comparisons for Tritanium Ore
        MarketItem("com-tritanium-ore", "Raw Tritanium Ore", CommodityCategory.METALS, "tons", 440L, 415L, 180, 950, "sta-harrow-foundry", "Harrow Deep Foundry", "sys-harrow", "Harrow", "reg-solari-core", System.currentTimeMillis(), 30),
        MarketItem("com-tritanium-ore", "Raw Tritanium Ore", CommodityCategory.METALS, "tons", 510L, 485L, 95, 1200, "sta-cinder-smuggler", "Cinder Smuggler Port", "sys-cinder", "Cinder", "reg-outer-rim", System.currentTimeMillis(), 55),

        // Regional comparisons for Hyper-Fuel
        MarketItem("com-hyper-fuel", "Sub-Space Hyper-Fuel", CommodityCategory.INDUSTRIAL, "tons", 1150L, 1080L, 120, 600, "sta-vantage-relay", "Vantage Orbital Relay", "sys-vantage", "Vantage", "reg-neutral-buffer", System.currentTimeMillis(), 65),
        MarketItem("com-hyper-fuel", "Sub-Space Hyper-Fuel", CommodityCategory.INDUSTRIAL, "tons", 1280L, 1210L, 80, 800, "sta-cinder-smuggler", "Cinder Smuggler Port", "sys-cinder", "Cinder", "reg-outer-rim", System.currentTimeMillis(), 90),

        // Regional comparisons for Nutrient Paste
        MarketItem("com-bio-rations", "Nutrient Paste", CommodityCategory.CONSUMER, "crates", 185L, 160L, 350, 2400, "sta-cinder-smuggler", "Cinder Smuggler Port", "sys-cinder", "Cinder", "reg-outer-rim", System.currentTimeMillis(), 20)
    )

    // GalNet Articles
    private val galNetArticles = mutableListOf(
        GalNetArticle(
            articleId = "art-001",
            headline = "Starlane Disruption Averted at Vantage Gateway",
            summary = "Solari Directorate naval patrols intercepted unauthorized electronic countermeasures near the Vantage jump beacon.",
            body = "VANTAGE SYSTEM - Commercial haulers experienced momentary telemetry latency this morning following an attempted interdiction of an automated cargo convoy. Directorate heavy patrol vessels engaged unidentified hostiles near Gate 4, dispersing pirate raiders without structural casualties. Regional security ratings remain steady at +0.8, with authorities cautioning unescorted traders traveling beyond the buffer zone.",
            category = GalNetChannel.SECURITY,
            publishedAtEpoch = System.currentTimeMillis() - 3600000L * 2,
            importance = "FLASH",
            isRead = false,
            isSaved = true
        ),
        GalNetArticle(
            articleId = "art-002",
            headline = "Refined Titanium Surges Across Crossroads Markets",
            summary = "Industrial expansion in the neutral buffer zone has driven titanium demand to record highs of 785 CR/ton.",
            body = "CROSSROADS CITADEL - The Commerce Guild reported a 14% week-over-week spike in metal intake. Orbital shipwrights preparing new exploration escorts are paying premium margins for high-purity titanium. Traders operating out of Kepler have reported gross arbitrage spreads exceeding 140 CR per haul.",
            category = GalNetChannel.ECONOMY,
            publishedAtEpoch = System.currentTimeMillis() - 3600000L * 5,
            importance = "STANDARD",
            isRead = false,
            isSaved = false
        ),
        GalNetArticle(
            articleId = "art-003",
            headline = "Iron Vanguard Fortifies 0.0 Outpost in Aurelia",
            summary = "Player sovereign entity reports completion of a Tier-3 Star Fortress in nullsec territory.",
            body = "AURELIA DEEP EXPENSE - Guild engineers have anchored a modular sovereign citadel over Aurelia VI. The fortress provides automated repair gantry access to allied commanders and levies zero tariffs on cooperative resource refined goods.",
            category = GalNetChannel.COMMUNITY,
            publishedAtEpoch = System.currentTimeMillis() - 3600000L * 12,
            importance = "CRITICAL",
            isRead = true,
            isSaved = true
        )
    )

    // Guild Info
    private val guildInfo = GuildInfo(
        guildId = "guild-iron-vanguard",
        name = "Iron Vanguard",
        ticker = "IVG",
        description = "Industrial, combat escort, and null-sec sovereign development collective. Dedicated to safe trade lanes and mutual defense.",
        emblemIcon = "shield_chevron",
        memberCount = 142,
        allianceId = "all-helios-compact",
        allianceName = "Helios Compact Alliance",
        homeSystemId = "sys-aurelia",
        homeSystemName = "Aurelia",
        grantedPermissions = setOf(
            "guild.read",
            "guild.members.read",
            "guild.notice.create",
            "guild.comms.write"
        ), // Notice: guild.member.remove or guild.role.manage not granted!
        territories = listOf(
            GuildTerritory("sys-aurelia", "Aurelia", 0.0f, "Sovereign Control", 450_000L)
        ),
        notices = listOf(
            GuildNotice(
                noticeId = "not-01",
                title = "Titanium Hauling Escort Operation",
                body = "Convoy departs Kepler at 22:00 GalTime heading to Crossroads. Combat pilots on standby for tip-of-the-spear jump coverage.",
                authorName = "Fleet Commander Kaelen",
                authorRole = "Tactical Marshal",
                postedAtEpoch = System.currentTimeMillis() - 18000000L,
                isPinned = true
            ),
            GuildNotice(
                noticeId = "not-02",
                title = "Aurelia Citadel Gantry Upgrades Active",
                body = "Repairs and re-arming at Vanguard Outpost are now 50% discounted for all verified Vanguard members.",
                authorName = "Quartermaster Thorne",
                authorRole = "Logistics Director",
                postedAtEpoch = System.currentTimeMillis() - 86400000L,
                isPinned = false
            )
        ),
        members = listOf(
            GuildMember("mem-01", "bannaar", "Senior Pilot", "Aster Raptor", "Kepler", true, System.currentTimeMillis() - 864000000L),
            GuildMember("mem-02", "Kaelen-7", "Tactical Marshal", "Commonwealth Valiant", "Kepler", true, System.currentTimeMillis() - 2500000000L),
            GuildMember("mem-03", "Astraea", "Logistics Lead", "Titan Mule", "Harrow", true, System.currentTimeMillis() - 1400000000L),
            GuildMember("mem-04", "Voss-Tracer", "Scout Recon", "Aster Raptor", "Aurelia", false, System.currentTimeMillis() - 3600000L)
        )
    )

    // Comms Data
    private val conversations = mutableListOf(
        CommsConversation("conv-guild", "Iron Vanguard Command", ConversationType.GUILD, "Kaelen: All escorts lock Gate 2 coordinates.", System.currentTimeMillis() - 120000L, 2, "142 pilots"),
        CommsConversation("conv-sys", "Kepler Local Broadcast", ConversationType.SYSTEM, "Station Traffic: Automated customs beacon open.", System.currentTimeMillis() - 600000L, 0, "Kepler System"),
        CommsConversation("conv-kaelen", "Commander Kaelen", ConversationType.DIRECT, "Check out the loadout balance on that Raptor.", System.currentTimeMillis() - 1800000L, 0, "Direct (Encrypted)"),
        CommsConversation("conv-trade", "Crossroads Trade Band", ConversationType.TRADE, "WTB 50t Refined Titanium at 790 CR!", System.currentTimeMillis() - 3600000L, 0, "Commercial Feed")
    )

    private val messagesMap = mutableMapOf(
        "conv-guild" to mutableListOf(
            UniverseMessage("msg-1", "conv-guild", "mem-02", "Kaelen-7", "VANGUARD-1", System.currentTimeMillis() - 300000L, "Traders, form up at Kepler Gate Alpha."),
            UniverseMessage("msg-2", "conv-guild", "mem-03", "Astraea", "HAULER-9", System.currentTimeMillis() - 240000L, "Mule cargo holds full of Titanium. Ready on your mark."),
            UniverseMessage("msg-3", "conv-guild", "mem-02", "Kaelen-7", "VANGUARD-1", System.currentTimeMillis() - 120000L, "All escorts lock Gate 2 coordinates.")
        ),
        "conv-kaelen" to mutableListOf(
            UniverseMessage("msg-4", "conv-kaelen", "mem-02", "Kaelen-7", "VANGUARD-1", System.currentTimeMillis() - 2400000L, "Hail bannaar. You refitted your pulse lasers?"),
            UniverseMessage("msg-5", "conv-kaelen", "cmd-bannaar", "bannaar", "COMMANDER", System.currentTimeMillis() - 2100000L, "Running Mk II Helios lasers and railgun kinetic backup."),
            UniverseMessage("msg-6", "conv-kaelen", "mem-02", "Kaelen-7", "VANGUARD-1", System.currentTimeMillis() - 1800000L, "Check out the loadout balance on that Raptor.")
        )
    )

    // =========================================================================
    // API Implementations
    // =========================================================================

    override suspend fun getCurrentEnvironment(): HelionEnvironment = mutex.withLock {
        activeEnvironment
    }

    override suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit> = mutex.withLock {
        delay(150)
        activeEnvironment = env
        Result.success(Unit)
    }

    override suspend fun getCommanderProfile(): Result<CommanderProfile> = mutex.withLock {
        delay(120)
        Result.success(
            CommanderProfile(
                commanderId = "cmd-bannaar",
                displayName = "bannaar",
                callSign = "HEL-772",
                credits = commanderCredits,
                xp = 18_420L,
                rank = "Lieutenant Commander (Grade IV)",
                career = "Vanguard Outrider & Combat Hauler",
                currentSystemId = activeSystemId,
                currentSystemName = "Kepler",
                currentStationId = activeStationId,
                currentStationName = "Kepler Prime Orbital",
                activeShipId = activeShipInstanceId,
                factionStandings = listOf(
                    FactionStanding("fac-solari-dir", "Solari Directorate", 6.8f, "Honored Vanguard"),
                    FactionStanding("fac-free-coalition", "Free Star Coalition", 3.2f, "Trusted Partner"),
                    FactionStanding("fac-concordat", "Concordat of Worlds", 1.4f, "Recognized Trader"),
                    FactionStanding("fac-outer-syndicate", "Outer Rim Syndicate", -4.2f, "Hostile Contact")
                ),
                licenses = listOf(
                    CommanderLicense("lic-deep-space", "Deep Space Navigation Endorsement", "Solari Navy", true),
                    CommanderLicense("lic-heavy-ord", "Class-3 Heavy Munitions Permit", "Directorate Ordnance Bureau", true),
                    CommanderLicense("lic-free-trade", "Free Coalition Commercial Passport", "Crossroads Trade Authority", true)
                ),
                guildId = guildInfo.guildId,
                guildTicker = guildInfo.ticker,
                allianceId = guildInfo.allianceId,
                environment = activeEnvironment,
                serverTimeEpoch = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getOwnedShips(): Result<List<OwnedShipInstance>> = mutex.withLock {
        delay(100)
        Result.success(ownedShips.toList())
    }

    override suspend fun getShipDefinition(hullId: String): Result<ShipDefinition> = mutex.withLock {
        val def = listOf(raptorDefinition, muleDefinition, valiantDefinition).find { it.hullId == hullId }
        if (def != null) Result.success(def) else Result.failure(Exception("Unknown hull ID: $hullId"))
    }

    override suspend fun getAllShipDefinitions(): Result<List<ShipDefinition>> = mutex.withLock {
        Result.success(listOf(raptorDefinition, muleDefinition, valiantDefinition))
    }

    override suspend fun getAvailableModules(): Result<List<ModuleItem>> = mutex.withLock {
        Result.success(moduleCatalog)
    }

    override suspend fun setActiveShip(shipInstanceId: String): Result<OwnedShipInstance> = mutex.withLock {
        delay(150)
        val targetIndex = ownedShips.indexOfFirst { it.instanceId == shipInstanceId }
        if (targetIndex == -1) {
            return Result.failure(Exception("Ship instance not found on server."))
        }

        // Validate station docking requirements
        val targetShip = ownedShips[targetIndex]
        if (targetShip.currentLocationStationName != "Kepler Prime Orbital") {
            return Result.failure(Exception("Cannot activate ship: Commander is docked at Kepler Prime Orbital, but '${targetShip.shipName}' is located at ${targetShip.currentLocationStationName}."))
        }

        activeShipInstanceId = shipInstanceId
        ownedShips.indices.forEach { i ->
            val s = ownedShips[i]
            ownedShips[i] = s.copy(isActiveShip = (s.instanceId == shipInstanceId))
        }

        Result.success(ownedShips[targetIndex])
    }

    override suspend fun applyLoadout(
        shipInstanceId: String,
        plannedModules: Map<String, String>
    ): Result<OwnedShipInstance> = mutex.withLock {
        delay(250)
        val shipIndex = ownedShips.indexOfFirst { it.instanceId == shipInstanceId }
        if (shipIndex == -1) return Result.failure(Exception("Ship instance not found on server."))

        val ship = ownedShips[shipIndex]

        // Server validation: Commander must be docked at a station with fitting service
        if (activeStationId.isEmpty()) {
            return Result.failure(Exception("Server validation failed: Fitting services require active docking at an outfitting station."))
        }

        // Validate modules and calculate refit cost
        var totalRefitCost = 0L
        val updatedSlots = ship.slots.map { slot ->
            val targetModuleId = plannedModules[slot.slotId]
            if (targetModuleId != null) {
                val mod = moduleCatalog.find { it.moduleId == targetModuleId }
                    ?: return Result.failure(Exception("Invalid module specification: $targetModuleId"))

                // Validate slot size & category compatibility
                if (mod.size > slot.size) {
                    return Result.failure(Exception("Slot size mismatch: ${mod.displayName} (Size ${mod.size}) exceeds slot capacity (Size ${slot.size})"))
                }
                if (mod.category != slot.category) {
                    return Result.failure(Exception("Category mismatch for slot ${slot.name}"))
                }

                if (slot.installedModule?.moduleId != mod.moduleId) {
                    totalRefitCost += mod.purchasePrice
                }
                slot.copy(installedModule = mod)
            } else {
                slot
            }
        }

        // Validate credits
        if (commanderCredits < totalRefitCost) {
            return Result.failure(Exception("Insufficient credits: Refit requires $totalRefitCost CR, but balance is $commanderCredits CR."))
        }

        commanderCredits -= totalRefitCost
        val updatedShip = ship.copy(slots = updatedSlots)
        ownedShips[shipIndex] = updatedShip

        Result.success(updatedShip)
    }

    override suspend fun requestLiveryUpdate(
        shipInstanceId: String,
        liveryId: String
    ): Result<OwnedShipInstance> = mutex.withLock {
        delay(150)
        val shipIndex = ownedShips.indexOfFirst { it.instanceId == shipInstanceId }
        if (shipIndex == -1) return Result.failure(Exception("Ship instance not found."))

        val livery = liveryCatalog.find { it.liveryId == liveryId }
            ?: return Result.failure(Exception("Unknown livery ID: $liveryId"))

        val updated = ownedShips[shipIndex].copy(currentLivery = livery)
        ownedShips[shipIndex] = updated
        Result.success(updated)
    }

    override suspend fun performShipMaintenance(shipInstanceId: String): Result<OwnedShipInstance> = mutex.withLock {
        delay(120)
        val shipIndex = ownedShips.indexOfFirst { it.instanceId == shipInstanceId }
        if (shipIndex == -1) return Result.failure(Exception("Ship instance not found."))

        val ship = ownedShips[shipIndex]
        val repairCost = ((100f - ship.hullConditionPercent) * 35f + ship.wearPercent * 20f).toLong().coerceAtLeast(150L)
        if (commanderCredits >= repairCost) {
            commanderCredits -= repairCost
        }

        val restored = ship.copy(
            hullConditionPercent = 100.0f,
            wearPercent = 0.0f
        )
        ownedShips[shipIndex] = restored
        Result.success(restored)
    }

    override suspend fun simulateShipWear(
        shipInstanceId: String,
        hullDamage: Float,
        wearIncrease: Float
    ): Result<OwnedShipInstance> = mutex.withLock {
        delay(80)
        val shipIndex = ownedShips.indexOfFirst { it.instanceId == shipInstanceId }
        if (shipIndex == -1) return Result.failure(Exception("Ship instance not found."))

        val ship = ownedShips[shipIndex]
        val damaged = ship.copy(
            hullConditionPercent = (ship.hullConditionPercent - hullDamage).coerceIn(10.0f, 100.0f),
            wearPercent = (ship.wearPercent + wearIncrease).coerceIn(0.0f, 100.0f)
        )
        ownedShips[shipIndex] = damaged
        Result.success(damaged)
    }

    override suspend fun getGalaxySystems(): Result<Map<String, SystemNode>> = mutex.withLock {
        delay(80)
        Result.success(galaxySystems)
    }

    override suspend fun getSystemDetails(systemId: String): Result<SystemNode> = mutex.withLock {
        val sys = galaxySystems[systemId]
        if (sys != null) Result.success(sys) else Result.failure(Exception("System $systemId not found"))
    }

    override suspend fun getMarketItems(stationId: String?): Result<List<MarketItem>> = mutex.withLock {
        delay(90)
        val targetStation = stationId ?: activeStationId
        val filtered = marketDatabase.filter { it.stationId == targetStation }
        Result.success(filtered)
    }

    override suspend fun getAllRegionalMarkets(): Result<List<MarketItem>> = mutex.withLock {
        delay(100)
        Result.success(marketDatabase.toList())
    }

    override suspend fun executeMarketTransaction(
        request: MarketTransactionRequest
    ): Result<MarketTransactionResult> = mutex.withLock {
        delay(200)
        // SERVER-AUTHORITATIVE VALIDATION
        val activeShip = ownedShips.find { it.instanceId == activeShipInstanceId }
            ?: return Result.failure(Exception("No active ship registered for commander."))

        val marketIndex = marketDatabase.indexOfFirst {
            it.stationId == request.currentStationId && it.commodityId == request.commodityId
        }
        if (marketIndex == -1) {
            return Result.failure(Exception("Commodity not found at target station market."))
        }

        val item = marketDatabase[marketIndex]
        val txId = "TX-${UUID.randomUUID().toString().take(8).uppercase()}"

        if (request.isBuyAction) {
            // Validate stock
            if (item.stockUnits < request.quantity) {
                return Result.failure(Exception("Insufficient market stock. Available: ${item.stockUnits} ${item.unit}."))
            }

            val totalCost = item.buyPrice * request.quantity
            if (commanderCredits < totalCost) {
                return Result.failure(Exception("Transaction declined: Insufficient commander balance. Total required: $totalCost CR, Available: $commanderCredits CR."))
            }

            // Validate cargo space
            val currentCargo = activeShip.currentCargoTons
            val maxCargo = activeShip.calculatedCargoCapacity
            if (currentCargo + request.quantity > maxCargo) {
                return Result.failure(Exception("Cargo bay full! Capacity: $maxCargo tons, Current: $currentCargo tons. Cannot fit ${request.quantity} tons."))
            }

            // Authoritative server mutation
            commanderCredits -= totalCost
            val newStock = item.stockUnits - request.quantity
            marketDatabase[marketIndex] = item.copy(stockUnits = newStock)

            val shipIndex = ownedShips.indexOf(activeShip)
            ownedShips[shipIndex] = activeShip.copy(currentCargoTons = currentCargo + request.quantity)

            Result.success(
                MarketTransactionResult(
                    transactionId = txId,
                    success = true,
                    authoritativeCredits = commanderCredits,
                    authoritativeCargoUnits = currentCargo + request.quantity,
                    authoritativeStock = newStock,
                    committedPricePerUnit = item.buyPrice,
                    timestampEpoch = System.currentTimeMillis()
                )
            )
        } else {
            // Sell Action
            if (activeShip.currentCargoTons < request.quantity) {
                return Result.failure(Exception("Insufficient cargo on active ship to sell ${request.quantity} ${item.unit}."))
            }

            val totalPayout = item.sellPrice * request.quantity
            commanderCredits += totalPayout
            val newStock = item.stockUnits + request.quantity
            marketDatabase[marketIndex] = item.copy(stockUnits = newStock)

            val shipIndex = ownedShips.indexOf(activeShip)
            val newCargo = activeShip.currentCargoTons - request.quantity
            ownedShips[shipIndex] = activeShip.copy(currentCargoTons = newCargo)

            Result.success(
                MarketTransactionResult(
                    transactionId = txId,
                    success = true,
                    authoritativeCredits = commanderCredits,
                    authoritativeCargoUnits = newCargo,
                    authoritativeStock = newStock,
                    committedPricePerUnit = item.sellPrice,
                    timestampEpoch = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun updateCommodityPrice(
        commodityId: String,
        newBuyPrice: Long,
        newSellPrice: Long,
        stationId: String?
    ): Result<MarketItem> = mutex.withLock {
        delay(60)
        val targetStation = stationId ?: activeStationId
        val index = marketDatabase.indexOfFirst {
            it.commodityId == commodityId && (stationId == null || it.stationId == targetStation)
        }
        if (index == -1) return Result.failure(Exception("Commodity not found in market registry."))

        val current = marketDatabase[index]
        val oldPrice = current.buyPrice
        val trendChange = if (oldPrice > 0) {
            (((newBuyPrice - oldPrice).toDouble() / oldPrice.toDouble()) * 100).toInt()
        } else 0

        val updated = current.copy(
            buyPrice = newBuyPrice,
            sellPrice = newSellPrice,
            priceChange24h = trendChange,
            lastUpdatedEpoch = System.currentTimeMillis()
        )
        marketDatabase[index] = updated
        Result.success(updated)
    }

    override suspend fun getGalNetArticles(): Result<List<GalNetArticle>> = mutex.withLock {
        delay(80)
        Result.success(galNetArticles.toList())
    }

    override suspend fun markArticleRead(articleId: String): Result<Unit> = mutex.withLock {
        val index = galNetArticles.indexOfFirst { it.articleId == articleId }
        if (index != -1) {
            galNetArticles[index] = galNetArticles[index].copy(isRead = true)
        }
        Result.success(Unit)
    }

    override suspend fun getGuildInfo(): Result<GuildInfo> = mutex.withLock {
        delay(100)
        Result.success(guildInfo)
    }

    override suspend fun postGuildNotice(title: String, body: String): Result<Unit> = mutex.withLock {
        delay(150)
        // Check permission
        if (!guildInfo.hasPermission("guild.notice.create")) {
            return Result.failure(Exception("Server Permission Denied: Lacks 'guild.notice.create' role."))
        }

        val newNotice = GuildNotice(
            noticeId = "not-${UUID.randomUUID().toString().take(6)}",
            title = title,
            body = body,
            authorName = "bannaar",
            authorRole = "Senior Pilot",
            postedAtEpoch = System.currentTimeMillis(),
            isPinned = false
        )
        // Add notice
        val updatedNotices = listOf(newNotice) + guildInfo.notices
        val updated = guildInfo.copy(notices = updatedNotices)
        // Reflection update for demo
        Result.success(Unit)
    }

    override suspend fun getConversations(): Result<List<CommsConversation>> = mutex.withLock {
        delay(80)
        Result.success(conversations.toList())
    }

    override suspend fun getMessages(conversationId: String): Result<List<UniverseMessage>> = mutex.withLock {
        delay(90)
        val msgs = messagesMap[conversationId] ?: emptyList()
        Result.success(msgs.toList())
    }

    override suspend fun sendMessage(
        conversationId: String,
        body: String
    ): Result<UniverseMessage> = mutex.withLock {
        delay(120)
        val newMsg = UniverseMessage(
            messageId = "msg-${UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId,
            senderId = "cmd-bannaar",
            senderDisplayName = "bannaar",
            senderCallSign = "COMMANDER",
            sentAtEpoch = System.currentTimeMillis(),
            body = body,
            deliveryState = DeliveryState.DELIVERED,
            isRead = true
        )

        val list = messagesMap.getOrPut(conversationId) { mutableListOf() }
        list.add(newMsg)

        // update conversation preview
        val convIndex = conversations.indexOfFirst { it.conversationId == conversationId }
        if (convIndex != -1) {
            conversations[convIndex] = conversations[convIndex].copy(
                lastMessagePreview = "bannaar: $body",
                lastMessageTimeEpoch = newMsg.sentAtEpoch
            )
        }

        Result.success(newMsg)
    }

    // --- TACTICAL MISSIONS STATE ---
    private val tacticalMissions = mutableListOf(
        TacticalMission(
            id = "mis-combat-01",
            title = "Operation Iron Hammer: Corsair Interdiction",
            briefing = "Kepler mining convoys report hostile corsair wings ambushing civilian haulers near outer asteroid grid. Deploy combat fleet wing to eliminate rogue privateers and secure the main hyperlane corridor.",
            sponsorFaction = "Terran Directorate 4th Fleet",
            category = MissionCategory.COMBAT_INTERDICTION,
            threatLevel = ThreatLevel.HIGH,
            creditReward = 280000L,
            standingReward = 25,
            bonusRewardItem = "Class-3 Pulsed Plasma Core",
            primaryLocation = LocationMarker(
                systemId = "sys-kepler",
                systemName = "Kepler Prime",
                celestialBodyName = "Kepler Majoris Orbital Grid",
                beaconCode = "NAV-884-KPL",
                coordinates = "X: +142.4 AU, Y: -68.1 AU, Z: +12.0 AU",
                securityRating = 0.8f,
                distanceLy = 0.0f,
                jumpCount = 0
            ),
            objectives = listOf(
                TacticalObjective(
                    id = "obj-101",
                    title = "Eliminate Corsair Vanguard Corvettes",
                    description = "Intercept and neutralize light attack corvettes harassing transport routes.",
                    status = ObjectiveStatus.IN_PROGRESS,
                    currentProgress = 3,
                    targetProgress = 4,
                    unit = "Corvettes Destroyed",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Kepler Majoris Asteroid Belt",
                        beaconCode = "NAV-884-KPL",
                        coordinates = "X: +142.4 AU, Y: -68.1 AU, Z: +12.0 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "1st Interceptor Squadron 'Valkyrie'"
                ),
                TacticalObjective(
                    id = "obj-102",
                    title = "Neutralize Pirate Raider Gunship 'Blood Vulture'",
                    description = "Take down the pirate flight commander vessel coordinating attacks.",
                    status = ObjectiveStatus.PENDING,
                    currentProgress = 0,
                    targetProgress = 1,
                    unit = "Flagship Neutralized",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Kepler Majoris Outer Orbit",
                        beaconCode = "GRID-04-A",
                        coordinates = "X: +146.0 AU, Y: -70.5 AU, Z: +11.2 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "Aegis Invictus (Heavy Cruiser)"
                ),
                TacticalObjective(
                    id = "obj-103",
                    title = "Deploy Nav Warning Beacon",
                    description = "Place automated threat broadcast beacon to alert civilian convoys.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 1,
                    targetProgress = 1,
                    unit = "Beacon Anchored",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Kepler High Orbital Gate",
                        beaconCode = "BEACON-KPL-01",
                        coordinates = "X: +140.1 AU, Y: -65.0 AU, Z: +12.0 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "Support Drone Delta"
                )
            ),
            assignedShipName = "Aegis Invictus (Vindicator Cruiser)",
            assignedFleetStatus = FleetTaskStatus.ENGAGING,
            assignedFleetTaskLabel = "Intercepting Outlaw Wings in Asteroid Belt",
            fleetProgressPercent = 75f,
            timeRemainingMinutes = 42,
            status = MissionStatus.ACTIVE,
            isPriorityTarget = true
        ),
        TacticalMission(
            id = "mis-mining-02",
            title = "Helium-3 Deep Siphon Harvest",
            briefing = "Kepler Majoris gas giant atmosphere is undergoing rich cyclical volatile storming. Siphon 450 metric tons of pure Helium-3 isotopes and transport extraction containers to the orbital refinery.",
            sponsorFaction = "Kepler Deep Mining Syndicate",
            category = MissionCategory.MINING_EXTRACTION,
            threatLevel = ThreatLevel.MODERATE,
            creditReward = 195000L,
            standingReward = 20,
            bonusRewardItem = "60t Refined Fuel Isotopes",
            primaryLocation = LocationMarker(
                systemId = "sys-kepler",
                systemName = "Kepler Prime",
                celestialBodyName = "Kepler Majoris (Gas Giant)",
                beaconCode = "SIPHON-02-KPL",
                coordinates = "X: +4.80 AU, Y: +12.3 AU, Z: -0.4 AU",
                securityRating = 0.8f,
                distanceLy = 0.0f,
                jumpCount = 0
            ),
            objectives = listOf(
                TacticalObjective(
                    id = "obj-201",
                    title = "Siphon High-Grade Helium-3 Volatiles",
                    description = "Engage atmospheric scoops in the upper cloud bands of Kepler Majoris.",
                    status = ObjectiveStatus.IN_PROGRESS,
                    currentProgress = 360,
                    targetProgress = 450,
                    unit = "Tons Siphoned",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Kepler Majoris Upper Cloud Deck",
                        beaconCode = "SIPHON-02-KPL",
                        coordinates = "X: +4.80 AU, Y: +12.3 AU, Z: -0.4 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "Mammoth Extractor Wing Alpha"
                ),
                TacticalObjective(
                    id = "obj-202",
                    title = "Transport Fuel Drums to Kepler Starbase",
                    description = "Dock at Orbital Starbase and offload volatile fuel canisters.",
                    status = ObjectiveStatus.PENDING,
                    currentProgress = 0,
                    targetProgress = 450,
                    unit = "Tons Offloaded",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Orbital Starbase Refinery",
                        beaconCode = "BASE-KPL-00",
                        coordinates = "X: +1.05 AU, Y: +0.0 AU, Z: +0.0 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "Mammoth Heavy Transport"
                )
            ),
            assignedShipName = "Mammoth MK-IV (Atmospheric Harvester)",
            assignedFleetStatus = FleetTaskStatus.HARVESTING,
            assignedFleetTaskLabel = "Deep Atmospheric Gas Siphon Active (80% Capacity)",
            fleetProgressPercent = 80f,
            timeRemainingMinutes = 78,
            status = MissionStatus.ACTIVE,
            isPriorityTarget = false
        ),
        TacticalMission(
            id = "mis-recon-03",
            title = "Anomalous Tachyon Frequency Scan",
            briefing = "Unidentified graviton waveforms detected originating from Harrow Core metallic crust. Orbit the body, align high-band sensor arrays, and transmit encrypted telemetry packets to GalNet.",
            sponsorFaction = "Astra Scientific Research Consortium",
            category = MissionCategory.RECON_SURVEILLANCE,
            threatLevel = ThreatLevel.LOW,
            creditReward = 140000L,
            standingReward = 15,
            bonusRewardItem = "Sub-Space Telemetry Cryptokey",
            primaryLocation = LocationMarker(
                systemId = "sys-harrow",
                systemName = "Harrow Core",
                celestialBodyName = "Harrow Core (Vulcan Prime)",
                beaconCode = "BEACON-H-CORE",
                coordinates = "X: +0.55 AU, Y: -1.2 AU, Z: +3.1 AU",
                securityRating = 0.6f,
                distanceLy = 8.4f,
                jumpCount = 1
            ),
            objectives = listOf(
                TacticalObjective(
                    id = "obj-301",
                    title = "Deploy Quantum Sensor Array",
                    description = "Position deep sensor buoys in polar geosynchronous orbit.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 1,
                    targetProgress = 1,
                    unit = "Array Anchored",
                    locationMarker = LocationMarker(
                        systemId = "sys-harrow",
                        systemName = "Harrow Core",
                        celestialBodyName = "Harrow Core Polar Orbit",
                        beaconCode = "BEACON-H-CORE",
                        coordinates = "X: +0.55 AU, Y: -1.2 AU, Z: +3.1 AU",
                        securityRating = 0.6f,
                        distanceLy = 8.4f,
                        jumpCount = 1
                    ),
                    assignedFleetUnit = "Valkyrie Recon Frigate"
                ),
                TacticalObjective(
                    id = "obj-302",
                    title = "Collect 100 Spectral Telemetry Samples",
                    description = "Record tectonic radiation and tachyon interference spikes.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 100,
                    targetProgress = 100,
                    unit = "Samples Processed",
                    locationMarker = LocationMarker(
                        systemId = "sys-harrow",
                        systemName = "Harrow Core",
                        celestialBodyName = "Harrow Core Sub-Surface Vein",
                        beaconCode = "SCAN-H-09",
                        coordinates = "X: +0.58 AU, Y: -1.1 AU, Z: +3.0 AU",
                        securityRating = 0.6f,
                        distanceLy = 8.4f,
                        jumpCount = 1
                    ),
                    assignedFleetUnit = "Valkyrie Recon Frigate"
                ),
                TacticalObjective(
                    id = "obj-303",
                    title = "Upload Encrypted Log to GalNet Node",
                    description = "Transmit collected data to orbital research uplink.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 1,
                    targetProgress = 1,
                    unit = "Telemetry Transmitted",
                    locationMarker = LocationMarker(
                        systemId = "sys-harrow",
                        systemName = "Harrow Core",
                        celestialBodyName = "Harrow Uplink Relay",
                        beaconCode = "UPLINK-HARROW",
                        coordinates = "X: +1.60 AU, Y: +0.0 AU, Z: +1.0 AU",
                        securityRating = 0.6f,
                        distanceLy = 8.4f,
                        jumpCount = 1
                    ),
                    assignedFleetUnit = "Comm Link Delta"
                )
            ),
            assignedShipName = "Valkyrie Shadow (Stealth Scout)",
            assignedFleetStatus = FleetTaskStatus.SURVEYING,
            assignedFleetTaskLabel = "Data Uplink Finalized • Ready to Claim",
            fleetProgressPercent = 100f,
            timeRemainingMinutes = 15,
            status = MissionStatus.COMPLETED,
            isPriorityTarget = false
        ),
        TacticalMission(
            id = "mis-escort-04",
            title = "Alliance Superfreighter Convoy Escort",
            briefing = "Escort superfreighter 'Goliath Dawn' carrying 1,200 tons of rare tritanium alloys through Harrow-Crossroads trunk line. Repel insurgent interceptors and maintain defensive perimeter.",
            sponsorFaction = "Free Trade Logistics Consortium",
            category = MissionCategory.CARGO_ESCORT,
            threatLevel = ThreatLevel.HIGH,
            creditReward = 340000L,
            standingReward = 30,
            bonusRewardItem = "50t Pure Tritanium Plates",
            primaryLocation = LocationMarker(
                systemId = "sys-crossroads",
                systemName = "Crossroads Central",
                celestialBodyName = "Crossroads Gateway Grid",
                beaconCode = "GATE-CRS-01",
                coordinates = "X: +1.12 AU, Y: +8.4 AU, Z: +0.2 AU",
                securityRating = 0.7f,
                distanceLy = 19.6f,
                jumpCount = 2
            ),
            objectives = listOf(
                TacticalObjective(
                    id = "obj-401",
                    title = "Form Defensive Escort Formation",
                    description = "Synchronize sub-warp telemetry with Goliath Dawn freighter.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 1,
                    targetProgress = 1,
                    unit = "Formation Synchronized",
                    locationMarker = LocationMarker(
                        systemId = "sys-vantage",
                        systemName = "Vantage Prime",
                        celestialBodyName = "Vantage Starlane Gate",
                        beaconCode = "GATE-VAN-01",
                        coordinates = "X: +0.88 AU, Y: +0.0 AU, Z: +0.0 AU",
                        securityRating = 0.7f,
                        distanceLy = 11.2f,
                        jumpCount = 1
                    ),
                    assignedFleetUnit = "Cerberus Escort Gunship"
                ),
                TacticalObjective(
                    id = "obj-402",
                    title = "Escort Freighter through 2 Star Lane Jumps",
                    description = "Transit through Harrow and Vantage relay gates safely.",
                    status = ObjectiveStatus.IN_PROGRESS,
                    currentProgress = 1,
                    targetProgress = 2,
                    unit = "Jumps Completed",
                    locationMarker = LocationMarker(
                        systemId = "sys-vantage",
                        systemName = "Vantage Prime",
                        celestialBodyName = "Crossroads Inbound Vector",
                        beaconCode = "CRS-INBOUND",
                        coordinates = "X: +2.10 AU, Y: +3.2 AU, Z: -0.1 AU",
                        securityRating = 0.7f,
                        distanceLy = 11.2f,
                        jumpCount = 1
                    ),
                    assignedFleetUnit = "Cerberus Escort Gunship"
                ),
                TacticalObjective(
                    id = "obj-403",
                    title = "Defend Against Raider Ambush",
                    description = "Destroy hostile raider wings attempting boarding actions.",
                    status = ObjectiveStatus.IN_PROGRESS,
                    currentProgress = 2,
                    targetProgress = 5,
                    unit = "Raiders Repelled",
                    locationMarker = LocationMarker(
                        systemId = "sys-crossroads",
                        systemName = "Crossroads Central",
                        celestialBodyName = "Crossroads Outskirts",
                        beaconCode = "RADAR-CRS-08",
                        coordinates = "X: +1.12 AU, Y: +8.4 AU, Z: +0.2 AU",
                        securityRating = 0.7f,
                        distanceLy = 19.6f,
                        jumpCount = 2
                    ),
                    assignedFleetUnit = "Cerberus Escort Gunship"
                )
            ),
            assignedShipName = "Cerberus Escort Gunship",
            assignedFleetStatus = FleetTaskStatus.EN_ROUTE,
            assignedFleetTaskLabel = "Cruising in Flank Escort Formation (Trunk Line)",
            fleetProgressPercent = 50f,
            timeRemainingMinutes = 110,
            status = MissionStatus.ACTIVE,
            isPriorityTarget = false
        ),
        TacticalMission(
            id = "mis-blackops-05",
            title = "Operation Phantom Courier (0.0 Space)",
            briefing = "Aurelia Nexus is an unsanctioned 0.0 sovereign lawless zone. Infiltrate the shattered planetary core fragment, extract prototype morphite nanites from abandoned laboratory vault, and exfiltrate undetected.",
            sponsorFaction = "Aurelia Shadow Syndicate",
            category = MissionCategory.COVERT_BLACK_OPS,
            threatLevel = ThreatLevel.EXTREME,
            creditReward = 520000L,
            standingReward = 40,
            bonusRewardItem = "Experimental Cloaking Baffle",
            primaryLocation = LocationMarker(
                systemId = "sys-aurelia",
                systemName = "Aurelia Nexus",
                celestialBodyName = "Aurelia Nexus (Shattered Throne)",
                beaconCode = "ANOM-AUR-00",
                coordinates = "X: +1.30 AU, Y: -14.2 AU, Z: -9.8 AU",
                securityRating = 0.0f,
                distanceLy = 31.0f,
                jumpCount = 3
            ),
            objectives = listOf(
                TacticalObjective(
                    id = "obj-501",
                    title = "Infiltrate Shattered Core Fragment",
                    description = "Slip past perimeter patrol drones using low thermal signature.",
                    status = ObjectiveStatus.PENDING,
                    currentProgress = 0,
                    targetProgress = 1,
                    unit = "Core Reached",
                    locationMarker = LocationMarker(
                        systemId = "sys-aurelia",
                        systemName = "Aurelia Nexus",
                        celestialBodyName = "Shattered Core Fragment B",
                        beaconCode = "ANOM-AUR-00",
                        coordinates = "X: +1.30 AU, Y: -14.2 AU, Z: -9.8 AU",
                        securityRating = 0.0f,
                        distanceLy = 31.0f,
                        jumpCount = 3
                    )
                ),
                TacticalObjective(
                    id = "obj-502",
                    title = "Extract 3 Encrypted Prototype Cores",
                    description = "Hack the vault mainframe and download military-grade nanite schematics.",
                    status = ObjectiveStatus.PENDING,
                    currentProgress = 0,
                    targetProgress = 3,
                    unit = "Cores Extracted",
                    locationMarker = LocationMarker(
                        systemId = "sys-aurelia",
                        systemName = "Aurelia Nexus",
                        celestialBodyName = "Sub-Surface Research Facility",
                        beaconCode = "VAULT-AUR-X",
                        coordinates = "X: +1.32 AU, Y: -14.0 AU, Z: -9.6 AU",
                        securityRating = 0.0f,
                        distanceLy = 31.0f,
                        jumpCount = 3
                    )
                )
            ),
            assignedShipName = "Specter Interceptor (Black Ops)",
            assignedFleetStatus = FleetTaskStatus.IDLE,
            assignedFleetTaskLabel = "Awaiting Commander Dispatch Orders",
            fleetProgressPercent = 0f,
            timeRemainingMinutes = 180,
            status = MissionStatus.AVAILABLE,
            isPriorityTarget = false
        ),
        TacticalMission(
            id = "mis-patrol-06",
            title = "Orbital Defense Perimeter Lockdown",
            briefing = "Patrol the high orbital perimeter around Kepler Prime orbital shipyard. Scan civilian docking transports and log contraband manifests.",
            sponsorFaction = "Kepler Defense Force",
            category = MissionCategory.COMBAT_INTERDICTION,
            threatLevel = ThreatLevel.MINIMAL,
            creditReward = 110000L,
            standingReward = 15,
            bonusRewardItem = null,
            primaryLocation = LocationMarker(
                systemId = "sys-kepler",
                systemName = "Kepler Prime",
                celestialBodyName = "Kepler Prime Shipyards",
                beaconCode = "PATROL-KPL-09",
                coordinates = "X: +1.05 AU, Y: +0.2 AU, Z: +0.1 AU",
                securityRating = 0.8f,
                distanceLy = 0.0f,
                jumpCount = 0
            ),
            objectives = listOf(
                TacticalObjective(
                    id = "obj-601",
                    title = "Inspect 10 Inbound Transport Hulls",
                    description = "Perform deep sensor scans on approaching transport vessels.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 10,
                    targetProgress = 10,
                    unit = "Transports Scanned",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Kepler Prime Approach Vector",
                        beaconCode = "PATROL-KPL-09",
                        coordinates = "X: +1.05 AU, Y: +0.2 AU, Z: +0.1 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "Vindicator Patrol Craft"
                ),
                TacticalObjective(
                    id = "obj-602",
                    title = "Intercept Unregistered Smuggler Shuttles",
                    description = "Disable drives of attempting runners.",
                    status = ObjectiveStatus.COMPLETED,
                    currentProgress = 2,
                    targetProgress = 2,
                    unit = "Shuttles Detained",
                    locationMarker = LocationMarker(
                        systemId = "sys-kepler",
                        systemName = "Kepler Prime",
                        celestialBodyName = "Kepler Low Orbit Ring",
                        beaconCode = "GRID-KPL-RING",
                        coordinates = "X: +1.02 AU, Y: +0.1 AU, Z: +0.0 AU",
                        securityRating = 0.8f,
                        distanceLy = 0.0f,
                        jumpCount = 0
                    ),
                    assignedFleetUnit = "Vindicator Patrol Craft"
                )
            ),
            assignedShipName = "Vindicator Patrol Craft",
            assignedFleetStatus = FleetTaskStatus.ORBITAL_PATROL,
            assignedFleetTaskLabel = "Station Perimeter Secured • Archived",
            fleetProgressPercent = 100f,
            timeRemainingMinutes = 0,
            status = MissionStatus.CLAIMED,
            isPriorityTarget = false
        )
    )

    override suspend fun getTacticalMissions(): Result<List<TacticalMission>> = mutex.withLock {
        delay(60)
        Result.success(tacticalMissions.toList())
    }

    override suspend fun advanceMissionObjective(
        missionId: String,
        objectiveId: String,
        increment: Int
    ): Result<TacticalMission> = mutex.withLock {
        delay(70)
        val index = tacticalMissions.indexOfFirst { it.id == missionId }
        if (index == -1) return Result.failure(IllegalArgumentException("Mission not found: $missionId"))

        val mission = tacticalMissions[index]
        val updatedObjectives = mission.objectives.map { obj ->
            if (obj.id == objectiveId) {
                val newProgress = (obj.currentProgress + increment).coerceAtMost(obj.targetProgress)
                val newStatus = if (newProgress >= obj.targetProgress) ObjectiveStatus.COMPLETED else ObjectiveStatus.IN_PROGRESS
                obj.copy(currentProgress = newProgress, status = newStatus)
            } else {
                obj
            }
        }

        val allCompleted = updatedObjectives.all { it.isCompleted }
        val newMissionStatus = if (allCompleted) MissionStatus.COMPLETED else mission.status
        val newFleetProgress = if (allCompleted) 100f else (mission.fleetProgressPercent + 15f).coerceAtMost(99f)

        val updatedMission = mission.copy(
            objectives = updatedObjectives,
            status = newMissionStatus,
            fleetProgressPercent = newFleetProgress
        )
        tacticalMissions[index] = updatedMission
        Result.success(updatedMission)
    }

    override suspend fun updateFleetTaskStatus(
        missionId: String,
        status: FleetTaskStatus,
        progressDelta: Float
    ): Result<TacticalMission> = mutex.withLock {
        delay(60)
        val index = tacticalMissions.indexOfFirst { it.id == missionId }
        if (index == -1) return Result.failure(IllegalArgumentException("Mission not found: $missionId"))

        val mission = tacticalMissions[index]
        val newProgress = (mission.fleetProgressPercent + progressDelta).coerceIn(0f, 100f)
        val newMissionStatus = if (newProgress >= 100f && mission.objectives.all { it.isCompleted }) {
            MissionStatus.COMPLETED
        } else {
            mission.status
        }

        val updated = mission.copy(
            assignedFleetStatus = status,
            fleetProgressPercent = newProgress,
            status = newMissionStatus
        )
        tacticalMissions[index] = updated
        Result.success(updated)
    }

    override suspend fun acceptMissionContract(missionId: String): Result<TacticalMission> = mutex.withLock {
        delay(80)
        val index = tacticalMissions.indexOfFirst { it.id == missionId }
        if (index == -1) return Result.failure(IllegalArgumentException("Mission not found: $missionId"))

        val mission = tacticalMissions[index]
        val updated = mission.copy(
            status = MissionStatus.ACTIVE,
            assignedFleetStatus = FleetTaskStatus.EN_ROUTE,
            assignedFleetTaskLabel = "Fleet Mobilizing to Coordinates",
            fleetProgressPercent = 10f
        )
        tacticalMissions[index] = updated
        Result.success(updated)
    }

    override suspend fun claimMissionReward(missionId: String): Result<TacticalMission> = mutex.withLock {
        delay(90)
        val index = tacticalMissions.indexOfFirst { it.id == missionId }
        if (index == -1) return Result.failure(IllegalArgumentException("Mission not found: $missionId"))

        val mission = tacticalMissions[index]
        val updated = mission.copy(
            status = MissionStatus.CLAIMED,
            assignedFleetStatus = FleetTaskStatus.IDLE,
            assignedFleetTaskLabel = "Contract Finalized • Bounty Disbursed"
        )
        tacticalMissions[index] = updated

        // Award credits to commander profile
        commanderCredits += mission.creditReward

        Result.success(updated)
    }

    override suspend fun setMissionPriority(missionId: String, isPriority: Boolean): Result<TacticalMission> = mutex.withLock {
        val index = tacticalMissions.indexOfFirst { it.id == missionId }
        if (index == -1) return Result.failure(IllegalArgumentException("Mission not found: $missionId"))

        // Clear priority from others if setting this one
        if (isPriority) {
            for (i in tacticalMissions.indices) {
                tacticalMissions[i] = tacticalMissions[i].copy(isPriorityTarget = false)
            }
        }

        val mission = tacticalMissions[index]
        val updated = mission.copy(isPriorityTarget = isPriority)
        tacticalMissions[index] = updated
        Result.success(updated)
    }

    override suspend fun abandonMission(missionId: String): Result<Unit> = mutex.withLock {
        val index = tacticalMissions.indexOfFirst { it.id == missionId }
        if (index != -1) {
            tacticalMissions[index] = tacticalMissions[index].copy(
                status = MissionStatus.AVAILABLE,
                assignedFleetStatus = FleetTaskStatus.IDLE,
                fleetProgressPercent = 0f
            )
        }
        Result.success(Unit)
    }
}

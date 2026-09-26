package com.example.helion.core.model

enum class ModuleSlotCategory {
    WEAPON_HARDPOINT,
    UTILITY,
    CORE_INTERNAL,
    OPTIONAL_INTERNAL
}

data class ModuleSlot(
    val slotId: String,
    val name: String,
    val category: ModuleSlotCategory,
    val size: Int,
    val installedModule: ModuleItem? = null
)

data class ModuleItem(
    val moduleId: String,
    val displayName: String,
    val manufacturer: String,
    val category: ModuleSlotCategory,
    val size: Int,
    val grade: String, // 'A', 'B', 'C', 'D', 'E'
    val purchasePrice: Long,
    val massTons: Float,
    val powerDrawMw: Float,
    val integrity: Int,
    val legalStatus: String = "Clean",
    val description: String = "",
    val statBoostSummary: String = ""
)

data class ShipDefinition(
    val hullId: String,
    val hullName: String,
    val manufacturer: String,
    val shipClass: String, // Interceptor, Freighter, Corvette, Explorer
    val role: String,
    val baseMassTons: Float,
    val baseCargoCapacity: Int,
    val basePowerOutputMw: Float,
    val baseSpeedMs: Int,
    val baseShieldRating: Int,
    val baseArmorRating: Int,
    val maxJumpRangeLy: Float,
    val defaultSlots: List<ModuleSlot>
)

data class LiveryOption(
    val liveryId: String,
    val name: String,
    val primaryColorHex: String,
    val accentColorHex: String,
    val patternType: String,
    val isGuildInsigniaAllowed: Boolean = true
)

data class OwnedShipInstance(
    val instanceId: String,
    val shipName: String,
    val hullDefinition: ShipDefinition,
    val currentLocationSystemId: String,
    val currentLocationSystemName: String,
    val currentLocationStationName: String,
    val isActiveShip: Boolean,
    val hullConditionPercent: Float, // 0 - 100
    val fuelPercent: Float,
    val currentCargoTons: Int,
    val wearPercent: Float,
    val slots: List<ModuleSlot>,
    val currentLivery: LiveryOption,
    val registrationMark: String
) {
    // Calculated live metrics based on installed modules
    val totalMassTons: Float
        get() = hullDefinition.baseMassTons + slots.mapNotNull { it.installedModule?.massTons }.sum()

    val totalPowerDrawMw: Float
        get() = slots.mapNotNull { it.installedModule?.powerDrawMw }.sum()

    val isPowerOverloaded: Boolean
        get() = totalPowerDrawMw > hullDefinition.basePowerOutputMw

    val calculatedCargoCapacity: Int
        get() = hullDefinition.baseCargoCapacity + (slots.filter {
            it.installedModule?.displayName?.contains("Cargo", ignoreCase = true) == true
        }.size * 16)

    val requiresMaintenance: Boolean
        get() = hullConditionPercent < 80.0f || wearPercent >= 35.0f

    val maintenanceStatusSummary: String
        get() = when {
            hullConditionPercent < 70.0f -> "CRITICAL: Hull breaches detected (${String.format("%.1f", hullConditionPercent)}%)"
            hullConditionPercent < 80.0f -> "WARNING: Hull degradation (${String.format("%.1f", hullConditionPercent)}%)"
            wearPercent >= 50.0f -> "CRITICAL: Severe component wear (${String.format("%.1f", wearPercent)}%)"
            wearPercent >= 35.0f -> "WARNING: Moderate component wear (${String.format("%.1f", wearPercent)}%)"
            else -> "NOMINAL: Systems operational"
        }
}

data class SavedLoadoutPlan(
    val planId: String,
    val name: String,
    val hullId: String,
    val hullName: String,
    val plannedModules: Map<String, String>, // slotId -> moduleId
    val createdAtEpoch: Long,
    val isFavorite: Boolean = false
)

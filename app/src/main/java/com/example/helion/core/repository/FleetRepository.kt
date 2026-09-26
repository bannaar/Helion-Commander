package com.example.helion.core.repository

import com.example.helion.core.database.LoadoutPlanDao
import com.example.helion.core.database.SavedLoadoutPlanEntity
import com.example.helion.core.model.ModuleItem
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.core.model.SavedLoadoutPlan
import com.example.helion.core.model.ShipDefinition
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.UUID

class FleetRepository(
    private val api: CompanionApi,
    private val loadoutPlanDao: LoadoutPlanDao
) {
    private val _ownedShips = MutableStateFlow<List<OwnedShipInstance>>(emptyList())
    val ownedShips: StateFlow<List<OwnedShipInstance>> = _ownedShips.asStateFlow()

    suspend fun refreshFleet(): Result<List<OwnedShipInstance>> {
        val res = api.getOwnedShips()
        res.onSuccess { _ownedShips.value = it }
        return res
    }

    suspend fun getAvailableModules(): Result<List<ModuleItem>> = api.getAvailableModules()

    suspend fun getAllShipDefinitions(): Result<List<ShipDefinition>> = api.getAllShipDefinitions()

    suspend fun setActiveShip(instanceId: String): Result<OwnedShipInstance> {
        val result = api.setActiveShip(instanceId)
        result.onSuccess { refreshFleet() }
        return result
    }

    suspend fun applyLoadoutToServer(
        shipInstanceId: String,
        plannedModules: Map<String, String>
    ): Result<OwnedShipInstance> {
        val res = api.applyLoadout(shipInstanceId, plannedModules)
        res.onSuccess { refreshFleet() }
        return res
    }

    suspend fun requestLiveryUpdate(shipInstanceId: String, liveryId: String): Result<OwnedShipInstance> {
        val res = api.requestLiveryUpdate(shipInstanceId, liveryId)
        res.onSuccess { refreshFleet() }
        return res
    }

    suspend fun performShipMaintenance(shipInstanceId: String): Result<OwnedShipInstance> {
        val res = api.performShipMaintenance(shipInstanceId)
        res.onSuccess { refreshFleet() }
        return res
    }

    suspend fun simulateShipWear(shipInstanceId: String, hullDamage: Float, wearIncrease: Float): Result<OwnedShipInstance> {
        val res = api.simulateShipWear(shipInstanceId, hullDamage, wearIncrease)
        res.onSuccess { refreshFleet() }
        return res
    }

    // Local saved plans
    fun getSavedPlans(): Flow<List<SavedLoadoutPlan>> = loadoutPlanDao.getAllSavedPlans().map { list ->
        list.map { entity ->
            val json = JSONObject(entity.plannedModulesJson)
            val map = mutableMapOf<String, String>()
            json.keys().forEach { k -> map[k] = json.getString(k) }
            SavedLoadoutPlan(
                planId = entity.planId,
                name = entity.name,
                hullId = entity.hullId,
                hullName = entity.hullName,
                plannedModules = map,
                createdAtEpoch = entity.createdAtEpoch,
                isFavorite = entity.isFavorite
            )
        }
    }

    suspend fun savePlan(
        name: String,
        hullId: String,
        hullName: String,
        plannedModules: Map<String, String>
    ): String {
        val id = "plan-${UUID.randomUUID().toString().take(8)}"
        val json = JSONObject()
        plannedModules.forEach { (k, v) -> json.put(k, v) }

        loadoutPlanDao.insertPlan(
            SavedLoadoutPlanEntity(
                planId = id,
                name = name,
                hullId = hullId,
                hullName = hullName,
                plannedModulesJson = json.toString(),
                isFavorite = false,
                createdAtEpoch = System.currentTimeMillis()
            )
        )
        return id
    }

    suspend fun deletePlan(planId: String) {
        loadoutPlanDao.deletePlanById(planId)
    }
}

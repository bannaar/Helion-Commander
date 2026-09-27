package com.example.helion.core.repository

import com.example.helion.core.database.TacticalMissionDao
import com.example.helion.core.database.TacticalMissionEntity
import com.example.helion.core.model.FleetTaskStatus
import com.example.helion.core.model.LocationMarker
import com.example.helion.core.model.MissionCategory
import com.example.helion.core.model.MissionStatus
import com.example.helion.core.model.ObjectiveStatus
import com.example.helion.core.model.TacticalMission
import com.example.helion.core.model.TacticalObjective
import com.example.helion.core.model.ThreatLevel
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class TacticalMissionsRepository(
    private val api: CompanionApi,
    private val missionDao: TacticalMissionDao
) {
    private val _missionsState = MutableStateFlow<List<TacticalMission>>(emptyList())
    val missionsState: StateFlow<List<TacticalMission>> = _missionsState.asStateFlow()

    suspend fun refreshMissions(): Result<List<TacticalMission>> {
        val result = api.getTacticalMissions()
        result.onSuccess { missions ->
            _missionsState.value = missions
            val entities = missions.map { it.toEntity() }
            missionDao.insertMissions(entities)
        }
        return result
    }

    suspend fun advanceObjective(
        missionId: String,
        objectiveId: String,
        increment: Int = 1
    ): Result<TacticalMission> {
        val result = api.advanceMissionObjective(missionId, objectiveId, increment)
        result.onSuccess { updated ->
            updateLocalMission(updated)
            missionDao.insertMission(updated.toEntity())
        }
        return result
    }

    suspend fun updateFleetTask(
        missionId: String,
        status: FleetTaskStatus,
        progressDelta: Float
    ): Result<TacticalMission> {
        val result = api.updateFleetTaskStatus(missionId, status, progressDelta)
        result.onSuccess { updated ->
            updateLocalMission(updated)
            missionDao.insertMission(updated.toEntity())
        }
        return result
    }

    suspend fun acceptContract(missionId: String): Result<TacticalMission> {
        val result = api.acceptMissionContract(missionId)
        result.onSuccess { updated ->
            updateLocalMission(updated)
            missionDao.insertMission(updated.toEntity())
        }
        return result
    }

    suspend fun claimReward(missionId: String): Result<TacticalMission> {
        val result = api.claimMissionReward(missionId)
        result.onSuccess { updated ->
            updateLocalMission(updated)
            missionDao.insertMission(updated.toEntity())
        }
        return result
    }

    suspend fun setPriority(missionId: String, isPriority: Boolean): Result<TacticalMission> {
        val result = api.setMissionPriority(missionId, isPriority)
        result.onSuccess { updated ->
            val list = _missionsState.value.map {
                if (it.id == missionId) updated else it.copy(isPriorityTarget = false)
            }
            _missionsState.value = list
            missionDao.setPriorityTarget(missionId, isPriority)
        }
        return result
    }

    suspend fun abandonMission(missionId: String): Result<Unit> {
        val result = api.abandonMission(missionId)
        result.onSuccess {
            refreshMissions()
        }
        return result
    }

    private fun updateLocalMission(mission: TacticalMission) {
        val current = _missionsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == mission.id }
        if (index != -1) {
            current[index] = mission
            _missionsState.value = current
        } else {
            current.add(mission)
            _missionsState.value = current
        }
    }

    // Room DB Flow mapping
    fun getCachedMissionsFlow(): Flow<List<TacticalMission>> = missionDao.getAllMissions().map { list ->
        list.map { it.toModel() }
    }

    companion object {
        fun TacticalMission.toEntity(): TacticalMissionEntity {
            val locJson = JSONObject().apply {
                put("systemId", primaryLocation.systemId)
                put("systemName", primaryLocation.systemName)
                put("celestialBodyName", primaryLocation.celestialBodyName)
                put("beaconCode", primaryLocation.beaconCode)
                put("coordinates", primaryLocation.coordinates)
                put("securityRating", primaryLocation.securityRating.toDouble())
                put("distanceLy", primaryLocation.distanceLy.toDouble())
                put("jumpCount", primaryLocation.jumpCount)
            }.toString()

            val objArray = JSONArray().apply {
                objectives.forEach { obj ->
                    val oJson = JSONObject().apply {
                        put("id", obj.id)
                        put("title", obj.title)
                        put("description", obj.description)
                        put("status", obj.status.name)
                        put("currentProgress", obj.currentProgress)
                        put("targetProgress", obj.targetProgress)
                        put("unit", obj.unit)
                        put("assignedFleetUnit", obj.assignedFleetUnit ?: "")
                        val mJson = JSONObject().apply {
                            put("systemId", obj.locationMarker.systemId)
                            put("systemName", obj.locationMarker.systemName)
                            put("celestialBodyName", obj.locationMarker.celestialBodyName)
                            put("beaconCode", obj.locationMarker.beaconCode)
                            put("coordinates", obj.locationMarker.coordinates)
                            put("securityRating", obj.locationMarker.securityRating.toDouble())
                            put("distanceLy", obj.locationMarker.distanceLy.toDouble())
                            put("jumpCount", obj.locationMarker.jumpCount)
                        }
                        put("locationMarker", mJson)
                    }
                    put(oJson)
                }
            }.toString()

            return TacticalMissionEntity(
                missionId = id,
                missionName = title,
                title = title,
                briefing = briefing,
                sponsorFaction = sponsorFaction,
                category = category.name,
                threatLevel = threatLevel.name,
                creditReward = creditReward,
                standingReward = standingReward,
                bonusRewardItem = bonusRewardItem,
                primaryLocationJson = locJson,
                objectivesJson = objArray,
                assignedShipName = assignedShipName,
                assignedFleetStatus = assignedFleetStatus.name,
                assignedFleetTaskLabel = assignedFleetTaskLabel,
                fleetProgressPercent = fleetProgressPercent,
                timeRemainingMinutes = timeRemainingMinutes,
                status = status.name,
                completionTimestamp = if (status == MissionStatus.COMPLETED || status == MissionStatus.CLAIMED) System.currentTimeMillis() else 0L,
                isPriorityTarget = isPriorityTarget,
                lastUpdatedEpoch = System.currentTimeMillis()
            )
        }

        fun TacticalMissionEntity.toModel(): TacticalMission {
            val locJson = JSONObject(primaryLocationJson)
            val loc = LocationMarker(
                systemId = locJson.optString("systemId", "sys-kepler"),
                systemName = locJson.optString("systemName", "Kepler Prime"),
                celestialBodyName = locJson.optString("celestialBodyName", "Kepler System"),
                beaconCode = locJson.optString("beaconCode", "NAV-001"),
                coordinates = locJson.optString("coordinates", "X: 0, Y: 0, Z: 0"),
                securityRating = locJson.optDouble("securityRating", 0.8).toFloat(),
                distanceLy = locJson.optDouble("distanceLy", 0.0).toFloat(),
                jumpCount = locJson.optInt("jumpCount", 0)
            )

            val objList = mutableListOf<TacticalObjective>()
            val objArray = JSONArray(objectivesJson)
            for (i in 0 until objArray.length()) {
                val oJson = objArray.getJSONObject(i)
                val mJson = oJson.optJSONObject("locationMarker")
                val oLoc = if (mJson != null) {
                    LocationMarker(
                        systemId = mJson.optString("systemId", loc.systemId),
                        systemName = mJson.optString("systemName", loc.systemName),
                        celestialBodyName = mJson.optString("celestialBodyName", loc.celestialBodyName),
                        beaconCode = mJson.optString("beaconCode", loc.beaconCode),
                        coordinates = mJson.optString("coordinates", loc.coordinates),
                        securityRating = mJson.optDouble("securityRating", 0.8).toFloat(),
                        distanceLy = mJson.optDouble("distanceLy", 0.0).toFloat(),
                        jumpCount = mJson.optInt("jumpCount", 0)
                    )
                } else loc

                val statusStr = oJson.optString("status", ObjectiveStatus.IN_PROGRESS.name)
                val objStatus = try {
                    ObjectiveStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    ObjectiveStatus.IN_PROGRESS
                }

                objList.add(
                    TacticalObjective(
                        id = oJson.optString("id", "obj-$i"),
                        title = oJson.optString("title", "Objective"),
                        description = oJson.optString("description", ""),
                        status = objStatus,
                        currentProgress = oJson.optInt("currentProgress", 0),
                        targetProgress = oJson.optInt("targetProgress", 1),
                        unit = oJson.optString("unit", "Units"),
                        locationMarker = oLoc,
                        assignedFleetUnit = oJson.optString("assignedFleetUnit").takeIf { it.isNotEmpty() }
                    )
                )
            }

            val categoryEnum = try {
                MissionCategory.valueOf(category)
            } catch (e: Exception) {
                MissionCategory.COMBAT_INTERDICTION
            }

            val threatEnum = try {
                ThreatLevel.valueOf(threatLevel)
            } catch (e: Exception) {
                ThreatLevel.MODERATE
            }

            val fleetStatusEnum = try {
                FleetTaskStatus.valueOf(assignedFleetStatus)
            } catch (e: Exception) {
                FleetTaskStatus.IDLE
            }

            val missionStatusEnum = try {
                MissionStatus.valueOf(status)
            } catch (e: Exception) {
                MissionStatus.ACTIVE
            }

            return TacticalMission(
                id = missionId,
                title = title,
                briefing = briefing,
                sponsorFaction = sponsorFaction,
                category = categoryEnum,
                threatLevel = threatEnum,
                creditReward = creditReward,
                standingReward = standingReward,
                bonusRewardItem = bonusRewardItem,
                primaryLocation = loc,
                objectives = objList,
                assignedShipName = assignedShipName,
                assignedFleetStatus = fleetStatusEnum,
                assignedFleetTaskLabel = assignedFleetTaskLabel,
                fleetProgressPercent = fleetProgressPercent,
                timeRemainingMinutes = timeRemainingMinutes,
                status = missionStatusEnum,
                isPriorityTarget = isPriorityTarget
            )
        }
    }
}

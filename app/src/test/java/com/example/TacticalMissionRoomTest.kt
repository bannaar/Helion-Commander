package com.example

import com.example.helion.core.database.TacticalMissionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TacticalMissionRoomTest {

    @Test
    fun testTacticalMissionEntityCreationAndFields() {
        val timestamp = 1727401234567L
        val mission = TacticalMissionEntity(
            missionId = "mis-test-001",
            missionName = "Operation Dark Void",
            status = "COMPLETED",
            completionTimestamp = timestamp
        )

        // Verify required fields: mission name, status, completion timestamp
        assertEquals("mis-test-001", mission.missionId)
        assertEquals("Operation Dark Void", mission.missionName)
        assertEquals("COMPLETED", mission.status)
        assertEquals(timestamp, mission.completionTimestamp)

        // Verify convenience properties & defaults
        assertEquals("Operation Dark Void", mission.title)
        assertEquals("Operation Dark Void", mission.name)
        assertEquals("mis-test-001", mission.id)
        assertTrue(mission.completionTimestamp > 0)
    }

    @Test
    fun testTacticalMissionEntityWithFullParameters() {
        val timestamp = System.currentTimeMillis()
        val mission = TacticalMissionEntity(
            missionId = "mis-full-002",
            missionName = "High-Sec Convoy Escort",
            status = "ACTIVE",
            completionTimestamp = timestamp,
            title = "High-Sec Convoy Escort",
            briefing = "Protect transport freighters in Kepler corridor",
            sponsorFaction = "Aegis Corporation",
            category = "CARGO_ESCORT",
            threatLevel = "MODERATE",
            creditReward = 120000L,
            standingReward = 25,
            bonusRewardItem = "Advanced Shield Booster",
            assignedShipName = "Aegis Invictus",
            assignedFleetStatus = "ORBITAL_PATROL",
            fleetProgressPercent = 50f,
            timeRemainingMinutes = 45,
            isPriorityTarget = true
        )

        assertEquals("mis-full-002", mission.missionId)
        assertEquals("High-Sec Convoy Escort", mission.missionName)
        assertEquals("ACTIVE", mission.status)
        assertEquals(timestamp, mission.completionTimestamp)
        assertEquals(120000L, mission.creditReward)
        assertEquals("CARGO_ESCORT", mission.category)
        assertTrue(mission.isPriorityTarget)
        assertEquals(50f, mission.fleetProgressPercent, 0.01f)
    }
}

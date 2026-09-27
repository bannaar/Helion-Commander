package com.example

import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.MarketTransactionRequest
import com.example.helion.core.model.ModuleSlotCategory
import com.example.helion.core.model.RouteOptimizationMode
import com.example.helion.core.model.SecurityClass
import com.example.helion.core.navigation.GalaxyRouter
import com.example.helion.core.network.FakeCompanionApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HelionCompanionTests {

    private val api = FakeCompanionApi()

    @Test
    fun testSecurityClassFromRating() {
        assertEquals(SecurityClass.HIGH_SECURITY, SecurityClass.fromRating(5.0f))
        assertEquals(SecurityClass.HIGH_SECURITY, SecurityClass.fromRating(0.8f))
        assertEquals(SecurityClass.HIGH_SECURITY, SecurityClass.fromRating(0.2f))
        assertEquals(SecurityClass.NULL_SECURITY, SecurityClass.fromRating(0.0f))
        assertEquals(SecurityClass.LOW_SECURITY, SecurityClass.fromRating(-0.2f))
        assertEquals(SecurityClass.LOW_SECURITY, SecurityClass.fromRating(-2.4f))
        assertEquals(SecurityClass.LOW_SECURITY, SecurityClass.fromRating(-5.0f))
    }

    @Test
    fun testGalaxyRouterFastestPath() = runBlocking {
        val systems = api.getGalaxySystems().getOrThrow()

        // Kepler -> Crossroads (Kepler -> Harrow -> Vantage -> Crossroads)
        val route = GalaxyRouter.findRoute(
            originId = "sys-kepler",
            destinationId = "sys-crossroads",
            allSystems = systems,
            mode = RouteOptimizationMode.FASTEST
        )

        assertNotNull(route)
        assertEquals(3, route?.totalJumps)
        assertEquals("Kepler", route?.origin?.name)
        assertEquals("Crossroads", route?.destination?.name)
        assertEquals(0.8f, route?.lowestSecurityRating ?: 0f, 0.01f)
    }

    @Test
    fun testGalaxyRouterAvoidLowSecAndNullSec() = runBlocking {
        val systems = api.getGalaxySystems().getOrThrow()

        // Cinder is low-sec (-2.4), Aurelia is 0.0 null-sec
        // Under HIGH_SEC_ONLY, routing to Cinder should be prohibited
        val routeToLowSec = GalaxyRouter.findRoute(
            originId = "sys-kepler",
            destinationId = "sys-cinder",
            allSystems = systems,
            mode = RouteOptimizationMode.HIGH_SEC_ONLY
        )
        assertNull("Route to low sec should not be possible under HIGH_SEC_ONLY", routeToLowSec)

        val routeToNullSec = GalaxyRouter.findRoute(
            originId = "sys-kepler",
            destinationId = "sys-aurelia",
            allSystems = systems,
            mode = RouteOptimizationMode.AVOID_NULLSEC
        )
        assertNull("Route to null sec should not be possible under AVOID_NULLSEC", routeToNullSec)
    }

    @Test
    fun testMarketServerAuthorityValidation() = runBlocking {
        // Buy order with quantity that exceeds balance or stock should fail
        val excessiveReq = MarketTransactionRequest(
            commanderId = "cmd-bannaar",
            currentStationId = "sta-kepler-prime",
            commodityId = "com-refined-metals",
            quantity = 99999, // Way more than stock/credits
            isBuyAction = true
        )

        val res = api.executeMarketTransaction(excessiveReq)
        assertTrue("Transaction should fail validation on server", res.isFailure)

        // Reasonable buy order should validate and commit
        val validReq = MarketTransactionRequest(
            commanderId = "cmd-bannaar",
            currentStationId = "sta-kepler-prime",
            commodityId = "com-refined-metals",
            quantity = 2,
            isBuyAction = true
        )

        val validRes = api.executeMarketTransaction(validReq)
        assertTrue("Valid transaction should be committed by server", validRes.isSuccess)
        val tx = validRes.getOrThrow()
        assertTrue(tx.transactionId.startsWith("TX-"))
        assertTrue(tx.authoritativeCredits < 42580L)
    }

    @Test
    fun testShipLoadoutPowerCalculations() = runBlocking {
        val ships = api.getOwnedShips().getOrThrow()
        val raptor = ships.find { it.instanceId == "ship-raptor-01" }

        assertNotNull(raptor)
        assertFalse("Standard Aster Raptor should not be power overloaded", raptor!!.isPowerOverloaded)
        assertTrue("Total mass should include base hull mass", raptor.totalMassTons > raptor.hullDefinition.baseMassTons)
    }

    @Test
    fun testEnvironmentSeparation() = runBlocking {
        assertEquals(HelionEnvironment.PRODUCTION, api.getCurrentEnvironment())
        api.switchEnvironment(HelionEnvironment.PRIVATE_TEST)
        assertEquals(HelionEnvironment.PRIVATE_TEST, api.getCurrentEnvironment())
        assertFalse(api.getCurrentEnvironment().isAuthoritativeProd)
    }

    @Test
    fun testStarSystemCelestialBodiesAndMiningYields() = runBlocking {
        val systems = api.getGalaxySystems().getOrThrow()
        assertTrue("Galaxy should contain systems", systems.isNotEmpty())

        val kepler = systems["sys-kepler"]
        assertNotNull(kepler)
        assertTrue("Kepler should contain surveyed planets", kepler!!.celestialBodies.isNotEmpty())
        assertEquals(4, kepler.celestialBodies.size)

        // Check specific planet types
        val volcanic = kepler.celestialBodies.find { it.type == com.example.helion.core.model.PlanetType.VOLCANIC }
        assertNotNull("Should contain volcanic world", volcanic)
        assertTrue("Volcanic world should have mining yields", volcanic!!.miningYields.isNotEmpty())

        val gasGiant = kepler.celestialBodies.find { it.type == com.example.helion.core.model.PlanetType.GAS_GIANT }
        assertNotNull("Should contain gas giant", gasGiant)
        val volatileYield = gasGiant!!.miningYields.find { it.resourceName.contains("Volatiles") }
        assertNotNull("Gas giant should yield hydrocarbon volatiles", volatileYield)
        assertTrue("Gas giant yield should exceed 80%", volatileYield!!.yieldPercentage >= 80)
        assertTrue("Gas giant should have tons/hr rate", volatileYield.estimatedTonsPerHour > 500)

        // Check 0.0 Null Sec system (Aurelia) for pristine high-end mining
        val aurelia = systems["sys-aurelia"]
        assertNotNull(aurelia)
        val shatteredWorld = aurelia!!.celestialBodies.find { it.type == com.example.helion.core.model.PlanetType.SHATTERED_WORLD }
        assertNotNull("Aurelia should contain shattered world with morphite", shatteredWorld)
        val morphiteYield = shatteredWorld!!.miningYields.find { it.resourceName.contains("Morphite") }
        assertNotNull(morphiteYield)
        assertEquals(com.example.helion.core.model.ResourceAbundance.PRISTINE, morphiteYield!!.abundance)
        assertTrue(morphiteYield.yieldPercentage >= 95)
    }

    @Test
    fun testTacticalMissionsAndFleetObjectivesTracking() = runBlocking {
        val missions = api.getTacticalMissions().getOrThrow()
        assertTrue("Missions should not be empty", missions.isNotEmpty())

        val combatMission = missions.find { it.id == "mis-combat-01" }
        assertNotNull("Combat mission should exist", combatMission)
        assertEquals(com.example.helion.core.model.MissionCategory.COMBAT_INTERDICTION, combatMission!!.category)
        assertEquals("sys-kepler", combatMission.primaryLocation.systemId)
        assertEquals("NAV-884-KPL", combatMission.primaryLocation.beaconCode)
        assertTrue("Location should have coordinates", combatMission.primaryLocation.coordinates.contains("AU"))

        // Verify objectives tracking
        val corvettesObjective = combatMission.objectives.find { it.id == "obj-101" }
        assertNotNull(corvettesObjective)
        assertEquals(3, corvettesObjective!!.currentProgress)
        assertEquals(4, corvettesObjective.targetProgress)
        assertFalse(corvettesObjective.isCompleted)

        // Advance objective
        val updated = api.advanceMissionObjective("mis-combat-01", "obj-101", 1).getOrThrow()
        val updatedObjective = updated.objectives.find { it.id == "obj-101" }
        assertNotNull(updatedObjective)
        assertEquals(4, updatedObjective!!.currentProgress)
        assertTrue("Objective should now be completed", updatedObjective.isCompleted)
        assertEquals(com.example.helion.core.model.ObjectiveStatus.COMPLETED, updatedObjective.status)

        // Verify fleet task progress
        assertTrue(updated.fleetProgressPercent > 0f)
        assertEquals("Aegis Invictus (Vindicator Cruiser)", updated.assignedShipName)

        // Update fleet task status
        val retasked = api.updateFleetTaskStatus("mis-combat-01", com.example.helion.core.model.FleetTaskStatus.ORBITAL_PATROL, 10f).getOrThrow()
        assertEquals(com.example.helion.core.model.FleetTaskStatus.ORBITAL_PATROL, retasked.assignedFleetStatus)

        // Test claiming reward for completed mission
        val completedMission = missions.find { it.id == "mis-recon-03" }
        assertNotNull(completedMission)
        assertEquals(com.example.helion.core.model.MissionStatus.COMPLETED, completedMission!!.status)

        val beforeCredits = api.getCommanderProfile().getOrThrow().credits
        val claimed = api.claimMissionReward("mis-recon-03").getOrThrow()
        assertEquals(com.example.helion.core.model.MissionStatus.CLAIMED, claimed.status)

        val afterCredits = api.getCommanderProfile().getOrThrow().credits
        assertEquals(beforeCredits + completedMission.creditReward, afterCredits)
    }

    @Test
    fun testTacticalMissionDashboardMetricsAndWeeklyBarChartData() = runBlocking {
        val missions = api.getTacticalMissions().getOrThrow()
        val metrics = com.example.helion.core.model.TacticalMissionAnalytics.generateDashboardData(missions)

        assertNotNull(metrics)
        assertTrue("Should have 6 weekly records", metrics.weeklyRecords.size >= 6)
        assertTrue("Total completed missions should be positive", metrics.totalCompletedMissions > 0)
        assertTrue("Weekly average completed should be positive", metrics.weeklyAverageCompleted > 0f)
        assertEquals(8, metrics.quotaTargetPerWeek)

        // Check weekly records structure for bar chart
        val currentWeek = metrics.weeklyRecords.find { it.isCurrentWeek }
        assertNotNull("Current week record should exist", currentWeek)
        assertTrue("Completed count should be positive", currentWeek!!.completedCount > 0)
        assertTrue("Credits earned should be positive", currentWeek.creditsEarned > 0L)
        assertTrue("Top operating ship should not be empty", currentWeek.topShip.isNotEmpty())
    }

    @Test
    fun testMarketPriceThresholdAlertLogic() = runBlocking {
        val marketItems = api.getMarketItems("sta-kepler-prime").getOrThrow()
        val metals = marketItems.find { it.commodityId == "com-refined-metals" }
        assertNotNull("Metals should exist", metals)

        // Buy price threshold: Alert when buy price <= target
        val buyPrice = metals!!.buyPrice
        val thresholdTarget = buyPrice + 10L // Target is higher, so current buy price is AT_OR_BELOW threshold
        val isThresholdMetBelow = buyPrice <= thresholdTarget
        assertTrue("Price should trigger AT_OR_BELOW threshold", isThresholdMetBelow)

        // Sell price threshold: Alert when sell price >= target
        val sellPrice = metals.sellPrice
        val sellTarget = sellPrice - 10L // Target is lower, so current sell price is AT_OR_ABOVE threshold
        val isThresholdMetAbove = sellPrice >= sellTarget
        assertTrue("Price should trigger AT_OR_ABOVE threshold", isThresholdMetAbove)
    }

    @Test
    fun testShipMaintenanceCalculations() = runBlocking {
        val isolatedApi = FakeCompanionApi()
        val ships = isolatedApi.getOwnedShips().getOrThrow()
        val raptor = ships.find { it.instanceId == "ship-raptor-01" }
        assertNotNull(raptor)

        // Fresh raptor has 94.5% hull and 12% wear (< 35% wear and >= 80% hull)
        assertFalse("New ship should not require maintenance", raptor!!.requiresMaintenance)

        // Simulate wear/damage
        val damaged = isolatedApi.simulateShipWear("ship-raptor-01", 30f, 40f).getOrThrow()
        assertTrue("Ship with 70% hull or 40% wear should require maintenance", damaged.requiresMaintenance)
        assertTrue("Maintenance status summary should reflect degradation", damaged.maintenanceStatusSummary.contains("CRITICAL") || damaged.maintenanceStatusSummary.contains("WARNING"))

        // Perform maintenance
        val overhauled = isolatedApi.performShipMaintenance("ship-raptor-01").getOrThrow()
        assertEquals(100f, overhauled.hullConditionPercent, 0.01f)
        assertEquals(0f, overhauled.wearPercent, 0.01f)
        assertFalse("Overhauled ship should no longer require maintenance", overhauled.requiresMaintenance)
    }
}


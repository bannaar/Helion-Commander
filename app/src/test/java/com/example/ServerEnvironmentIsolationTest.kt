package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.database.AppDatabase
import com.example.helion.core.database.CachedCommanderEntity
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.network.CompanionApiFactory
import com.example.helion.core.network.DefaultCompanionApiFactory
import com.example.helion.core.network.DevelopmentSimulationApi
import com.example.helion.core.network.FakeCompanionApi
import com.example.helion.core.network.RealCompanionApi
import com.example.helion.core.network.ServerNotConfiguredException
import com.example.helion.core.session.AuthCredentialStore
import com.example.helion.core.session.EnvironmentPreferences
import com.example.helion.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ServerEnvironmentIsolationTest {

    private lateinit var context: Context
    private lateinit var container: HelionAppContainer
    private lateinit var preferences: EnvironmentPreferences
    private lateinit var credentialStore: AuthCredentialStore
    private lateinit var apiFactory: CompanionApiFactory

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferences = EnvironmentPreferences(context)
        credentialStore = AuthCredentialStore(context)
        credentialStore.clearAll()
        preferences.setSelectedEnvironment(ServerEnvironment.DEMO)
        apiFactory = DefaultCompanionApiFactory()
        container = HelionAppContainer(context)
    }

    @Test
    fun testDemoDoesNotClaimRealServerConnectivity() = runBlocking {
        val demoApi = apiFactory.getApi(ServerEnvironment.DEMO)
        assertTrue("DEMO must use FakeCompanionApi", demoApi is FakeCompanionApi)

        val statusResult = demoApi.getServerStatus()
        assertTrue("DEMO status must succeed with simulated probe", statusResult.isSuccess)
        val status = statusResult.getOrThrow()
        assertEquals(ServerEnvironment.DEMO, status.environment)
        assertTrue("DEMO must be clearly labeled as mock/simulated", status.serverVersion.contains("mock"))
    }

    @Test
    fun testUnconfiguredRealServersFailHonestly() = runBlocking {
        val testApi = apiFactory.getApi(ServerEnvironment.PRIVATE_TEST)
        assertTrue("PRIVATE_TEST must use RealCompanionApi", testApi is RealCompanionApi)
        val testStatus = testApi.getServerStatus()
        assertTrue("Unconfigured PRIVATE_TEST status probe must fail", testStatus.isFailure)
        assertTrue(
            "Failure message must explain server not configured",
            testStatus.exceptionOrNull() is ServerNotConfiguredException
        )

        val prodApi = apiFactory.getApi(ServerEnvironment.PRODUCTION)
        assertTrue("PRODUCTION must use RealCompanionApi", prodApi is RealCompanionApi)
        val prodStatus = prodApi.getServerStatus()
        assertTrue("Unconfigured PRODUCTION status probe must fail", prodStatus.isFailure)
        assertTrue(
            "Failure message must explain server not configured",
            prodStatus.exceptionOrNull() is ServerNotConfiguredException
        )
    }

    @Test
    fun testCompanionApiFactorySelectsCorrectImplementations() {
        val demoApi = apiFactory.getApi(ServerEnvironment.DEMO)
        val testApi = apiFactory.getApi(ServerEnvironment.PRIVATE_TEST)
        val prodApi = apiFactory.getApi(ServerEnvironment.PRODUCTION)

        assertTrue("DEMO must use FakeCompanionApi", demoApi is FakeCompanionApi)
        assertTrue("PRIVATE_TEST must use RealCompanionApi", testApi is RealCompanionApi)
        assertTrue("PRODUCTION must use RealCompanionApi", prodApi is RealCompanionApi)
    }

    @Test
    fun testRealCompanionApiDoesNotImplementDevelopmentSimulationApi() {
        val testApi = apiFactory.getApi(ServerEnvironment.PRIVATE_TEST)
        val prodApi = apiFactory.getApi(ServerEnvironment.PRODUCTION)

        assertFalse(
            "RealCompanionApi for PRIVATE_TEST must NOT implement DevelopmentSimulationApi",
            testApi is DevelopmentSimulationApi
        )
        assertFalse(
            "RealCompanionApi for PRODUCTION must NOT implement DevelopmentSimulationApi",
            prodApi is DevelopmentSimulationApi
        )
    }

    @Test
    fun testTestSelectionPersists() {
        preferences.setSelectedEnvironment(ServerEnvironment.PRIVATE_TEST)
        val reloadedPreferences = EnvironmentPreferences(context)
        assertEquals(ServerEnvironment.PRIVATE_TEST, reloadedPreferences.getSelectedEnvironment())
    }

    @Test
    fun testProductionSelectionPersists() {
        preferences.setSelectedEnvironment(ServerEnvironment.PRODUCTION)
        val reloadedPreferences = EnvironmentPreferences(context)
        assertEquals(ServerEnvironment.PRODUCTION, reloadedPreferences.getSelectedEnvironment())
    }

    @Test
    fun testSwitchingToProductionRequiresConfirmation() = runBlocking {
        preferences.setSelectedEnvironment(ServerEnvironment.DEMO)
        val viewModel = SettingsViewModel(container)

        assertEquals(ServerEnvironment.DEMO, viewModel.uiState.value.currentServerEnvironment)
        assertFalse(viewModel.uiState.value.pendingProductionSwitch)

        // Request switch to PRODUCTION
        viewModel.requestSwitchEnvironment(ServerEnvironment.PRODUCTION)

        // Invariant: environment must NOT change immediately; confirmation dialog must be pending
        assertTrue("Switch to PRODUCTION must trigger pending confirmation dialog", viewModel.uiState.value.pendingProductionSwitch)
        assertEquals("Environment must remain DEMO until confirmed", ServerEnvironment.DEMO, viewModel.uiState.value.currentServerEnvironment)

        // Cancel switch
        viewModel.cancelProductionSwitch()
        assertFalse(viewModel.uiState.value.pendingProductionSwitch)
        assertEquals(ServerEnvironment.DEMO, viewModel.uiState.value.currentServerEnvironment)

        // Request again and confirm
        viewModel.requestSwitchEnvironment(ServerEnvironment.PRODUCTION)
        assertTrue(viewModel.uiState.value.pendingProductionSwitch)
        viewModel.confirmProductionSwitch()

        assertFalse("Pending flag cleared after confirmation", viewModel.uiState.value.pendingProductionSwitch)
        assertEquals("Environment successfully switched to PRODUCTION after confirmation", ServerEnvironment.PRODUCTION, viewModel.uiState.value.currentServerEnvironment)
        assertEquals("Persistent preferences updated to PRODUCTION", ServerEnvironment.PRODUCTION, preferences.getSelectedEnvironment())
    }

    @Test
    fun testSwitchingToPrivateTestDoesNotRequireProductionDialog() {
        preferences.setSelectedEnvironment(ServerEnvironment.DEMO)
        val viewModel = SettingsViewModel(container)

        viewModel.requestSwitchEnvironment(ServerEnvironment.PRIVATE_TEST)
        assertFalse("Switching to PRIVATE_TEST must not trigger production dialog", viewModel.uiState.value.pendingProductionSwitch)
        assertEquals(ServerEnvironment.PRIVATE_TEST, viewModel.uiState.value.currentServerEnvironment)
    }

    @Test
    fun testCredentialsAreStrictlyIsolatedByEnvironment() {
        val testToken = "test_bearer_token_secret_123"
        val prodToken = "prod_bearer_token_secret_456"

        credentialStore.setAuthToken(ServerEnvironment.PRIVATE_TEST, testToken)

        // Verify isolation: PRODUCTION must NOT see TEST token
        assertNull("PRODUCTION must not see TEST token", credentialStore.getAuthToken(ServerEnvironment.PRODUCTION))
        assertEquals(testToken, credentialStore.getAuthToken(ServerEnvironment.PRIVATE_TEST))

        // Set PRODUCTION token
        credentialStore.setAuthToken(ServerEnvironment.PRODUCTION, prodToken)

        // Verify both tokens remain separate and distinct
        assertEquals(testToken, credentialStore.getAuthToken(ServerEnvironment.PRIVATE_TEST))
        assertEquals(prodToken, credentialStore.getAuthToken(ServerEnvironment.PRODUCTION))
        assertNotEquals(credentialStore.getAuthToken(ServerEnvironment.PRIVATE_TEST), credentialStore.getAuthToken(ServerEnvironment.PRODUCTION))

        // Clear TEST token does not affect PRODUCTION
        credentialStore.clearAuthToken(ServerEnvironment.PRIVATE_TEST)
        assertNull(credentialStore.getAuthToken(ServerEnvironment.PRIVATE_TEST))
        assertEquals(prodToken, credentialStore.getAuthToken(ServerEnvironment.PRODUCTION))
    }

    @Test
    fun testDatabasesAreStrictlyIsolatedBetweenEnvironments() = runBlocking {
        val demoDb = AppDatabase.getInstance(context, ServerEnvironment.DEMO)
        val testDb = AppDatabase.getInstance(context, ServerEnvironment.PRIVATE_TEST)
        val prodDb = AppDatabase.getInstance(context, ServerEnvironment.PRODUCTION)

        assertNotNull(demoDb)
        assertNotNull(testDb)
        assertNotNull(prodDb)

        // Insert commander into TEST database
        val testCommander = CachedCommanderEntity(
            commanderId = "cmd-test-isolation",
            displayName = "Test Pilot",
            callSign = "TST-01",
            credits = 999999L,
            xp = 1000L,
            rank = "Test Rank",
            career = "Combat",
            currentSystemName = "Test Space",
            currentStationName = "Test Station",
            activeShipId = "ship-test",
            environment = "PRIVATE_TEST",
            cachedAtEpoch = System.currentTimeMillis()
        )
        testDb.commanderDao().insertCachedCommander(testCommander)

        // Verify presence in TEST database
        val fetchedTest = testDb.commanderDao().getCachedCommander().first()
        assertNotNull("TEST database must contain test commander", fetchedTest)
        assertEquals("cmd-test-isolation", fetchedTest?.commanderId)

        // Invariant: PRODUCTION database must NOT contain TEST commander
        val fetchedProd = prodDb.commanderDao().getCachedCommander().first()
        assertNull("PRODUCTION database must NOT contain data inserted into TEST database", fetchedProd)

        // Invariant: DEMO database must NOT contain TEST commander
        val fetchedDemo = demoDb.commanderDao().getCachedCommander().first()
        assertNull("DEMO database must NOT contain data inserted into TEST database", fetchedDemo)
    }

    @Test
    fun testRepositoryPersistenceFollowsEnvironmentSwitch() = runBlocking {
        val demoPlanId = container.fleetRepository.savePlan(
            name = "DEMO isolation plan",
            hullId = "hull-demo",
            hullName = "Demo Hull",
            plannedModules = mapOf("slot-1" to "module-demo")
        )

        container.setActiveServerEnvironment(ServerEnvironment.PRIVATE_TEST)

        val testPlanId = container.fleetRepository.savePlan(
            name = "TEST isolation plan",
            hullId = "hull-test",
            hullName = "Test Hull",
            plannedModules = mapOf("slot-1" to "module-test")
        )

        val demoPlans = AppDatabase.getInstance(context, ServerEnvironment.DEMO)
            .loadoutPlanDao()
            .getAllSavedPlans()
            .first()
        val testPlans = AppDatabase.getInstance(context, ServerEnvironment.PRIVATE_TEST)
            .loadoutPlanDao()
            .getAllSavedPlans()
            .first()
        val prodPlans = AppDatabase.getInstance(context, ServerEnvironment.PRODUCTION)
            .loadoutPlanDao()
            .getAllSavedPlans()
            .first()

        assertTrue("DEMO plan must remain in DEMO database", demoPlans.any { it.planId == demoPlanId })
        assertFalse("DEMO plan must not leak into TEST database", testPlans.any { it.planId == demoPlanId })
        assertTrue("TEST plan must be written to TEST database after switch", testPlans.any { it.planId == testPlanId })
        assertFalse("TEST plan must not remain bound to DEMO database", demoPlans.any { it.planId == testPlanId })
        assertFalse("TEST plan must not leak into PRODUCTION database", prodPlans.any { it.planId == testPlanId })
    }

    @Test
    fun testEnvironmentSwitchClearsInMemoryAuthoritativeState() = runBlocking {
        assertTrue(container.commanderRepository.refreshCommanderProfile().isSuccess)
        assertTrue(container.fleetRepository.refreshFleet().isSuccess)
        assertTrue(container.marketRepository.refreshLocalMarket().isSuccess)

        assertNotNull(container.commanderRepository.commanderState.value)
        assertTrue(container.fleetRepository.ownedShips.value.isNotEmpty())
        assertTrue(container.marketRepository.localMarket.value.isNotEmpty())

        container.setActiveServerEnvironment(ServerEnvironment.PRIVATE_TEST)

        assertNull("Commander state from DEMO must be cleared on TEST switch", container.commanderRepository.commanderState.value)
        assertTrue("Fleet state from DEMO must be cleared on TEST switch", container.fleetRepository.ownedShips.value.isEmpty())
        assertTrue("Market state from DEMO must be cleared on TEST switch", container.marketRepository.localMarket.value.isEmpty())
    }

    @Test
    fun testDemoSimulationPowersFollowDelegatedEnvironment() = runBlocking {
        val demoMarket = container.marketRepository.refreshLocalMarket().getOrThrow()
        val item = demoMarket.first()

        val demoMutation = container.marketRepository.updateCommodityPrice(
            commodityId = item.commodityId,
            newBuyPrice = item.buyPrice + 1,
            newSellPrice = item.sellPrice + 1,
            stationId = item.stationId
        )
        assertTrue("DEMO must retain development simulation controls", demoMutation.isSuccess)

        container.setActiveServerEnvironment(ServerEnvironment.PRIVATE_TEST)

        val testMutation = container.marketRepository.updateCommodityPrice(
            commodityId = item.commodityId,
            newBuyPrice = item.buyPrice + 2,
            newSellPrice = item.sellPrice + 2,
            stationId = item.stationId
        )
        assertTrue("PRIVATE TEST ordinary companion path must not expose local simulation controls", testMutation.isFailure)
        assertTrue(testMutation.exceptionOrNull() is UnsupportedOperationException)
    }

}

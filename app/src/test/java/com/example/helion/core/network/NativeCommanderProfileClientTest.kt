package com.example.helion.core.network

import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeCommanderProfileClientTest {

    private val profileLine =
        "PROFILE user=explorer display=Explorer One faction=free-traders ship=sidewinder " +
            "credits=1536 experience=5 hull=100 max-hull=100 engine-level=1 hull-level=1 " +
            "mission-stage=1 mission-ore-mined=1"

    @Test
    fun parsesVerifiedNativeProfileWithoutInventingUnsupportedFields() {
        val profile = parseNativeCommanderProfile(
            ServerEnvironment.PRIVATE_TEST,
            profileLine
        )

        assertEquals("explorer", profile.commanderId)
        assertEquals("Explorer One", profile.displayName)
        assertEquals(1536L, profile.credits)
        assertEquals(5L, profile.xp)
        assertEquals("sidewinder", profile.activeShipId)
        assertEquals(HelionEnvironment.PRIVATE_TEST, profile.environment)
        assertEquals(NATIVE_PROFILE_NOT_ADVERTISED, profile.callSign)
        assertEquals(NATIVE_PROFILE_NOT_ADVERTISED, profile.rank)
        assertEquals(NATIVE_PROFILE_NOT_ADVERTISED, profile.career)
        assertEquals(NATIVE_PROFILE_NOT_ADVERTISED, profile.currentSystemName)
        assertTrue(profile.factionStandings.isEmpty())
        assertTrue(profile.licenses.isEmpty())
        assertEquals(0L, profile.serverTimeEpoch)
    }

    @Test(expected = NativeProfileProtocolException::class)
    fun rejectsMalformedNativeProfile() {
        parseNativeCommanderProfile(
            ServerEnvironment.PRIVATE_TEST,
            "PROFILE user=explorer display=Explorer One credits=1536"
        )
    }

    @Test
    fun validatesCompanionTokenEnvelope() {
        val valid = "hc1.0123456789abcdef." + "a".repeat(64)
        assertTrue(isValidCompanionTokenFormat(valid))
        assertTrue(!isValidCompanionTokenFormat("hc1.bad.token"))
        assertTrue(!isValidCompanionTokenFormat(valid.uppercase()))
    }

    @Test
    fun realApiUsesEnvironmentScopedTokenForProfileRead() = runBlocking {
        val endpoint = ServerEndpoint("test.helion.invalid", 4242)
        val token = "hc1.0123456789abcdef." + "b".repeat(64)
        var capturedEnvironment: ServerEnvironment? = null
        var capturedEndpoint: ServerEndpoint? = null
        var capturedToken: String? = null

        val client = object : NativeCommanderProfileClient {
            override suspend fun fetchProfile(
                environment: ServerEnvironment,
                endpoint: ServerEndpoint,
                companionToken: String
            ): Result<CommanderProfile> {
                capturedEnvironment = environment
                capturedEndpoint = endpoint
                capturedToken = companionToken
                return Result.success(
                    parseNativeCommanderProfile(environment, profileLine)
                )
            }
        }

        val api = RealCompanionApi(
            environment = ServerEnvironment.PRIVATE_TEST,
            endpoint = endpoint,
            credentialProvider = { token },
            commanderProfileClient = client
        )

        val result = api.getCommanderProfile()

        assertTrue(result.isSuccess)
        assertEquals(ServerEnvironment.PRIVATE_TEST, capturedEnvironment)
        assertEquals(endpoint, capturedEndpoint)
        assertEquals(token, capturedToken)
        assertEquals("explorer", result.getOrThrow().commanderId)
    }

    @Test
    fun realApiRequiresCompanionCredentialBeforeProfileRead() = runBlocking {
        var clientCalled = false
        val client = object : NativeCommanderProfileClient {
            override suspend fun fetchProfile(
                environment: ServerEnvironment,
                endpoint: ServerEndpoint,
                companionToken: String
            ): Result<CommanderProfile> {
                clientCalled = true
                return Result.failure(AssertionError("client must not be called"))
            }
        }

        val api = RealCompanionApi(
            environment = ServerEnvironment.PRODUCTION,
            endpoint = ServerEndpoint("prod.helion.invalid", 4242),
            credentialProvider = { null },
            commanderProfileClient = client
        )

        val result = api.getCommanderProfile()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CompanionAuthenticationRequiredException)
        assertTrue(!clientCalled)
    }

    @Test
    fun factoryKeepsTestAndProductionCredentialsSeparate() = runBlocking {
        val testToken = "hc1.1111111111111111." + "c".repeat(64)
        val prodToken = "hc1.2222222222222222." + "d".repeat(64)
        val seen = mutableListOf<Pair<ServerEnvironment, String>>()
        val client = object : NativeCommanderProfileClient {
            override suspend fun fetchProfile(
                environment: ServerEnvironment,
                endpoint: ServerEndpoint,
                companionToken: String
            ): Result<CommanderProfile> {
                seen += environment to companionToken
                return Result.success(parseNativeCommanderProfile(environment, profileLine))
            }
        }

        val factory = DefaultCompanionApiFactory(
            privateTestEndpoint = ServerEndpoint("test.helion.invalid", 4242),
            productionEndpoint = ServerEndpoint("prod.helion.invalid", 4242),
            credentialProvider = { env ->
                when (env) {
                    ServerEnvironment.PRIVATE_TEST -> testToken
                    ServerEnvironment.PRODUCTION -> prodToken
                    ServerEnvironment.DEMO -> null
                }
            },
            commanderProfileClient = client
        )

        factory.getApi(ServerEnvironment.PRIVATE_TEST).getCommanderProfile().getOrThrow()
        factory.getApi(ServerEnvironment.PRODUCTION).getCommanderProfile().getOrThrow()

        assertEquals(
            listOf(
                ServerEnvironment.PRIVATE_TEST to testToken,
                ServerEnvironment.PRODUCTION to prodToken
            ),
            seen
        )
    }
}

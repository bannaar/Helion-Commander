package com.example.helion.core.network

import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeServerStatusProbeTest {

    @Test
    fun acceptsVerifiedNativeProtocolWelcome() {
        val status = parseNativeServerWelcome(
            ServerEnvironment.PRIVATE_TEST,
            "WELCOME Helion/2"
        )

        assertEquals("HELION Native Server", status.serviceName)
        assertEquals(ServerEnvironment.PRIVATE_TEST, status.environment)
        assertEquals("2", status.protocolVersion)
        assertNull("Native server does not advertise software version", status.serverVersion)
        assertNull("Native server does not advertise maintenance state", status.maintenance)
    }

    @Test(expected = ServerProtocolMismatchException::class)
    fun rejectsDifferentNativeProtocolVersion() {
        parseNativeServerWelcome(
            ServerEnvironment.PRIVATE_TEST,
            "WELCOME Helion/3"
        )
    }

    @Test(expected = ServerUnavailableException::class)
    fun rejectsUnexpectedGreeting() {
        parseNativeServerWelcome(
            ServerEnvironment.PRIVATE_TEST,
            "HELION READY"
        )
    }

    @Test
    fun configuredRealApiUsesNativeStatusProbe() = runBlocking {
        val endpoint = ServerEndpoint("test.helion.invalid", 4242)
        var probedEndpoint: ServerEndpoint? = null
        val fakeProbe = object : NativeServerStatusProbe {
            override suspend fun probe(
                environment: ServerEnvironment,
                endpoint: ServerEndpoint
            ): Result<ServerStatus> {
                probedEndpoint = endpoint
                return Result.success(
                    ServerStatus(
                        serviceName = "HELION Native Server",
                        environment = environment,
                        protocolVersion = "2",
                        message = "synthetic probe"
                    )
                )
            }
        }

        val api = RealCompanionApi(
            environment = ServerEnvironment.PRIVATE_TEST,
            endpoint = endpoint,
            statusProbe = fakeProbe
        )

        val result = api.getServerStatus()

        assertTrue(result.isSuccess)
        assertEquals(endpoint, probedEndpoint)
        assertEquals("2", result.getOrThrow().protocolVersion)
    }

    @Test
    fun unconfiguredRealApiStillFailsHonestly() = runBlocking {
        val api = RealCompanionApi(ServerEnvironment.PRODUCTION)

        val result = api.getServerStatus()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerNotConfiguredException)
    }

    @Test
    fun configuredFactoryMarksOnlyConfiguredEnvironment() {
        val endpoint = ServerEndpoint("test.helion.invalid", 4242)
        val factory = DefaultCompanionApiFactory(privateTestEndpoint = endpoint)

        assertTrue(factory.getProfile(ServerEnvironment.PRIVATE_TEST).isConfigured)
        assertEquals(endpoint, factory.getProfile(ServerEnvironment.PRIVATE_TEST).endpoint)
        assertFalse(factory.getProfile(ServerEnvironment.PRODUCTION).isConfigured)
        assertNull(factory.getProfile(ServerEnvironment.PRODUCTION).endpoint)
    }
}

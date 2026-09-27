package com.example.helion.core.network

import com.example.helion.core.model.ServerEndpoint
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerProfile

interface CompanionApiFactory {
    fun getApi(environment: ServerEnvironment): CompanionApi
    fun getProfile(environment: ServerEnvironment): ServerProfile
}

class DefaultCompanionApiFactory(
    val fakeApi: FakeCompanionApi = FakeCompanionApi(),
    private val privateTestEndpoint: ServerEndpoint? = null,
    private val productionEndpoint: ServerEndpoint? = null,
    private val statusProbe: NativeServerStatusProbe = TlsNativeServerStatusProbe()
) : CompanionApiFactory {

    private val realTestApi = RealCompanionApi(
        environment = ServerEnvironment.PRIVATE_TEST,
        endpoint = privateTestEndpoint,
        statusProbe = statusProbe
    )

    private val realProdApi = RealCompanionApi(
        environment = ServerEnvironment.PRODUCTION,
        endpoint = productionEndpoint,
        statusProbe = statusProbe
    )

    override fun getApi(environment: ServerEnvironment): CompanionApi = when (environment) {
        ServerEnvironment.DEMO -> fakeApi
        ServerEnvironment.PRIVATE_TEST -> realTestApi
        ServerEnvironment.PRODUCTION -> realProdApi
    }

    override fun getProfile(environment: ServerEnvironment): ServerProfile = when (environment) {
        ServerEnvironment.DEMO -> ServerProfile(
            environment = ServerEnvironment.DEMO,
            displayName = "DEMO / OFFLINE",
            endpoint = null,
            isConfigured = true,
            description = "Local mock simulation. Safe for offline development and UI testing."
        )
        ServerEnvironment.PRIVATE_TEST -> ServerProfile(
            environment = ServerEnvironment.PRIVATE_TEST,
            displayName = "PRIVATE TEST",
            endpoint = privateTestEndpoint,
            description = if (privateTestEndpoint == null) {
                "Isolated staging universe (LAB-SEC-7). Native TLS endpoint is not configured."
            } else {
                "Isolated staging universe (LAB-SEC-7). Native TLS status probe configured for ${privateTestEndpoint.displayAddress}."
            }
        )
        ServerEnvironment.PRODUCTION -> ServerProfile(
            environment = ServerEnvironment.PRODUCTION,
            displayName = "PRODUCTION",
            endpoint = productionEndpoint,
            description = if (productionEndpoint == null) {
                "Persistent live universe (HELION-1). Native TLS endpoint is not configured."
            } else {
                "Persistent live universe (HELION-1). Native TLS status probe configured for ${productionEndpoint.displayAddress}."
            }
        )
    }
}

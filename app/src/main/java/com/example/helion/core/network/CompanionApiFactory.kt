package com.example.helion.core.network

import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerProfile

interface CompanionApiFactory {
    fun getApi(environment: ServerEnvironment): CompanionApi
    fun getProfile(environment: ServerEnvironment): ServerProfile
}

class DefaultCompanionApiFactory(
    val fakeApi: FakeCompanionApi = FakeCompanionApi()
) : CompanionApiFactory {

    private val realTestApi = RealCompanionApi(
        environment = ServerEnvironment.PRIVATE_TEST,
        baseUrl = null,
        isConfigured = false
    )

    private val realProdApi = RealCompanionApi(
        environment = ServerEnvironment.PRODUCTION,
        baseUrl = null,
        isConfigured = false
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
            baseUrl = null,
            isConfigured = true,
            description = "Local mock simulation. Safe for offline development and UI testing."
        )
        ServerEnvironment.PRIVATE_TEST -> ServerProfile(
            environment = ServerEnvironment.PRIVATE_TEST,
            displayName = "PRIVATE TEST",
            baseUrl = null,
            isConfigured = false,
            description = "Isolated staging universe (LAB-SEC-7). Prototype is not connected."
        )
        ServerEnvironment.PRODUCTION -> ServerProfile(
            environment = ServerEnvironment.PRODUCTION,
            displayName = "PRODUCTION",
            baseUrl = null,
            isConfigured = false,
            description = "Persistent live universe (HELION-1). Prototype is not connected."
        )
    }
}

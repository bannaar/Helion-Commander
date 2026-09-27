package com.example.helion.core.model

enum class HelionEnvironment(
    val displayName: String,
    val serverEndpoint: String,
    val isAuthoritativeProd: Boolean,
    val description: String,
    val serverEnv: ServerEnvironment
) {
    PRODUCTION(
        displayName = "PRODUCTION (HELION-1)",
        serverEndpoint = "NOT_CONNECTED",
        isAuthoritativeProd = true,
        description = "Production target profile. This prototype build is not connected to the live HELION server.",
        serverEnv = ServerEnvironment.PRODUCTION
    ),
    PRIVATE_TEST(
        displayName = "TEST (LAB-SEC-7)",
        serverEndpoint = "NOT_CONNECTED",
        isAuthoritativeProd = false,
        description = "Private-test target profile. This prototype build currently uses local mock data.",
        serverEnv = ServerEnvironment.PRIVATE_TEST
    ),
    DEVELOPMENT(
        displayName = "DEV (LOCAL-SIM)",
        serverEndpoint = "NOT_CONNECTED",
        isAuthoritativeProd = false,
        description = "Development target profile. Local mock simulation.",
        serverEnv = ServerEnvironment.DEMO
    );

    companion object {
        fun fromServerEnvironment(env: ServerEnvironment): HelionEnvironment = when (env) {
            ServerEnvironment.DEMO -> DEVELOPMENT
            ServerEnvironment.PRIVATE_TEST -> PRIVATE_TEST
            ServerEnvironment.PRODUCTION -> PRODUCTION
        }
    }
}

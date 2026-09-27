package com.example.helion.core.model

enum class HelionEnvironment(
    val displayName: String,
    val serverEndpoint: String,
    val isAuthoritativeProd: Boolean,
    val description: String
) {
    PRODUCTION(
        displayName = "PRODUCTION (HELION-1)",
        serverEndpoint = "NOT_CONNECTED",
        isAuthoritativeProd = true,
        description = "Production target profile. This prototype build is not connected to the live HELION server."
    ),
    PRIVATE_TEST(
        displayName = "TEST (LAB-SEC-7)",
        serverEndpoint = "NOT_CONNECTED",
        isAuthoritativeProd = false,
        description = "Private-test target profile. This prototype build currently uses local mock data."
    ),
    DEVELOPMENT(
        displayName = "DEV (LOCAL-SIM)",
        serverEndpoint = "NOT_CONNECTED",
        isAuthoritativeProd = false,
        description = "Development target profile. Configure a real server endpoint only when RealCompanionApi exists."
    )
}

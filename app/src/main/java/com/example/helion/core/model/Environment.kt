package com.example.helion.core.model

enum class HelionEnvironment(
    val displayName: String,
    val serverEndpoint: String,
    val isAuthoritativeProd: Boolean,
    val description: String
) {
    PRODUCTION(
        displayName = "PRODUCTION (HELION-1)",
        serverEndpoint = "https://universe.helion-game.net/v1",
        isAuthoritativeProd = true,
        description = "Live persistent universe. All actions commit to live economy."
    ),
    PRIVATE_TEST(
        displayName = "TEST (LAB-SEC-7)",
        serverEndpoint = "https://lab.helion-game.net/v1",
        isAuthoritativeProd = false,
        description = "Staging sandbox. Experimental 100-credit baseline for testing."
    ),
    DEVELOPMENT(
        displayName = "DEV (LOCAL-SIM)",
        serverEndpoint = "http://127.0.0.1:9090/v1",
        isAuthoritativeProd = false,
        description = "Local node development simulation server."
    )
}

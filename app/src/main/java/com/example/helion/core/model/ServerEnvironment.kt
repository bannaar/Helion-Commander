package com.example.helion.core.model

enum class ServerEnvironment(
    val id: String,
    val displayName: String,
    val badgeLabel: String,
    val description: String,
    val defaultBaseUrl: String?
) {
    DEMO(
        id = "demo",
        displayName = "DEMO / OFFLINE",
        badgeLabel = "DEMO // OFFLINE",
        description = "Local prototype simulation using mock data. Fully safe for offline development and local UI evaluation.",
        defaultBaseUrl = null
    ),
    PRIVATE_TEST(
        id = "private_test",
        displayName = "PRIVATE TEST",
        badgeLabel = "TEST // PRIVATE TEST UNIVERSE",
        description = "Target profile for the isolated private test universe (LAB-SEC-7). This build remains NOT CONFIGURED until a verified server endpoint is connected.",
        defaultBaseUrl = null
    ),
    PRODUCTION(
        id = "production",
        displayName = "PRODUCTION",
        badgeLabel = "LIVE // PERSISTENT UNIVERSE",
        description = "Target profile for the single persistent HELION universe. This build remains NOT CONFIGURED until a verified production endpoint is connected.",
        defaultBaseUrl = null
    );

    companion object {
        fun fromId(id: String?): ServerEnvironment {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: DEMO
        }
    }
}

data class ServerProfile(
    val environment: ServerEnvironment,
    val displayName: String = environment.displayName,
    val baseUrl: String? = environment.defaultBaseUrl,
    val isConfigured: Boolean = false,
    val description: String = environment.description
)

data class ServerStatus(
    val serviceName: String,
    val environment: ServerEnvironment,
    val serverVersion: String,
    val protocolVersion: String,
    val maintenance: Boolean,
    val message: String? = null
)

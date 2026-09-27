package com.example.helion.core.model

enum class ServerEnvironment(
    val id: String,
    val displayName: String,
    val badgeLabel: String,
    val description: String
) {
    DEMO(
        id = "demo",
        displayName = "DEMO / OFFLINE",
        badgeLabel = "DEMO // OFFLINE",
        description = "Local prototype simulation using mock data. Fully safe for offline development and local UI evaluation."
    ),
    PRIVATE_TEST(
        id = "private_test",
        displayName = "PRIVATE TEST",
        badgeLabel = "TEST // PRIVATE TEST UNIVERSE",
        description = "Target profile for the isolated private test universe (LAB-SEC-7). Requires an explicitly configured native TLS endpoint."
    ),
    PRODUCTION(
        id = "production",
        displayName = "PRODUCTION",
        badgeLabel = "LIVE // PERSISTENT UNIVERSE",
        description = "Target profile for the single persistent HELION universe. Requires an explicitly configured native TLS endpoint."
    );

    companion object {
        fun fromId(id: String?): ServerEnvironment {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: DEMO
        }
    }
}

data class ServerEndpoint(
    val host: String,
    val port: Int = 4242
) {
    init {
        require(host.isNotBlank()) { "Server endpoint host must not be blank." }
        require(port in 1..65535) { "Server endpoint port must be between 1 and 65535." }
    }

    val displayAddress: String
        get() = "$host:$port"
}

data class ServerProfile(
    val environment: ServerEnvironment,
    val displayName: String = environment.displayName,
    val endpoint: ServerEndpoint? = null,
    val isConfigured: Boolean = endpoint != null,
    val description: String = environment.description
)

data class ServerStatus(
    val serviceName: String,
    val environment: ServerEnvironment,
    val serverVersion: String? = null,
    val protocolVersion: String,
    val maintenance: Boolean? = null,
    val message: String? = null
)

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
        serverEndpoint = "BUILD_CONFIGURED_NATIVE_TLS",
        isAuthoritativeProd = true,
        description = "Production target profile. Configured builds support verified native TLS status and scoped companion PROFILE reads.",
        serverEnv = ServerEnvironment.PRODUCTION
    ),
    PRIVATE_TEST(
        displayName = "TEST (LAB-SEC-7)",
        serverEndpoint = "BUILD_CONFIGURED_NATIVE_TLS",
        isAuthoritativeProd = false,
        description = "Private-test target profile. Configured builds support verified native TLS status and scoped companion PROFILE reads without DEMO fallback.",
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

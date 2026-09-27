package com.example.helion.core.repository

import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerProfile
import com.example.helion.core.network.CompanionApi
import com.example.helion.core.session.EnvironmentPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val api: CompanionApi,
    private val preferences: EnvironmentPreferences? = null
) {
    private val initialEnv = preferences?.getSelectedEnvironment() ?: ServerEnvironment.DEMO

    private val _currentServerEnvironment = MutableStateFlow(initialEnv)
    val currentServerEnvironment: StateFlow<ServerEnvironment> = _currentServerEnvironment.asStateFlow()

    private val _currentEnvironment = MutableStateFlow(HelionEnvironment.fromServerEnvironment(initialEnv))
    val currentEnvironment: StateFlow<HelionEnvironment> = _currentEnvironment.asStateFlow()

    private val _isOfflineSimulated = MutableStateFlow(false)
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    private val _notifyDMs = MutableStateFlow(true)
    val notifyDMs: StateFlow<Boolean> = _notifyDMs.asStateFlow()

    private val _notifyGuild = MutableStateFlow(true)
    val notifyGuild: StateFlow<Boolean> = _notifyGuild.asStateFlow()

    private val _notifyMarketWatch = MutableStateFlow(true)
    val notifyMarketWatch: StateFlow<Boolean> = _notifyMarketWatch.asStateFlow()

    private val _notifyGalNetFlash = MutableStateFlow(true)
    val notifyGalNetFlash: StateFlow<Boolean> = _notifyGalNetFlash.asStateFlow()

    suspend fun switchServerEnvironment(env: ServerEnvironment): Result<Unit> {
        val helionEnv = HelionEnvironment.fromServerEnvironment(env)
        val res = api.switchEnvironment(helionEnv)
        res.onSuccess {
            _currentServerEnvironment.value = env
            _currentEnvironment.value = helionEnv
            preferences?.setSelectedEnvironment(env)
        }
        return res
    }

    suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit> {
        val serverEnv = env.serverEnv
        return switchServerEnvironment(serverEnv)
    }

    fun getServerProfile(env: ServerEnvironment = _currentServerEnvironment.value): ServerProfile {
        return when (env) {
            ServerEnvironment.DEMO -> ServerProfile(
                environment = ServerEnvironment.DEMO,
                displayName = "DEMO / OFFLINE",
                baseUrl = null,
                isConfigured = true,
                description = "Local mock simulation. Safe for offline development and local testing."
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

    fun setOfflineSimulated(offline: Boolean) {
        _isOfflineSimulated.value = offline
    }

    fun setNotifyDMs(value: Boolean) { _notifyDMs.value = value }
    fun setNotifyGuild(value: Boolean) { _notifyGuild.value = value }
    fun setNotifyMarketWatch(value: Boolean) { _notifyMarketWatch.value = value }
    fun setNotifyGalNetFlash(value: Boolean) { _notifyGalNetFlash.value = value }
}

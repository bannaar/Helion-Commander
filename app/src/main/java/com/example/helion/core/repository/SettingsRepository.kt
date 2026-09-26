package com.example.helion.core.repository

import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val api: CompanionApi
) {
    private val _currentEnvironment = MutableStateFlow(HelionEnvironment.PRODUCTION)
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

    suspend fun switchEnvironment(env: HelionEnvironment): Result<Unit> {
        val res = api.switchEnvironment(env)
        res.onSuccess {
            _currentEnvironment.value = env
        }
        return res
    }

    fun setOfflineSimulated(offline: Boolean) {
        _isOfflineSimulated.value = offline
    }

    fun setNotifyDMs(value: Boolean) { _notifyDMs.value = value }
    fun setNotifyGuild(value: Boolean) { _notifyGuild.value = value }
    fun setNotifyMarketWatch(value: Boolean) { _notifyMarketWatch.value = value }
    fun setNotifyGalNetFlash(value: Boolean) { _notifyGalNetFlash.value = value }
}

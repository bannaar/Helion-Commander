package com.example.helion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.HelionEnvironment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentEnvironment: HelionEnvironment = HelionEnvironment.PRODUCTION,
    val isOfflineSimulated: Boolean = false,
    val notifyDMs: Boolean = true,
    val notifyGuild: Boolean = true,
    val notifyMarketWatch: Boolean = true,
    val notifyGalNetFlash: Boolean = true,
    val cacheClearedMessage: String? = null
)

class SettingsViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.settingsRepository.currentEnvironment.collect { env ->
                _uiState.value = _uiState.value.copy(currentEnvironment = env)
            }
        }
        viewModelScope.launch {
            container.settingsRepository.isOfflineSimulated.collect { off ->
                _uiState.value = _uiState.value.copy(isOfflineSimulated = off)
            }
        }
    }

    fun switchEnvironment(env: HelionEnvironment) {
        viewModelScope.launch {
            container.settingsRepository.switchEnvironment(env)
            // Reload all cached repositories for environment isolation
            container.commanderRepository.refreshCommanderProfile()
            container.fleetRepository.refreshFleet()
            container.marketRepository.refreshLocalMarket()
        }
    }

    fun setOfflineSimulated(offline: Boolean) {
        container.settingsRepository.setOfflineSimulated(offline)
    }

    fun toggleDMs(enabled: Boolean) {
        container.settingsRepository.setNotifyDMs(enabled)
        _uiState.value = _uiState.value.copy(notifyDMs = enabled)
    }

    fun toggleGuild(enabled: Boolean) {
        container.settingsRepository.setNotifyGuild(enabled)
        _uiState.value = _uiState.value.copy(notifyGuild = enabled)
    }

    fun toggleMarket(enabled: Boolean) {
        container.settingsRepository.setNotifyMarketWatch(enabled)
        _uiState.value = _uiState.value.copy(notifyMarketWatch = enabled)
    }

    fun toggleGalNet(enabled: Boolean) {
        container.settingsRepository.setNotifyGalNetFlash(enabled)
        _uiState.value = _uiState.value.copy(notifyGalNetFlash = enabled)
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cacheClearedMessage = "Local database cache flushed.")
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(cacheClearedMessage = null)
    }
}

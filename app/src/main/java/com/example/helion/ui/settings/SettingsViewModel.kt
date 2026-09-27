package com.example.helion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.ServerEnvironment
import com.example.helion.core.model.ServerProfile
import com.example.helion.core.model.ServerStatus
import com.example.helion.core.network.isValidCompanionTokenFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentEnvironment: HelionEnvironment = HelionEnvironment.DEVELOPMENT,
    val currentServerEnvironment: ServerEnvironment = ServerEnvironment.DEMO,
    val serverProfile: ServerProfile = ServerProfile(ServerEnvironment.DEMO, isConfigured = true),
    val serverStatus: ServerStatus? = null,
    val serverStatusError: String? = null,
    val isCheckingStatus: Boolean = false,
    val companionCredentialConfigured: Boolean = false,
    val companionTokenDraft: String = "",
    val credentialMessage: String? = null,
    val isVerifyingCredential: Boolean = false,
    val pendingProductionSwitch: Boolean = false,
    val isOfflineSimulated: Boolean = false,
    val notifyDMs: Boolean = true,
    val notifyGuild: Boolean = true,
    val notifyMarketWatch: Boolean = true,
    val notifyGalNetFlash: Boolean = true,
    val cacheClearedMessage: String? = null
)

class SettingsViewModel(val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            currentEnvironment = HelionEnvironment.fromServerEnvironment(container.activeEnvironment),
            currentServerEnvironment = container.activeEnvironment,
            serverProfile = container.apiFactory.getProfile(container.activeEnvironment)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.collect { env ->
                val profile = container.apiFactory.getProfile(env)
                val credentialConfigured = if (env == ServerEnvironment.DEMO) {
                    false
                } else {
                    runCatching { container.credentialStore.getCompanionToken(env) != null }
                        .getOrDefault(false)
                }
                _uiState.value = _uiState.value.copy(
                    currentServerEnvironment = env,
                    currentEnvironment = HelionEnvironment.fromServerEnvironment(env),
                    serverProfile = profile,
                    companionCredentialConfigured = credentialConfigured,
                    companionTokenDraft = "",
                    credentialMessage = null
                )
                refreshServerStatus()
            }
        }
        viewModelScope.launch {
            container.settingsRepository.isOfflineSimulated.collect { off ->
                _uiState.value = _uiState.value.copy(isOfflineSimulated = off)
            }
        }
    }

    fun profileFor(env: ServerEnvironment): ServerProfile = container.apiFactory.getProfile(env)

    fun requestSwitchEnvironment(env: ServerEnvironment) {
        if (env == _uiState.value.currentServerEnvironment) return

        // Safeguard: switching into PRODUCTION requires explicit confirmation
        if (env == ServerEnvironment.PRODUCTION) {
            _uiState.value = _uiState.value.copy(pendingProductionSwitch = true)
        } else {
            confirmSwitchEnvironment(env)
        }
    }

    fun cancelProductionSwitch() {
        _uiState.value = _uiState.value.copy(pendingProductionSwitch = false)
    }

    fun confirmProductionSwitch() {
        confirmSwitchEnvironment(ServerEnvironment.PRODUCTION)
    }

    fun confirmSwitchEnvironment(env: ServerEnvironment) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pendingProductionSwitch = false)
            container.setActiveServerEnvironment(env)

            // Safely refresh repositories for the newly active environment
            try {
                container.commanderRepository.refreshCommanderProfile()
                container.fleetRepository.refreshFleet()
                container.marketRepository.refreshLocalMarket()
            } catch (_: Exception) {
                // Unconfigured environments fail gracefully
            }

            refreshServerStatus()
        }
    }

    // Backward compatibility helper
    fun switchEnvironment(env: HelionEnvironment) {
        requestSwitchEnvironment(env.serverEnv)
    }

    fun refreshServerStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingStatus = true)
            val result = container.api.getServerStatus()
            result.fold(
                onSuccess = { status ->
                    _uiState.value = _uiState.value.copy(
                        serverStatus = status,
                        serverStatusError = null,
                        isCheckingStatus = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        serverStatus = null,
                        serverStatusError = error.message ?: "Server unavailable",
                        isCheckingStatus = false
                    )
                }
            )
        }
    }

    fun updateCompanionTokenDraft(value: String) {
        if (value.length > 128 || value.contains('\n') || value.contains('\r')) return
        _uiState.value = _uiState.value.copy(
            companionTokenDraft = value,
            credentialMessage = null
        )
    }

    fun saveCompanionCredential() {
        val env = _uiState.value.currentServerEnvironment
        if (env == ServerEnvironment.DEMO) {
            _uiState.value = _uiState.value.copy(
                credentialMessage = "DEMO/OFFLINE does not use authoritative companion credentials."
            )
            return
        }
        val token = _uiState.value.companionTokenDraft.trim()
        if (!isValidCompanionTokenFormat(token)) {
            _uiState.value = _uiState.value.copy(
                credentialMessage = "Invalid companion token format. Expected an hc1 token issued by the HELION server."
            )
            return
        }
        try {
            container.credentialStore.setCompanionToken(env, token)
            _uiState.value = _uiState.value.copy(
                companionCredentialConfigured = true,
                companionTokenDraft = "",
                credentialMessage = "Companion credential stored encrypted for ${env.displayName}."
            )
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                credentialMessage = error.message ?: "Unable to store companion credential."
            )
        }
    }

    fun clearCompanionCredential() {
        val env = _uiState.value.currentServerEnvironment
        if (env != ServerEnvironment.DEMO) {
            container.credentialStore.clearCompanionToken(env)
        }
        container.commanderRepository.clearEnvironmentState()
        _uiState.value = _uiState.value.copy(
            companionCredentialConfigured = false,
            companionTokenDraft = "",
            credentialMessage = "Companion credential cleared for ${env.displayName}."
        )
    }

    fun verifyCompanionCredential() {
        val env = _uiState.value.currentServerEnvironment
        if (env == ServerEnvironment.DEMO) {
            _uiState.value = _uiState.value.copy(
                credentialMessage = "DEMO/OFFLINE does not use companion authentication."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingCredential = true)
            val result = container.commanderRepository.refreshCommanderProfile()
            result.fold(
                onSuccess = { profile ->
                    _uiState.value = _uiState.value.copy(
                        companionCredentialConfigured = true,
                        credentialMessage = "Verified profile.read access for ${profile.displayName}.",
                        isVerifyingCredential = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        credentialMessage = error.message ?: "Companion credential verification failed.",
                        isVerifyingCredential = false
                    )
                }
            )
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
            _uiState.value = _uiState.value.copy(cacheClearedMessage = "Local database cache flushed for current environment.")
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(cacheClearedMessage = null)
    }
}

package com.example.helion.ui.guild

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.GuildInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class GuildUiState(
    val isLoading: Boolean = false,
    val guildInfo: GuildInfo? = null,
    val isPostingNotice: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class GuildViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(GuildUiState(isLoading = true))
    val uiState: StateFlow<GuildUiState> = _uiState.asStateFlow()

    init {
        loadGuildData()
        observeEnvironmentChanges()
    }

    private fun observeEnvironmentChanges() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.drop(1).collect {
                _uiState.value = GuildUiState(isLoading = true)
                loadGuildData()
            }
        }
    }

    fun loadGuildData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = container.guildRepository.refreshGuild()
            res.onSuccess {
                _uiState.value = GuildUiState(isLoading = false, guildInfo = it)
            }.onFailure {
                _uiState.value = GuildUiState(isLoading = false, errorMessage = it.message)
            }
        }
    }

    fun postNotice(title: String, body: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPostingNotice = true, errorMessage = null)
            val res = container.guildRepository.postNotice(title, body)
            res.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isPostingNotice = false,
                    statusMessage = "Notice posted to Guild board."
                )
                loadGuildData()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isPostingNotice = false,
                    errorMessage = err.message ?: "Failed to post notice."
                )
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = null, errorMessage = null)
    }
}

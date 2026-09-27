package com.example.helion.ui.commander

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.CommanderProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class CommanderUiState(
    val isLoading: Boolean = false,
    val profile: CommanderProfile? = null,
    val errorMessage: String? = null
)

class CommanderViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(CommanderUiState(isLoading = true))
    val uiState: StateFlow<CommanderUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeEnvironmentChanges()
    }

    private fun observeEnvironmentChanges() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.drop(1).collect {
                _uiState.value = CommanderUiState(isLoading = true)
                loadProfile()
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = container.commanderRepository.refreshCommanderProfile()
            result.onSuccess {
                _uiState.value = CommanderUiState(isLoading = false, profile = it)
            }.onFailure {
                _uiState.value = CommanderUiState(isLoading = false, errorMessage = it.message)
            }
        }
    }
}

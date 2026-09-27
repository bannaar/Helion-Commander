package com.example.helion.ui.galnet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.GalNetArticle
import com.example.helion.core.model.GalNetChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class GalNetUiState(
    val isLoading: Boolean = false,
    val articles: List<GalNetArticle> = emptyList(),
    val selectedChannel: GalNetChannel? = null, // null = All / Top Stories
    val selectedArticle: GalNetArticle? = null,
    val showSavedOnly: Boolean = false,
    val searchQuery: String = ""
)

class GalNetViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(GalNetUiState(isLoading = true))
    val uiState: StateFlow<GalNetUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
        observeEnvironmentChanges()
    }

    private fun observeEnvironmentChanges() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.drop(1).collect {
                _uiState.value = GalNetUiState(isLoading = true)
                loadArticles()
            }
        }
    }

    fun loadArticles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = container.galNetRepository.refreshArticles()
            val list = res.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                articles = list
            )
        }
    }

    fun selectChannel(channel: GalNetChannel?) {
        _uiState.value = _uiState.value.copy(selectedChannel = channel, showSavedOnly = false)
    }

    fun toggleSavedFilter() {
        _uiState.value = _uiState.value.copy(showSavedOnly = !_uiState.value.showSavedOnly, selectedChannel = null)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectArticle(article: GalNetArticle) {
        _uiState.value = _uiState.value.copy(selectedArticle = article)
        viewModelScope.launch {
            container.galNetRepository.markArticleRead(article.articleId)
        }
    }

    fun closeArticleDetail() {
        _uiState.value = _uiState.value.copy(selectedArticle = null)
    }

    fun toggleSaveArticle(articleId: String) {
        viewModelScope.launch {
            container.galNetRepository.toggleSaveArticle(articleId)
            val updated = _uiState.value.selectedArticle?.let {
                if (it.articleId == articleId) it.copy(isSaved = !it.isSaved) else it
            }
            _uiState.value = _uiState.value.copy(selectedArticle = updated)
        }
    }
}

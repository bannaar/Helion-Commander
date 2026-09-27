package com.example.helion.ui.universe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.database.RouteBookmarkEntity
import com.example.helion.core.model.PlanetType
import com.example.helion.core.model.ResourceAbundance
import com.example.helion.core.model.RouteOptimizationMode
import com.example.helion.core.model.RoutePlanResult
import com.example.helion.core.model.SystemNode
import com.example.helion.core.model.ThreatLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class UniverseUiState(
    val isLoading: Boolean = false,
    val systems: Map<String, SystemNode> = emptyMap(),
    val selectedSystem: SystemNode? = null,
    val drillDownSystem: SystemNode? = null,
    val selectedPlanetTypeFilter: PlanetType? = null,
    val originSystemId: String = "sys-kepler",
    val destinationSystemId: String = "sys-crossroads",
    val routeMode: RouteOptimizationMode = RouteOptimizationMode.FASTEST,
    val calculatedRoute: RoutePlanResult? = null,
    val bookmarks: List<RouteBookmarkEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedThreatFilter: ThreatLevel? = null,
    val selectedAbundanceFilter: ResourceAbundance? = null,
    val feedbackMessage: String? = null
)

class UniverseViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(UniverseUiState(isLoading = true))
    val uiState: StateFlow<UniverseUiState> = _uiState.asStateFlow()

    init {
        loadUniverseData()
        observeBookmarks()
        observeEnvironmentChanges()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.collectLatest {
                _uiState.value = _uiState.value.copy(bookmarks = emptyList())
                container.universeRepository.getBookmarks().collect { bms ->
                    _uiState.value = _uiState.value.copy(bookmarks = bms)
                }
            }
        }
    }

    private fun observeEnvironmentChanges() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.drop(1).collect {
                _uiState.value = UniverseUiState(isLoading = true)
                loadUniverseData()
            }
        }
    }

    fun loadUniverseData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = container.universeRepository.refreshGalaxySystems()
            val map = res.getOrDefault(emptyMap())
            val initialSelected = map["sys-kepler"]

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                systems = map,
                selectedSystem = initialSelected
            )
            recalculateRoute()
        }
    }

    fun selectSystem(system: SystemNode) {
        _uiState.value = _uiState.value.copy(selectedSystem = system)
    }

    fun setOrigin(systemId: String) {
        _uiState.value = _uiState.value.copy(originSystemId = systemId)
        recalculateRoute()
    }

    fun setDestination(systemId: String) {
        _uiState.value = _uiState.value.copy(destinationSystemId = systemId)
        recalculateRoute()
    }

    fun setRouteMode(mode: RouteOptimizationMode) {
        _uiState.value = _uiState.value.copy(routeMode = mode)
        recalculateRoute()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setThreatFilter(level: ThreatLevel?) {
        _uiState.value = _uiState.value.copy(selectedThreatFilter = level)
    }

    fun selectSystemById(systemId: String) {
        val sys = _uiState.value.systems[systemId]
        if (sys != null) {
            _uiState.value = _uiState.value.copy(selectedSystem = sys)
        }
    }

    fun setAbundanceFilter(abundance: ResourceAbundance?) {
        _uiState.value = _uiState.value.copy(selectedAbundanceFilter = abundance)
    }

    fun openSystemDrillDown(system: SystemNode) {
        _uiState.value = _uiState.value.copy(drillDownSystem = system, selectedPlanetTypeFilter = null)
    }

    fun closeSystemDrillDown() {
        _uiState.value = _uiState.value.copy(drillDownSystem = null, selectedPlanetTypeFilter = null)
    }

    fun setPlanetTypeFilter(type: PlanetType?) {
        _uiState.value = _uiState.value.copy(selectedPlanetTypeFilter = type)
    }

    fun recalculateRoute() {
        val current = _uiState.value
        val result = container.universeRepository.calculateRoute(
            originId = current.originSystemId,
            destinationId = current.destinationSystemId,
            mode = current.routeMode
        )
        _uiState.value = current.copy(calculatedRoute = result)
    }

    fun bookmarkCurrentRoute() {
        val route = _uiState.value.calculatedRoute ?: return
        viewModelScope.launch {
            container.universeRepository.saveBookmark(route)
            _uiState.value = _uiState.value.copy(feedbackMessage = "Route bookmarked.")
        }
    }

    fun deleteBookmark(bookmarkId: String) {
        viewModelScope.launch {
            container.universeRepository.deleteBookmark(bookmarkId)
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}

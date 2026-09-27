package com.example.helion.ui.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.LiveryOption
import com.example.helion.core.model.ModuleItem
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.core.model.SavedLoadoutPlan
import com.example.helion.core.model.ShipDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

enum class FleetFilter(val label: String) {
    ALL("All Ships"),
    ACTIVE("Active Flagship"),
    LOCAL("In-System"),
    STORED("Station Stored")
}

enum class FleetSort(val label: String) {
    NAME("Name"),
    CLASS("Ship Class"),
    CONDITION("Hull Integrity"),
    CARGO("Cargo Hold")
}

data class FleetUiState(
    val isLoading: Boolean = false,
    val ownedShips: List<OwnedShipInstance> = emptyList(),
    val selectedShip: OwnedShipInstance? = null,
    val availableModules: List<ModuleItem> = emptyList(),
    val savedPlans: List<SavedLoadoutPlan> = emptyList(),
    // Filtering, sorting, and inspection state
    val filter: FleetFilter = FleetFilter.ALL,
    val sort: FleetSort = FleetSort.NAME,
    val expandedShipId: String? = null,
    // Hypothetical loadout planner state: slotId -> moduleId
    val plannedModules: Map<String, String> = emptyMap(),
    // Livery customization state
    val selectedLivery: LiveryOption? = null,
    val liveryWearPreview: Float = 0f,
    val isApplyingAction: Boolean = false,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class FleetViewModel(private val container: HelionAppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(FleetUiState(isLoading = true))
    val uiState: StateFlow<FleetUiState> = _uiState.asStateFlow()

    init {
        loadFleetData()
        observeSavedPlans()
        observeEnvironmentChanges()
    }

    private fun observeSavedPlans() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.collectLatest {
                _uiState.value = _uiState.value.copy(savedPlans = emptyList())
                container.fleetRepository.getSavedPlans().collect { plans ->
                    _uiState.value = _uiState.value.copy(savedPlans = plans)
                }
            }
        }
    }

    private fun observeEnvironmentChanges() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.drop(1).collect {
                _uiState.value = FleetUiState(isLoading = true)
                loadFleetData()
            }
        }
    }

    fun loadFleetData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val fleetRes = container.fleetRepository.refreshFleet()
            val modulesRes = container.fleetRepository.getAvailableModules()

            val ships = fleetRes.getOrDefault(emptyList())
            val active = ships.find { it.isActiveShip } ?: ships.firstOrNull()
            val modules = modulesRes.getOrDefault(emptyList())

            val initialPlanned = active?.slots?.mapNotNull { s ->
                s.installedModule?.let { m -> s.slotId to m.moduleId }
            }?.toMap() ?: emptyMap()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                ownedShips = ships,
                selectedShip = active,
                availableModules = modules,
                plannedModules = initialPlanned,
                selectedLivery = active?.currentLivery,
                liveryWearPreview = active?.wearPercent ?: 0f
            )

            // Check if any ship requires maintenance and dispatch alert
            ships.forEach { ship ->
                if (ship.requiresMaintenance) {
                    container.notificationManager.notifyFleetShipMaintenance(ship)
                }
            }
        }
    }

    fun selectShip(ship: OwnedShipInstance) {
        val initialPlanned = ship.slots.mapNotNull { s ->
            s.installedModule?.let { m -> s.slotId to m.moduleId }
        }.toMap()

        _uiState.value = _uiState.value.copy(
            selectedShip = ship,
            plannedModules = initialPlanned,
            selectedLivery = ship.currentLivery,
            liveryWearPreview = ship.wearPercent,
            actionSuccessMessage = null,
            errorMessage = null
        )
    }

    fun setFilter(filter: FleetFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun setSort(sort: FleetSort) {
        _uiState.value = _uiState.value.copy(sort = sort)
    }

    fun toggleExpandedShip(instanceId: String) {
        val current = _uiState.value.expandedShipId
        _uiState.value = _uiState.value.copy(
            expandedShipId = if (current == instanceId) null else instanceId
        )
    }

    fun setActiveShipOnServer(instanceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplyingAction = true, errorMessage = null)
            val res = container.fleetRepository.setActiveShip(instanceId)
            res.onSuccess { updatedShip ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    selectedShip = updatedShip,
                    actionSuccessMessage = "Server confirmed: '${updatedShip.shipName}' is now your active ship."
                )
                // refresh commander profile too
                container.commanderRepository.refreshCommanderProfile()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    errorMessage = err.message ?: "Failed to set active ship."
                )
            }
        }
    }

    // Loadout planning functions
    fun setPlannedModuleForSlot(slotId: String, moduleId: String) {
        val current = _uiState.value.plannedModules.toMutableMap()
        current[slotId] = moduleId
        _uiState.value = _uiState.value.copy(plannedModules = current)
    }

    fun saveCurrentPlan(planName: String) {
        val ship = _uiState.value.selectedShip ?: return
        viewModelScope.launch {
            container.fleetRepository.savePlan(
                name = planName,
                hullId = ship.hullDefinition.hullId,
                hullName = ship.hullDefinition.hullName,
                plannedModules = _uiState.value.plannedModules
            )
            _uiState.value = _uiState.value.copy(
                actionSuccessMessage = "Fitting plan '$planName' saved locally."
            )
        }
    }

    fun loadSavedPlan(plan: SavedLoadoutPlan) {
        _uiState.value = _uiState.value.copy(
            plannedModules = plan.plannedModules,
            actionSuccessMessage = "Loaded fitting plan '${plan.name}' into planner."
        )
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            container.fleetRepository.deletePlan(planId)
        }
    }

    fun applyPlannedLoadoutToServer() {
        val ship = _uiState.value.selectedShip ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplyingAction = true, errorMessage = null)
            val res = container.fleetRepository.applyLoadoutToServer(
                shipInstanceId = ship.instanceId,
                plannedModules = _uiState.value.plannedModules
            )
            res.onSuccess { updatedShip ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    selectedShip = updatedShip,
                    actionSuccessMessage = "Server validation passed! New modules fitted to '${updatedShip.shipName}'."
                )
                container.commanderRepository.refreshCommanderProfile()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    errorMessage = err.message ?: "Refit rejected by server."
                )
            }
        }
    }

    // Livery functions
    fun selectLivery(livery: LiveryOption) {
        _uiState.value = _uiState.value.copy(selectedLivery = livery)
    }

    fun setWearPreview(wear: Float) {
        _uiState.value = _uiState.value.copy(liveryWearPreview = wear)
    }

    fun applyLiveryToServer() {
        val ship = _uiState.value.selectedShip ?: return
        val livery = _uiState.value.selectedLivery ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplyingAction = true, errorMessage = null)
            val res = container.fleetRepository.requestLiveryUpdate(ship.instanceId, livery.liveryId)
            res.onSuccess { updatedShip ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    selectedShip = updatedShip,
                    actionSuccessMessage = "Server confirmed: Livery paint scheme '${livery.name}' applied."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    errorMessage = err.message ?: "Failed to update livery on server."
                )
            }
        }
    }

    // Maintenance & notification actions
    fun performMaintenance(instanceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplyingAction = true, errorMessage = null)
            val res = container.fleetRepository.performShipMaintenance(instanceId)
            res.onSuccess { updatedShip ->
                container.notificationManager.resetDeduplicationForShip(instanceId)
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    selectedShip = updatedShip,
                    actionSuccessMessage = "Shipyard overhaul complete: '${updatedShip.shipName}' hull restored to 100% and wear cleared."
                )
                container.commanderRepository.refreshCommanderProfile()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    errorMessage = err.message ?: "Maintenance order failed."
                )
            }
        }
    }

    fun simulateWearAndDamage(instanceId: String, hullDamage: Float = 26.0f, wearIncrease: Float = 38.0f) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplyingAction = true, errorMessage = null)
            val res = container.fleetRepository.simulateShipWear(instanceId, hullDamage, wearIncrease)
            res.onSuccess { updatedShip ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    selectedShip = updatedShip,
                    actionSuccessMessage = "Combat sortie simulated: '${updatedShip.shipName}' sustained damage and requires maintenance!"
                )
                // Trigger notification!
                container.notificationManager.notifyFleetShipMaintenance(updatedShip, force = true)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isApplyingAction = false,
                    errorMessage = err.message ?: "Failed to simulate wear."
                )
            }
        }
    }

    fun sendTestMaintenanceNotification(ship: OwnedShipInstance) {
        container.notificationManager.notifyFleetShipMaintenance(ship, force = true)
        _uiState.value = _uiState.value.copy(
            actionSuccessMessage = "Local maintenance alert dispatched for '${ship.shipName}'."
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(actionSuccessMessage = null, errorMessage = null)
    }
}

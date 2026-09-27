package com.example.helion.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.model.FleetTaskStatus
import com.example.helion.core.model.MissionCategory
import com.example.helion.core.model.MissionStatus
import com.example.helion.core.model.TacticalMission
import com.example.helion.core.model.ThreatLevel
import com.example.helion.core.model.MissionChartMode
import com.example.helion.core.model.MissionDashboardMetrics
import com.example.helion.core.model.TacticalMissionAnalytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TacticalMissionsUiState(
    val isLoading: Boolean = false,
    val missions: List<TacticalMission> = emptyList(),
    val selectedTab: Int = 0, // 0: Active Operations, 1: Fleet Tasks, 2: Available Contracts, 3: Mission Archive, 4: Summary Dashboard
    val selectedCategoryFilter: MissionCategory? = null,
    val selectedThreatFilter: ThreatLevel? = null,
    val searchQuery: String = "",
    val selectedMission: TacticalMission? = null,
    val actionBannerMessage: String? = null,
    val isSimulatingFleetFeed: Boolean = false,
    val selectedWeekIndex: Int = 5,
    val chartMode: MissionChartMode = MissionChartMode.TOTAL,
    val dashboardMetrics: MissionDashboardMetrics = TacticalMissionAnalytics.generateDashboardData(emptyList())
)

class TacticalMissionsViewModel(
    private val container: HelionAppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TacticalMissionsUiState(isLoading = true))
    val uiState: StateFlow<TacticalMissionsUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        loadMissions()
        observeEnvironmentChanges()
        // Collect repository state flow and monitor for 100% progress
        viewModelScope.launch {
            container.tacticalMissionsRepository.missionsState.collect { list ->
                val metrics = TacticalMissionAnalytics.generateDashboardData(list)
                _uiState.value = _uiState.value.copy(
                    missions = list,
                    dashboardMetrics = metrics
                )

                // Trigger alerts when progress reaches 100%
                list.forEach { mission ->
                    if (mission.status == MissionStatus.COMPLETED || mission.overallProgressPercent >= 1.0f) {
                        container.notificationManager.notifyMissionProgressComplete(mission, isFleetTask = false)
                    }
                    if (mission.fleetProgressPercent >= 100f) {
                        container.notificationManager.notifyMissionProgressComplete(mission, isFleetTask = true)
                    }
                }
            }
        }
    }

    private fun observeEnvironmentChanges() {
        viewModelScope.launch {
            container.settingsRepository.currentServerEnvironment.drop(1).collect {
                simulationJob?.cancel()
                simulationJob = null
                _uiState.value = TacticalMissionsUiState(isLoading = true)
                loadMissions()
            }
        }
    }

    fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            container.tacticalMissionsRepository.refreshMissions()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setCategoryFilter(category: MissionCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    fun setThreatFilter(threat: ThreatLevel?) {
        _uiState.value = _uiState.value.copy(selectedThreatFilter = threat)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectMission(mission: TacticalMission?) {
        _uiState.value = _uiState.value.copy(selectedMission = mission)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(actionBannerMessage = null)
    }

    fun selectWeek(index: Int) {
        _uiState.value = _uiState.value.copy(selectedWeekIndex = index)
    }

    fun setChartMode(mode: MissionChartMode) {
        _uiState.value = _uiState.value.copy(chartMode = mode)
    }

    fun advanceObjective(missionId: String, objectiveId: String, increment: Int = 1) {
        viewModelScope.launch {
            val result = container.tacticalMissionsRepository.advanceObjective(missionId, objectiveId, increment)
            result.onSuccess { updated ->
                if (updated.status == MissionStatus.COMPLETED || updated.overallProgressPercent >= 1.0f) {
                    container.notificationManager.notifyMissionProgressComplete(updated, isFleetTask = false, force = true)
                }
                val banner = if (updated.status == MissionStatus.COMPLETED) {
                    "TACTICAL OPERATION COMPLETE: ${updated.title} — All objectives fulfilled! Ready to claim bounty."
                } else {
                    "Telemetry logged: Objective progress advanced on ${updated.title}"
                }
                _uiState.value = _uiState.value.copy(
                    actionBannerMessage = banner,
                    selectedMission = if (_uiState.value.selectedMission?.id == missionId) updated else _uiState.value.selectedMission
                )
            }
        }
    }

    fun updateFleetTaskStatus(missionId: String, status: FleetTaskStatus, progressDelta: Float) {
        viewModelScope.launch {
            val result = container.tacticalMissionsRepository.updateFleetTask(missionId, status, progressDelta)
            result.onSuccess { updated ->
                if (updated.fleetProgressPercent >= 100f) {
                    container.notificationManager.notifyMissionProgressComplete(updated, isFleetTask = true, force = true)
                }
                _uiState.value = _uiState.value.copy(
                    actionBannerMessage = "Fleet dispatch update: ${updated.assignedShipName} assigned to ${status.label}",
                    selectedMission = if (_uiState.value.selectedMission?.id == missionId) updated else _uiState.value.selectedMission
                )
            }
        }
    }

    fun simulateMission100Percent(missionId: String) {
        viewModelScope.launch {
            val mission = _uiState.value.missions.find { it.id == missionId } ?: return@launch
            var current = mission
            for (obj in current.objectives) {
                if (!obj.isCompleted) {
                    val res = container.tacticalMissionsRepository.advanceObjective(
                        missionId = mission.id,
                        objectiveId = obj.id,
                        increment = (obj.targetProgress - obj.currentProgress).coerceAtLeast(1)
                    )
                    res.onSuccess { current = it }
                }
            }
            container.notificationManager.notifyMissionProgressComplete(current, isFleetTask = false, force = true)
            _uiState.value = _uiState.value.copy(
                actionBannerMessage = "100% COMPLETION TRIGGERED: Notification dispatched for '${current.title}'!",
                selectedMission = current
            )
        }
    }

    fun simulateFleetTask100Percent(missionId: String) {
        viewModelScope.launch {
            val mission = _uiState.value.missions.find { it.id == missionId } ?: return@launch
            val res = container.tacticalMissionsRepository.updateFleetTask(
                missionId = mission.id,
                status = mission.assignedFleetStatus,
                progressDelta = (100f - mission.fleetProgressPercent).coerceAtLeast(10f)
            )
            res.onSuccess { updated ->
                container.notificationManager.notifyMissionProgressComplete(updated, isFleetTask = true, force = true)
                _uiState.value = _uiState.value.copy(
                    actionBannerMessage = "100% FLEET TASK REACHED: Notification dispatched for '${updated.assignedShipName}'!",
                    selectedMission = updated
                )
            }
        }
    }

    fun sendTestNotification(mission: TacticalMission) {
        container.notificationManager.notifyMissionProgressComplete(mission, isFleetTask = false, force = true)
        _uiState.value = _uiState.value.copy(
            actionBannerMessage = "Local notification dispatched for '${mission.title}'."
        )
    }

    fun hasNotificationPermission(): Boolean = container.notificationManager.hasNotificationPermission()

    fun acceptContract(missionId: String) {
        viewModelScope.launch {
            val result = container.tacticalMissionsRepository.acceptContract(missionId)
            result.onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    actionBannerMessage = "CONTRACT ACCEPTED: ${updated.title} deployed to active fleet queue.",
                    selectedTab = 0 // Switch to active operations tab
                )
            }
        }
    }

    fun claimReward(missionId: String) {
        viewModelScope.launch {
            val result = container.tacticalMissionsRepository.claimReward(missionId)
            result.onSuccess { updated ->
                // Refresh commander to show updated credits
                container.commanderRepository.refreshCommanderProfile()
                _uiState.value = _uiState.value.copy(
                    actionBannerMessage = "BOUNTY CLAIMED: +${updated.creditReward} Credits & +${updated.standingReward} Standing added to Commander account!",
                    selectedMission = if (_uiState.value.selectedMission?.id == missionId) updated else _uiState.value.selectedMission
                )
            }
        }
    }

    fun togglePriority(missionId: String) {
        viewModelScope.launch {
            val current = _uiState.value.missions.find { it.id == missionId }
            val newPriority = !(current?.isPriorityTarget ?: false)
            container.tacticalMissionsRepository.setPriority(missionId, newPriority)
            _uiState.value = _uiState.value.copy(
                actionBannerMessage = if (newPriority) "Priority Target designated: ${current?.title}" else "Priority Target cleared."
            )
        }
    }

    fun abandonMission(missionId: String) {
        viewModelScope.launch {
            container.tacticalMissionsRepository.abandonMission(missionId)
            _uiState.value = _uiState.value.copy(
                actionBannerMessage = "Mission abandoned and returned to contract pool."
            )
        }
    }

    fun toggleSimulatingFleetFeed() {
        val willSimulate = !_uiState.value.isSimulatingFleetFeed
        _uiState.value = _uiState.value.copy(isSimulatingFleetFeed = willSimulate)

        if (willSimulate) {
            simulationJob?.cancel()
            simulationJob = viewModelScope.launch {
                while (isActive) {
                    delay(3000)
                    // Tick active fleet missions forward
                    val activeMissions = _uiState.value.missions.filter { it.status == MissionStatus.ACTIVE }
                    if (activeMissions.isNotEmpty()) {
                        val target = activeMissions.random()
                        container.tacticalMissionsRepository.updateFleetTask(
                            missionId = target.id,
                            status = target.assignedFleetStatus,
                            progressDelta = 4f
                        )
                    }
                }
            }
        } else {
            simulationJob?.cancel()
            simulationJob = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}

package com.example.helion.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.database.TacticalMissionDao
import com.example.helion.core.database.TacticalMissionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TacticalMissionUiState(
    val missions: List<TacticalMissionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedStatusFilter: String? = null,
    val errorMessage: String? = null
)

class TacticalMissionViewModel(
    private val missionDao: TacticalMissionDao
) : ViewModel() {

    // Secondary constructor accepting HelionAppContainer for dependency injection
    constructor(container: HelionAppContainer) : this(container.database.tacticalMissionDao())

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Observe all missions directly from Room DAO
    val allMissions: Flow<List<TacticalMissionEntity>> = missionDao.getAllMissions()

    // Direct missions Flow for convenience
    val missions: Flow<List<TacticalMissionEntity>> = allMissions

    // Combined UI State reactive stream
    val uiState: Flow<TacticalMissionUiState> = combine(
        missionDao.getAllMissions(),
        _statusFilter,
        _isLoading,
        _errorMessage
    ) { missionList, filter, loading, error ->
        val filtered = if (filter == null) {
            missionList
        } else {
            missionList.filter { it.status.equals(filter, ignoreCase = true) }
        }
        TacticalMissionUiState(
            missions = filtered,
            isLoading = loading,
            selectedStatusFilter = filter,
            errorMessage = error
        )
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun insertMission(mission: TacticalMissionEntity) {
        try {
            missionDao.insertMission(mission)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to insert mission: ${e.message}"
        }
    }

    suspend fun insert(mission: TacticalMissionEntity) = insertMission(mission)

    suspend fun insertMissions(missions: List<TacticalMissionEntity>) {
        try {
            missionDao.insertMissions(missions)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to insert missions: ${e.message}"
        }
    }

    suspend fun insertAll(missions: List<TacticalMissionEntity>) = insertMissions(missions)

    suspend fun updateMission(mission: TacticalMissionEntity) {
        try {
            missionDao.updateMission(mission)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update mission: ${e.message}"
        }
    }

    suspend fun update(mission: TacticalMissionEntity) = updateMission(mission)

    suspend fun completeMission(missionId: String, completionTimestamp: Long = System.currentTimeMillis()) {
        try {
            missionDao.setMissionStatus(
                missionId = missionId,
                status = "COMPLETED",
                completionTimestamp = completionTimestamp,
                epoch = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _errorMessage.value = "Failed to complete mission: ${e.message}"
        }
    }

    suspend fun updateObjectiveProgress(
        missionId: String,
        objectivesJson: String,
        fleetProgress: Float,
        status: String
    ) {
        try {
            missionDao.updateObjectiveProgress(
                missionId = missionId,
                objectivesJson = objectivesJson,
                fleetProgress = fleetProgress,
                status = status,
                epoch = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update objective: ${e.message}"
        }
    }

    suspend fun setPriorityTarget(missionId: String, isPriority: Boolean) {
        try {
            missionDao.setPriorityTarget(missionId, isPriority)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to set priority: ${e.message}"
        }
    }

    suspend fun deleteMission(missionId: String) {
        try {
            missionDao.deleteMission(missionId)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete mission: ${e.message}"
        }
    }

    suspend fun delete(mission: TacticalMissionEntity) {
        try {
            missionDao.delete(mission)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete mission: ${e.message}"
        }
    }

    suspend fun deleteAllMissions() {
        try {
            missionDao.deleteAllMissions()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete all missions: ${e.message}"
        }
    }

    // Fire-and-forget helpers for Composable UI callers without an existing CoroutineScope
    fun launchInsert(mission: TacticalMissionEntity) {
        viewModelScope.launch { insertMission(mission) }
    }

    fun launchComplete(missionId: String, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch { completeMission(missionId, timestamp) }
    }

    fun launchDelete(missionId: String) {
        viewModelScope.launch { deleteMission(missionId) }
    }

    fun getMissionById(missionId: String) = missionDao.getMissionById(missionId)

    fun getMissionsByStatus(status: String) = missionDao.getMissionsByStatus(status)

    fun getCompletedMissions() = missionDao.getCompletedMissions()
}

package com.example

import com.example.helion.core.database.TacticalMissionDao
import com.example.helion.core.database.TacticalMissionEntity
import com.example.helion.ui.missions.TacticalMissionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTacticalMissionDao : TacticalMissionDao {
    private val missionsMap = mutableMapOf<String, TacticalMissionEntity>()
    private val missionsFlow = MutableStateFlow<List<TacticalMissionEntity>>(emptyList())

    private fun emit() {
        missionsFlow.value = missionsMap.values.toList()
    }

    override fun getAllMissions(): Flow<List<TacticalMissionEntity>> = missionsFlow

    override fun getAll(): Flow<List<TacticalMissionEntity>> = missionsFlow

    override suspend fun getAllMissionsList(): List<TacticalMissionEntity> = missionsMap.values.toList()

    override fun getMissionById(missionId: String): Flow<TacticalMissionEntity?> {
        return missionsFlow.map { list -> list.find { it.missionId == missionId } }
    }

    override suspend fun getById(missionId: String): TacticalMissionEntity? {
        return missionsMap[missionId]
    }

    override fun getMissionsByStatus(status: String): Flow<List<TacticalMissionEntity>> {
        return missionsFlow.map { list -> list.filter { it.status == status } }
    }

    override fun getCompletedMissions(): Flow<List<TacticalMissionEntity>> {
        return missionsFlow.map { list -> list.filter { it.completionTimestamp > 0 } }
    }

    override suspend fun insertMissions(missions: List<TacticalMissionEntity>) {
        missions.forEach { missionsMap[it.missionId] = it }
        emit()
    }

    override suspend fun insertMission(mission: TacticalMissionEntity) {
        missionsMap[mission.missionId] = mission
        emit()
    }

    override suspend fun insert(mission: TacticalMissionEntity) {
        insertMission(mission)
    }

    override suspend fun insertAll(missions: List<TacticalMissionEntity>) {
        insertMissions(missions)
    }

    override suspend fun updateMission(mission: TacticalMissionEntity) {
        missionsMap[mission.missionId] = mission
        emit()
    }

    override suspend fun update(mission: TacticalMissionEntity) {
        updateMission(mission)
    }

    override suspend fun delete(mission: TacticalMissionEntity) {
        missionsMap.remove(mission.missionId)
        emit()
    }

    override suspend fun updateObjectiveProgress(
        missionId: String,
        objectivesJson: String,
        fleetProgress: Float,
        status: String,
        epoch: Long
    ) {
        val existing = missionsMap[missionId] ?: return
        missionsMap[missionId] = existing.copy(
            objectivesJson = objectivesJson,
            fleetProgressPercent = fleetProgress,
            status = status,
            lastUpdatedEpoch = epoch
        )
        emit()
    }

    override suspend fun updateFleetStatus(
        missionId: String,
        fleetStatus: String,
        fleetProgress: Float,
        epoch: Long
    ) {
        val existing = missionsMap[missionId] ?: return
        missionsMap[missionId] = existing.copy(
            assignedFleetStatus = fleetStatus,
            fleetProgressPercent = fleetProgress,
            lastUpdatedEpoch = epoch
        )
        emit()
    }

    override suspend fun setPriorityTarget(missionId: String, isPriority: Boolean) {
        val existing = missionsMap[missionId] ?: return
        missionsMap[missionId] = existing.copy(isPriorityTarget = isPriority)
        emit()
    }

    override suspend fun setMissionStatus(
        missionId: String,
        status: String,
        completionTimestamp: Long,
        epoch: Long
    ) {
        val existing = missionsMap[missionId] ?: return
        missionsMap[missionId] = existing.copy(
            status = status,
            completionTimestamp = completionTimestamp,
            lastUpdatedEpoch = epoch
        )
        emit()
    }

    override suspend fun deleteMission(missionId: String) {
        missionsMap.remove(missionId)
        emit()
    }

    override suspend fun deleteById(missionId: String) {
        deleteMission(missionId)
    }

    override suspend fun deleteAllMissions() {
        missionsMap.clear()
        emit()
    }

    override suspend fun deleteAll() {
        deleteAllMissions()
    }
}

class TacticalMissionViewModelTest {

    private lateinit var fakeDao: FakeTacticalMissionDao
    private lateinit var viewModel: TacticalMissionViewModel

    @Before
    fun setUp() {
        fakeDao = FakeTacticalMissionDao()
        viewModel = TacticalMissionViewModel(fakeDao)
    }

    @Test
    fun testInsertAndObserveMissions() = runBlocking {
        val mission = TacticalMissionEntity(
            missionId = "mis-test-1",
            missionName = "Test Strike Alpha",
            status = "ACTIVE",
            completionTimestamp = 0L,
            creditReward = 50000L
        )

        viewModel.insertMission(mission)

        val missionsList = viewModel.allMissions.first()
        assertEquals(1, missionsList.size)
        assertEquals("Test Strike Alpha", missionsList[0].missionName)
        assertEquals("ACTIVE", missionsList[0].status)
    }

    @Test
    fun testCompleteMissionCoordination() = runBlocking {
        val mission = TacticalMissionEntity(
            missionId = "mis-test-2",
            missionName = "Operation Recon Beta",
            status = "ACTIVE",
            completionTimestamp = 0L
        )

        viewModel.insert(mission)
        val timestamp = 1727405000L
        viewModel.completeMission("mis-test-2", timestamp)

        val updated = fakeDao.getMissionById("mis-test-2").first()
        assertNotNull(updated)
        assertEquals("COMPLETED", updated!!.status)
        assertEquals(timestamp, updated.completionTimestamp)
    }

    @Test
    fun testStatusFilterAndUiState() = runBlocking {
        val activeMission = TacticalMissionEntity(
            missionId = "mis-active",
            missionName = "Active Patrol",
            status = "ACTIVE"
        )
        val completedMission = TacticalMissionEntity(
            missionId = "mis-completed",
            missionName = "Completed Delivery",
            status = "COMPLETED",
            completionTimestamp = 1727400000L
        )

        viewModel.insertMissions(listOf(activeMission, completedMission))

        // No filter
        viewModel.setStatusFilter(null)
        val allState = viewModel.uiState.first()
        assertEquals(2, allState.missions.size)

        // Filter ACTIVE
        viewModel.setStatusFilter("ACTIVE")
        val activeState = viewModel.uiState.first()
        assertEquals(1, activeState.missions.size)
        assertEquals("mis-active", activeState.missions[0].missionId)

        // Filter COMPLETED
        viewModel.setStatusFilter("COMPLETED")
        val completedState = viewModel.uiState.first()
        assertEquals(1, completedState.missions.size)
        assertEquals("mis-completed", completedState.missions[0].missionId)
    }

    @Test
    fun testDeleteMissionCoordination() = runBlocking {
        val mission = TacticalMissionEntity(
            missionId = "mis-delete",
            missionName = "To Delete",
            status = "CANCELLED"
        )

        viewModel.insert(mission)
        assertEquals(1, viewModel.allMissions.first().size)

        viewModel.deleteMission("mis-delete")
        assertEquals(0, viewModel.allMissions.first().size)
    }

    @Test
    fun testDaoCrudOperationsDirectly() = runBlocking {
        val mission = TacticalMissionEntity(
            missionId = "mis-crud-1",
            missionName = "Deep Space Scan",
            status = "ACTIVE",
            completionTimestamp = 0L
        )

        // Insert
        fakeDao.insert(mission)
        val fetched = fakeDao.getById("mis-crud-1")
        assertNotNull(fetched)
        assertEquals("Deep Space Scan", fetched?.missionName)

        // Update
        val updated = mission.copy(status = "COMPLETED", completionTimestamp = 123456789L)
        fakeDao.update(updated)
        val fetchedUpdated = fakeDao.getById("mis-crud-1")
        assertEquals("COMPLETED", fetchedUpdated?.status)
        assertEquals(123456789L, fetchedUpdated?.completionTimestamp)

        // Fetch all
        val allList = fakeDao.getAllMissionsList()
        assertEquals(1, allList.size)

        // Delete
        fakeDao.deleteById("mis-crud-1")
        val afterDelete = fakeDao.getById("mis-crud-1")
        assertEquals(null, afterDelete)
    }
}

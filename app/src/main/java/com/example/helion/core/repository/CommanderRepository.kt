package com.example.helion.core.repository

import com.example.helion.core.database.CachedCommanderEntity
import com.example.helion.core.database.CommanderDao
import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommanderRepository(
    private val api: CompanionApi,
    private val commanderDao: CommanderDao
) {
    private val _commanderState = MutableStateFlow<CommanderProfile?>(null)
    val commanderState: StateFlow<CommanderProfile?> = _commanderState.asStateFlow()

    suspend fun refreshCommanderProfile(): Result<CommanderProfile> {
        val result = api.getCommanderProfile()
        result.onSuccess { profile ->
            _commanderState.value = profile
            commanderDao.insertCachedCommander(
                CachedCommanderEntity(
                    commanderId = profile.commanderId,
                    displayName = profile.displayName,
                    callSign = profile.callSign,
                    credits = profile.credits,
                    xp = profile.xp,
                    rank = profile.rank,
                    career = profile.career,
                    currentSystemName = profile.currentSystemName,
                    currentStationName = profile.currentStationName,
                    activeShipId = profile.activeShipId,
                    environment = profile.environment.name,
                    cachedAtEpoch = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    fun getCachedCommander(): Flow<CachedCommanderEntity?> = commanderDao.getCachedCommander()
}

package com.example.helion.core.repository

import com.example.helion.core.model.GuildInfo
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuildRepository(
    private val api: CompanionApi
) {
    private val _guildState = MutableStateFlow<GuildInfo?>(null)
    val guildState: StateFlow<GuildInfo?> = _guildState.asStateFlow()

    suspend fun refreshGuild(): Result<GuildInfo> {
        val res = api.getGuildInfo()
        res.onSuccess { _guildState.value = it }
        return res
    }

    suspend fun postNotice(title: String, body: String): Result<Unit> {
        val res = api.postGuildNotice(title, body)
        res.onSuccess { refreshGuild() }
        return res
    }

    fun clearEnvironmentState() {
        _guildState.value = null
    }
}

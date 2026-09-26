package com.example.helion.core.repository

import com.example.helion.core.database.RouteBookmarkDao
import com.example.helion.core.database.RouteBookmarkEntity
import com.example.helion.core.model.RouteOptimizationMode
import com.example.helion.core.model.RoutePlanResult
import com.example.helion.core.model.SystemNode
import com.example.helion.core.navigation.GalaxyRouter
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UniverseRepository(
    private val api: CompanionApi,
    private val bookmarkDao: RouteBookmarkDao
) {
    private val _systemsMap = MutableStateFlow<Map<String, SystemNode>>(emptyMap())
    val systemsMap: StateFlow<Map<String, SystemNode>> = _systemsMap.asStateFlow()

    suspend fun refreshGalaxySystems(): Result<Map<String, SystemNode>> {
        val res = api.getGalaxySystems()
        res.onSuccess { _systemsMap.value = it }
        return res
    }

    fun calculateRoute(
        originId: String,
        destinationId: String,
        mode: RouteOptimizationMode
    ): RoutePlanResult? {
        val map = _systemsMap.value
        return GalaxyRouter.findRoute(originId, destinationId, map, mode)
    }

    fun getBookmarks(): Flow<List<RouteBookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun saveBookmark(result: RoutePlanResult, customTitle: String? = null) {
        val title = customTitle ?: "${result.origin.name} ➔ ${result.destination.name}"
        bookmarkDao.insertBookmark(
            RouteBookmarkEntity(
                bookmarkId = "bm-${UUID.randomUUID().toString().take(8)}",
                title = title,
                originSystemId = result.origin.systemId,
                destinationSystemId = result.destination.systemId,
                originName = result.origin.name,
                destinationName = result.destination.name,
                jumps = result.totalJumps,
                savedAtEpoch = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBookmark(bookmarkId: String) {
        bookmarkDao.deleteBookmark(bookmarkId)
    }
}

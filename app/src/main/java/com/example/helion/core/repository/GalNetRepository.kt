package com.example.helion.core.repository

import com.example.helion.core.database.CachedGalNetArticleEntity
import com.example.helion.core.database.GalNetDao
import com.example.helion.core.model.GalNetArticle
import com.example.helion.core.model.GalNetChannel
import com.example.helion.core.network.CompanionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class GalNetRepository(
    private val api: CompanionApi,
    private val galNetDaoProvider: () -> GalNetDao
) {
    constructor(api: CompanionApi, galNetDao: GalNetDao) : this(api, { galNetDao })

    private val galNetDao: GalNetDao
        get() = galNetDaoProvider()
    private val _articles = MutableStateFlow<List<GalNetArticle>>(emptyList())
    val articles: StateFlow<List<GalNetArticle>> = _articles.asStateFlow()

    suspend fun refreshArticles(): Result<List<GalNetArticle>> {
        val res = api.getGalNetArticles()
        res.onSuccess { list ->
            _articles.value = list
            galNetDao.insertArticles(
                list.map {
                    CachedGalNetArticleEntity(
                        articleId = it.articleId,
                        headline = it.headline,
                        summary = it.summary,
                        body = it.body,
                        category = it.category.name,
                        publishedAtEpoch = it.publishedAtEpoch,
                        importance = it.importance,
                        isRead = it.isRead,
                        isSaved = it.isSaved
                    )
                }
            )
        }
        return res
    }

    suspend fun markArticleRead(articleId: String) {
        api.markArticleRead(articleId)
        val current = _articles.value
        _articles.value = current.map {
            if (it.articleId == articleId) it.copy(isRead = true) else it
        }
        galNetDao.markAsRead(articleId)
    }

    suspend fun toggleSaveArticle(articleId: String) {
        val current = _articles.value
        val item = current.find { it.articleId == articleId } ?: return
        val newSaved = !item.isSaved
        _articles.value = current.map {
            if (it.articleId == articleId) it.copy(isSaved = newSaved) else it
        }
        galNetDao.updateSavedState(articleId, newSaved)
    }

    fun getCachedArticles(): Flow<List<GalNetArticle>> = galNetDao.getAllCachedArticles().map { list ->
        list.map { entity ->
            val cat = try {
                GalNetChannel.valueOf(entity.category)
            } catch (e: Exception) {
                GalNetChannel.TOP_STORIES
            }
            GalNetArticle(
                articleId = entity.articleId,
                headline = entity.headline,
                summary = entity.summary,
                body = entity.body,
                category = cat,
                publishedAtEpoch = entity.publishedAtEpoch,
                importance = entity.importance,
                isRead = entity.isRead,
                isSaved = entity.isSaved
            )
        }
    }

    fun clearEnvironmentState() {
        _articles.value = emptyList()
    }
}

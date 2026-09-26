package com.example.helion.core.model

enum class GalNetChannel(val label: String) {
    TOP_STORIES("Top Stories"),
    LOCAL("Local Feeds"),
    ECONOMY("Galactic Economy"),
    SECURITY("System Security"),
    WAR("Frontier Conflict"),
    EXPLORATION("Deep Void"),
    CORPORATE("Corporate Reports"),
    COMMUNITY("Guild Dispatch"),
    BOUNTIES("Bounty Board")
}

data class GalNetArticle(
    val articleId: String,
    val headline: String,
    val summary: String,
    val body: String,
    val category: GalNetChannel,
    val publishedAtEpoch: Long,
    val systemIds: List<String> = emptyList(),
    val regionIds: List<String> = emptyList(),
    val importance: String = "STANDARD", // "CRITICAL", "FLASH", "STANDARD"
    val isRead: Boolean = false,
    val isSaved: Boolean = false
)

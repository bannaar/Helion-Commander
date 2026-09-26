package com.example.helion.core.model

data class GuildRole(
    val roleId: String,
    val title: String,
    val priority: Int,
    val permissions: Set<String>
)

data class GuildMember(
    val memberId: String,
    val displayName: String,
    val roleTitle: String,
    val currentShipHull: String,
    val locationSystem: String,
    val isOnline: Boolean,
    val joinDateEpoch: Long
)

data class GuildNotice(
    val noticeId: String,
    val title: String,
    val body: String,
    val authorName: String,
    val authorRole: String,
    val postedAtEpoch: Long,
    val isPinned: Boolean
)

data class GuildTerritory(
    val systemId: String,
    val systemName: String,
    val securityRating: Float,
    val sovereigntyStatus: String, // "Sovereign Control", "Contested", "Fortified Hub"
    val dailyRevenueCr: Long
)

data class GuildInfo(
    val guildId: String,
    val name: String,
    val ticker: String,
    val description: String,
    val emblemIcon: String,
    val memberCount: Int,
    val allianceId: String?,
    val allianceName: String?,
    val homeSystemId: String,
    val homeSystemName: String,
    val grantedPermissions: Set<String>, // Server-returned permissions for current user
    val territories: List<GuildTerritory>,
    val notices: List<GuildNotice>,
    val members: List<GuildMember>
) {
    fun hasPermission(permission: String): Boolean = grantedPermissions.contains(permission)
}

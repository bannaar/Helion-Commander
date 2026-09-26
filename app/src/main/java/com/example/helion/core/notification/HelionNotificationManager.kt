package com.example.helion.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.helion.R
import com.example.helion.core.model.OwnedShipInstance
import com.example.helion.core.model.TacticalMission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class NotificationType {
    MISSION_COMPLETE,
    FLEET_MAINTENANCE,
    SYSTEM_ALERT
}

data class HelionNotificationEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val targetRoute: String,
    val entityId: String? = null
)

class HelionNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_MISSIONS = "helion_tactical_missions"
        const val CHANNEL_NAME_MISSIONS = "Tactical Operations & Objectives"

        const val CHANNEL_ID_FLEET = "helion_fleet_maintenance"
        const val CHANNEL_NAME_FLEET = "Fleet Maintenance & Engineering"

        const val EXTRA_DESTINATION = "extra_helion_destination"
        const val EXTRA_ENTITY_ID = "extra_helion_entity_id"

        private const val BASE_MISSION_NOTIF_ID = 2000
        private const val BASE_FLEET_NOTIF_ID = 3000
    }

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    // In-memory event log for HUD notification feed
    private val _notificationHistory = MutableStateFlow<List<HelionNotificationEvent>>(emptyList())
    val notificationHistory: StateFlow<List<HelionNotificationEvent>> = _notificationHistory.asStateFlow()

    // Deduplication keys to avoid annoying spam for the same event
    private val notifiedEvents = mutableSetOf<String>()

    // Settings
    var missionAlertsEnabled: Boolean = true
    var fleetMaintenanceAlertsEnabled: Boolean = true
    var vibrationEnabled: Boolean = true

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Channel 1: Tactical Missions
            val missionsChannel = NotificationChannel(
                CHANNEL_ID_MISSIONS,
                CHANNEL_NAME_MISSIONS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when tactical mission objectives or fleet operations reach 100% completion"
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel 2: Fleet Maintenance
            val fleetChannel = NotificationChannel(
                CHANNEL_ID_FLEET,
                CHANNEL_NAME_FLEET,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts when a starship requires shipyard maintenance, hull overhaul, or repairs"
                enableVibration(true)
                setShowBadge(true)
            }

            systemNotificationManager.createNotificationChannel(missionsChannel)
            systemNotificationManager.createNotificationChannel(fleetChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Dispatches a high-priority local notification when a tactical mission reaches 100%.
     */
    fun notifyMissionProgressComplete(
        mission: TacticalMission,
        isFleetTask: Boolean = false,
        force: Boolean = false
    ) {
        if (!missionAlertsEnabled) return

        val dedupeKey = "mission_${mission.id}_100_${if (isFleetTask) "fleet" else "obj"}"
        if (!force && notifiedEvents.contains(dedupeKey)) {
            return
        }
        notifiedEvents.add(dedupeKey)

        val title = if (isFleetTask) {
            "🚀 FLEET TASK 100% COMPLETE: ${mission.assignedShipName}"
        } else {
            "🎯 TACTICAL OPERATION 100%: ${mission.title}"
        }

        val shortSummary = if (isFleetTask) {
            "Task [${mission.assignedFleetStatus.displayName}] at ${mission.primaryLocation.systemName} complete. Ready for new orders."
        } else {
            "All objectives fulfilled at ${mission.primaryLocation.systemName}. Bounty of ${mission.creditReward} CR ready to claim."
        }

        val detailedDebrief = buildString {
            append("• Sector: ${mission.primaryLocation.systemName} (${mission.primaryLocation.beaconCode})\n")
            append("• Target Anchor: ${mission.primaryLocation.celestialBodyName}\n")
            append("• Faction Sponsor: ${mission.sponsorFaction}\n")
            append("• Bounty Value: ${mission.creditReward} Credits + ${mission.reputationReward} REP\n")
            append("• Assigned Craft: ${mission.assignedShipName} (Task: ${mission.assignedFleetStatus.displayName})\n")
            append("• Action Required: Return to Operations Command to finalize debrief and claim rewards.")
        }

        // Tap action intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, "missions")
            putExtra(EXTRA_ENTITY_ID, mission.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            mission.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = BASE_MISSION_NOTIF_ID + (mission.id.hashCode() % 500).let { if (it < 0) -it else it }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_MISSIONS)
            .setSmallIcon(R.drawable.ic_notification_mission)
            .setContentTitle(title)
            .setContentText(shortSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailedDebrief).setSummaryText("Tactical Mission 100% Complete"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF00E5FF.toInt()) // HelionCyan

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 200, 100, 250, 100, 400))
        }

        // Add to in-app event log
        val event = HelionNotificationEvent(
            title = title,
            message = shortSummary,
            type = NotificationType.MISSION_COMPLETE,
            targetRoute = "missions",
            entityId = mission.id
        )
        recordNotificationEvent(event)

        if (hasNotificationPermission()) {
            try {
                notificationManagerCompat.notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Permission revoked at runtime
            }
        }
    }

    /**
     * Dispatches a local notification when a ship in the commander's fleet requires maintenance.
     */
    fun notifyFleetShipMaintenance(
        ship: OwnedShipInstance,
        force: Boolean = false
    ) {
        if (!fleetMaintenanceAlertsEnabled) return

        val dedupeKey = "ship_${ship.instanceId}_maintenance"
        if (!force && notifiedEvents.contains(dedupeKey)) {
            return
        }
        notifiedEvents.add(dedupeKey)

        val title = "⚠️ FLEET MAINTENANCE REQUIRED: ${ship.shipName}"
        val shortSummary = "${ship.hullDefinition.hullName} (${ship.registrationMark}) at ${ship.currentLocationStationName} reports critical maintenance needed."

        val detailedDebrief = buildString {
            append("• Vessel: ${ship.shipName} (${ship.hullDefinition.hullName})\n")
            append("• Hull Integrity: ${String.format("%.1f", ship.hullConditionPercent)}% (Threshold: < 80%)\n")
            append("• Component Wear: ${String.format("%.1f", ship.wearPercent)}% (Warning limit: > 35%)\n")
            append("• Current Berth: ${ship.currentLocationStationName} (${ship.currentLocationSystemName})\n")
            append("• Status: Flight systems compromised. Drydock overhaul recommended to prevent critical module failure.")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, "fleet")
            putExtra(EXTRA_ENTITY_ID, ship.instanceId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ship.instanceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = BASE_FLEET_NOTIF_ID + (ship.instanceId.hashCode() % 500).let { if (it < 0) -it else it }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_FLEET)
            .setSmallIcon(R.drawable.ic_notification_maintenance)
            .setContentTitle(title)
            .setContentText(shortSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailedDebrief).setSummaryText("Fleet Shipyard Maintenance Required"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFFFB300.toInt()) // HelionAmber

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 350, 150, 350))
        }

        val event = HelionNotificationEvent(
            title = title,
            message = shortSummary,
            type = NotificationType.FLEET_MAINTENANCE,
            targetRoute = "fleet",
            entityId = ship.instanceId
        )
        recordNotificationEvent(event)

        if (hasNotificationPermission()) {
            try {
                notificationManagerCompat.notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Permission revoked at runtime
            }
        }
    }

    fun resetDeduplicationForMission(missionId: String) {
        notifiedEvents.removeAll { it.startsWith("mission_${missionId}_") }
    }

    fun resetDeduplicationForShip(instanceId: String) {
        notifiedEvents.remove("ship_${instanceId}_maintenance")
    }

    private fun recordNotificationEvent(event: HelionNotificationEvent) {
        val current = _notificationHistory.value.toMutableList()
        current.add(0, event)
        if (current.size > 25) {
            current.removeAt(current.size - 1)
        }
        _notificationHistory.value = current
    }

    fun clearNotificationHistory() {
        _notificationHistory.value = emptyList()
    }
}

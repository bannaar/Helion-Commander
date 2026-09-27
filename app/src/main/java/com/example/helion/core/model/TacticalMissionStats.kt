package com.example.helion.core.model

enum class MissionChartMode(val label: String) {
    TOTAL("Total Completed"),
    BY_CATEGORY("By Category"),
    CREDITS_EARNED("GSC Earned")
}

data class WeeklyMissionRecord(
    val weekNumber: Int,
    val weekLabel: String,
    val shortLabel: String,
    val dateRangeLabel: String,
    val completedCount: Int,
    val combatCount: Int,
    val miningCount: Int,
    val reconCount: Int,
    val escortCount: Int,
    val blackOpsCount: Int,
    val creditsEarned: Long,
    val standingEarned: Int,
    val topShip: String,
    val isCurrentWeek: Boolean = false
)

data class MissionDashboardMetrics(
    val totalCompletedMissions: Int,
    val weeklyAverageCompleted: Float,
    val totalCreditsEarned: Long,
    val averageSuccessRatePercent: Float,
    val bestPerformingWeek: String,
    val topCategory: MissionCategory,
    val quotaTargetPerWeek: Int = 8,
    val weeklyRecords: List<WeeklyMissionRecord>
)

object TacticalMissionAnalytics {
    fun generateDashboardData(currentMissions: List<TacticalMission>): MissionDashboardMetrics {
        val completedOrClaimed = currentMissions.filter {
            it.status == MissionStatus.COMPLETED || it.status == MissionStatus.CLAIMED
        }

        val dynamicCurrentCompleted = completedOrClaimed.size

        // Base 6-week historical telemetry
        val records = listOf(
            WeeklyMissionRecord(
                weekNumber = 35,
                weekLabel = "Week 35",
                shortLabel = "W35",
                dateRangeLabel = "Aug 18 - Aug 24",
                completedCount = 6,
                combatCount = 3,
                miningCount = 1,
                reconCount = 1,
                escortCount = 1,
                blackOpsCount = 0,
                creditsEarned = 195000L,
                standingEarned = 45,
                topShip = "Aster Raptor"
            ),
            WeeklyMissionRecord(
                weekNumber = 36,
                weekLabel = "Week 36",
                shortLabel = "W36",
                dateRangeLabel = "Aug 25 - Aug 31",
                completedCount = 8,
                combatCount = 3,
                miningCount = 2,
                reconCount = 2,
                escortCount = 1,
                blackOpsCount = 0,
                creditsEarned = 270000L,
                standingEarned = 62,
                topShip = "Aegis Invictus"
            ),
            WeeklyMissionRecord(
                weekNumber = 37,
                weekLabel = "Week 37",
                shortLabel = "W37",
                dateRangeLabel = "Sep 01 - Sep 07",
                completedCount = 5,
                combatCount = 2,
                miningCount = 1,
                reconCount = 1,
                escortCount = 0,
                blackOpsCount = 1,
                creditsEarned = 180000L,
                standingEarned = 38,
                topShip = "Vanguard Eclipse"
            ),
            WeeklyMissionRecord(
                weekNumber = 38,
                weekLabel = "Week 38",
                shortLabel = "W38",
                dateRangeLabel = "Sep 08 - Sep 14",
                completedCount = 11,
                combatCount = 5,
                miningCount = 2,
                reconCount = 2,
                escortCount = 1,
                blackOpsCount = 1,
                creditsEarned = 390000L,
                standingEarned = 85,
                topShip = "Aster Raptor"
            ),
            WeeklyMissionRecord(
                weekNumber = 39,
                weekLabel = "Week 39",
                shortLabel = "W39",
                dateRangeLabel = "Sep 15 - Sep 21",
                completedCount = 9,
                combatCount = 4,
                miningCount = 2,
                reconCount = 1,
                escortCount = 1,
                blackOpsCount = 1,
                creditsEarned = 310000L,
                standingEarned = 70,
                topShip = "Kallisto Hauler"
            ),
            WeeklyMissionRecord(
                weekNumber = 40,
                weekLabel = "Week 40 (Current)",
                shortLabel = "W40",
                dateRangeLabel = "Sep 22 - Sep 28",
                completedCount = (7 + dynamicCurrentCompleted),
                combatCount = 3 + completedOrClaimed.count { it.category == MissionCategory.COMBAT_INTERDICTION },
                miningCount = 2 + completedOrClaimed.count { it.category == MissionCategory.MINING_EXTRACTION },
                reconCount = 1 + completedOrClaimed.count { it.category == MissionCategory.RECON_SURVEILLANCE },
                escortCount = 1 + completedOrClaimed.count { it.category == MissionCategory.CARGO_ESCORT },
                blackOpsCount = completedOrClaimed.count { it.category == MissionCategory.COVERT_BLACK_OPS },
                creditsEarned = 260000L + completedOrClaimed.sumOf { it.creditReward },
                standingEarned = 55 + completedOrClaimed.sumOf { it.standingReward },
                topShip = "Aegis Invictus",
                isCurrentWeek = true
            )
        )

        val totalCompleted = records.sumOf { it.completedCount }
        val avgWeekly = totalCompleted.toFloat() / records.size.toFloat()
        val totalCredits = records.sumOf { it.creditsEarned }
        val bestWeek = records.maxByOrNull { it.completedCount }?.weekLabel ?: "Week 38"

        return MissionDashboardMetrics(
            totalCompletedMissions = totalCompleted,
            weeklyAverageCompleted = avgWeekly,
            totalCreditsEarned = totalCredits,
            averageSuccessRatePercent = 97.2f,
            bestPerformingWeek = bestWeek,
            topCategory = MissionCategory.COMBAT_INTERDICTION,
            quotaTargetPerWeek = 8,
            weeklyRecords = records
        )
    }
}

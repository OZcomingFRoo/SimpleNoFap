package com.example.simplenofap.daystreaks

enum class DayStreakType {
    ThreeDays,
    OneWeek,
    ThreeWeeks,
    OneMonth,
    TwoMonths
}

data class DayStreakMetadata(
    val type: DayStreakType,
    val requiredDayCount: Int,
    val titleKey: String,
    val iconKey: String
)

object DayStreakMetadataMapper {
    val allMetadata: List<DayStreakMetadata> = listOf(
        DayStreakMetadata(
            type = DayStreakType.ThreeDays,
            requiredDayCount = 3,
            titleKey = "day_streak_three_days",
            iconKey = "day_streak_badge_seed"
        ),
        DayStreakMetadata(
            type = DayStreakType.OneWeek,
            requiredDayCount = 7,
            titleKey = "day_streak_one_week",
            iconKey = "day_streak_badge_spark"
        ),
        DayStreakMetadata(
            type = DayStreakType.ThreeWeeks,
            requiredDayCount = 21,
            titleKey = "day_streak_three_weeks",
            iconKey = "day_streak_badge_shield_star"
        ),
        DayStreakMetadata(
            type = DayStreakType.OneMonth,
            requiredDayCount = 30,
            titleKey = "day_streak_one_month",
            iconKey = "day_streak_badge_flame"
        ),
        DayStreakMetadata(
            type = DayStreakType.TwoMonths,
            requiredDayCount = 60,
            titleKey = "day_streak_two_months",
            iconKey = "day_streak_badge_crown"
        )
    )

    private val metadataByType = allMetadata.associateBy { it.type }

    fun metadataFor(type: DayStreakType): DayStreakMetadata = metadataByType.getValue(type)
}

object DayStreakMilestones {
    const val DayMillis: Long = 24L * 60L * 60L * 1_000L

    val all: List<DayStreakMetadata> = DayStreakMetadataMapper.allMetadata

    fun bitFor(type: DayStreakType): Int {
        return 1 shl all.indexOfFirst { it.type == type }.also { index ->
            require(index >= 0) { "Unknown Day-Streak type: $type" }
        }
    }

    fun thresholdMillis(type: DayStreakType): Long {
        return DayStreakMetadataMapper.metadataFor(type).requiredDayCount * DayMillis
    }

    fun thresholdEpochMillis(startedAtEpochMillis: Long, type: DayStreakType): Long {
        return safeAdd(startedAtEpochMillis, thresholdMillis(type))
    }

    fun crossedTypes(startedAtEpochMillis: Long, nowEpochMillis: Long): List<DayStreakType> {
        val elapsed = (nowEpochMillis - startedAtEpochMillis).coerceAtLeast(0L)
        return all.filter { elapsed >= it.requiredDayCount * DayMillis }.map { it.type }
    }

    fun crossedMask(startedAtEpochMillis: Long, nowEpochMillis: Long): Int {
        return crossedTypes(startedAtEpochMillis, nowEpochMillis)
            .fold(0) { mask, type -> mask or bitFor(type) }
    }

    fun unprocessedCrossedTypes(
        startedAtEpochMillis: Long,
        nowEpochMillis: Long,
        processedMask: Int
    ): List<DayStreakType> {
        return crossedTypes(startedAtEpochMillis, nowEpochMillis)
            .filter { processedMask and bitFor(it) == 0 }
    }

    fun markProcessed(mask: Int, types: Iterable<DayStreakType>): Int {
        return types.fold(mask) { current, type -> current or bitFor(type) }
    }

    fun nextUnprocessedFutureMilestone(
        startedAtEpochMillis: Long,
        nowEpochMillis: Long,
        processedMask: Int
    ): DayStreakNextMilestone? {
        return all.firstOrNull { metadata ->
            processedMask and bitFor(metadata.type) == 0 &&
                nowEpochMillis < thresholdEpochMillis(startedAtEpochMillis, metadata.type)
        }?.let { metadata ->
            DayStreakNextMilestone(
                type = metadata.type,
                triggerAtEpochMillis = thresholdEpochMillis(startedAtEpochMillis, metadata.type)
            )
        }
    }

    private fun safeAdd(left: Long, right: Long): Long {
        if (right > 0L && left > Long.MAX_VALUE - right) return Long.MAX_VALUE
        if (right < 0L && left < Long.MIN_VALUE - right) return Long.MIN_VALUE
        return left + right
    }
}

data class DayStreakNextMilestone(
    val type: DayStreakType,
    val triggerAtEpochMillis: Long
)

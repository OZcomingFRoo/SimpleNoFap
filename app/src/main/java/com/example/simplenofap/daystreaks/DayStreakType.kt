package com.example.simplenofap.daystreaks

enum class DayStreakType {
    ThreeDays,
    OneWeek,
    TwoWeeks,
    ThreeWeeks,
    OneMonth,
    TwoMonths,
    ThreeMonths
}

data class DayStreakMetadata(
    val type: DayStreakType,
    val requiredDayCount: Int,
    val titleKey: String,
    val iconKey: String
)

object DayStreakMetadataMapper {
    private val metadataByType = mapOf(
        DayStreakType.ThreeDays to DayStreakMetadata(
            type = DayStreakType.ThreeDays,
            requiredDayCount = 3,
            titleKey = "day_streak_three_days",
            iconKey = "day_streak_badge_seed"
        ),
        DayStreakType.OneWeek to DayStreakMetadata(
            type = DayStreakType.OneWeek,
            requiredDayCount = 7,
            titleKey = "day_streak_one_week",
            iconKey = "day_streak_badge_spark"
        ),
        DayStreakType.TwoWeeks to DayStreakMetadata(
            type = DayStreakType.TwoWeeks,
            requiredDayCount = 14,
            titleKey = "day_streak_two_weeks",
            iconKey = "day_streak_badge_shield"
        ),
        DayStreakType.ThreeWeeks to DayStreakMetadata(
            type = DayStreakType.ThreeWeeks,
            requiredDayCount = 21,
            titleKey = "day_streak_three_weeks",
            iconKey = "day_streak_badge_star"
        ),
        DayStreakType.OneMonth to DayStreakMetadata(
            type = DayStreakType.OneMonth,
            requiredDayCount = 30,
            titleKey = "day_streak_one_month",
            iconKey = "day_streak_badge_flame"
        ),
        DayStreakType.TwoMonths to DayStreakMetadata(
            type = DayStreakType.TwoMonths,
            requiredDayCount = 60,
            titleKey = "day_streak_two_months",
            iconKey = "day_streak_badge_crown"
        ),
        DayStreakType.ThreeMonths to DayStreakMetadata(
            type = DayStreakType.ThreeMonths,
            requiredDayCount = 90,
            titleKey = "day_streak_three_months",
            iconKey = "day_streak_badge_diamond"
        )
    )

    fun metadataFor(type: DayStreakType): DayStreakMetadata = metadataByType.getValue(type)
}

package com.example.simplenofap.daystreaks

import com.example.simplenofap.localization.AppStrings

data class DayStreakNotificationText(
    val title: String,
    val body: String
)

fun AppStrings.dayStreakNotificationText(type: DayStreakType): DayStreakNotificationText {
    return when (type) {
        DayStreakType.ThreeDays -> DayStreakNotificationText(
            title = dayStreakThreeDaysNotificationTitle,
            body = dayStreakThreeDaysNotificationBody
        )
        DayStreakType.OneWeek -> DayStreakNotificationText(
            title = dayStreakOneWeekNotificationTitle,
            body = dayStreakOneWeekNotificationBody
        )
        DayStreakType.ThreeWeeks -> DayStreakNotificationText(
            title = dayStreakThreeWeeksNotificationTitle,
            body = dayStreakThreeWeeksNotificationBody
        )
        DayStreakType.OneMonth -> DayStreakNotificationText(
            title = dayStreakOneMonthNotificationTitle,
            body = dayStreakOneMonthNotificationBody
        )
        DayStreakType.TwoMonths -> DayStreakNotificationText(
            title = dayStreakTwoMonthsNotificationTitle,
            body = dayStreakTwoMonthsNotificationBody
        )
    }
}

fun AppStrings.dayStreakName(type: DayStreakType): String {
    return when (type) {
        DayStreakType.ThreeDays -> dayStreakThreeDaysName
        DayStreakType.OneWeek -> dayStreakOneWeekName
        DayStreakType.ThreeWeeks -> dayStreakThreeWeeksName
        DayStreakType.OneMonth -> dayStreakOneMonthName
        DayStreakType.TwoMonths -> dayStreakTwoMonthsName
    }
}

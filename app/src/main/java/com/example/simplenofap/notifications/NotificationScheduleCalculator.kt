package com.example.simplenofap.notifications

import java.time.ZonedDateTime

object NotificationScheduleCalculator {
    fun nextOccurrence(
        notification: ScheduledNotification,
        now: ZonedDateTime
    ): ZonedDateTime {
        val hour = notification.timeMinutesOfDay / 60
        val minute = notification.timeMinutesOfDay % 60
        for (daysAhead in 0..7) {
            val candidateDate = now.toLocalDate().plusDays(daysAhead.toLong())
            val weekday = candidateDate.dayOfWeek.toNotificationWeekday()
            if (!DaysOfWeekMask.contains(notification.daysOfWeekMask, weekday)) continue
            val candidate = candidateDate.atTime(hour, minute).atZone(now.zone)
            if (candidate.isAfter(now)) return candidate
        }
        error("A valid weekday mask always has a next occurrence")
    }
}

private fun java.time.DayOfWeek.toNotificationWeekday(): NotificationWeekday = when (this) {
    java.time.DayOfWeek.SUNDAY -> NotificationWeekday.Sunday
    java.time.DayOfWeek.MONDAY -> NotificationWeekday.Monday
    java.time.DayOfWeek.TUESDAY -> NotificationWeekday.Tuesday
    java.time.DayOfWeek.WEDNESDAY -> NotificationWeekday.Wednesday
    java.time.DayOfWeek.THURSDAY -> NotificationWeekday.Thursday
    java.time.DayOfWeek.FRIDAY -> NotificationWeekday.Friday
    java.time.DayOfWeek.SATURDAY -> NotificationWeekday.Saturday
}

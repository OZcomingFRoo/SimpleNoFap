package com.example.simplenofap.notifications

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationScheduleCalculatorTest {
    private val zone = ZoneId.of("Asia/Jerusalem")

    @Test fun sameDayFutureTime_isChosen() {
        val now = ZonedDateTime.of(2026, 7, 15, 9, 0, 0, 0, zone) // Wednesday
        assertEquals(
            ZonedDateTime.of(2026, 7, 15, 10, 30, 0, 0, zone),
            next(NotificationWeekday.Wednesday.maskBit, 10 * 60 + 30, now)
        )
    }

    @Test fun sameDayPastTime_wrapsOneWeek() {
        val now = ZonedDateTime.of(2026, 7, 15, 11, 0, 0, 0, zone)
        assertEquals(
            ZonedDateTime.of(2026, 7, 22, 10, 30, 0, 0, zone),
            next(NotificationWeekday.Wednesday.maskBit, 10 * 60 + 30, now)
        )
    }

    @Test fun everyWeekdayBit_mapsToTheExpectedDay() {
        val sunday = ZonedDateTime.of(2026, 7, 12, 0, 0, 0, 0, zone)
        NotificationWeekday.entries.forEachIndexed { index, day ->
            assertEquals(sunday.toLocalDate().plusDays(index.toLong()), next(day.maskBit, 60, sunday).toLocalDate())
        }
    }

    @Test fun dstTransition_usesTheZoneRulesAtTheOccurrence() {
        val now = ZonedDateTime.of(2026, 3, 26, 12, 0, 0, 0, zone)
        val occurrence = next(NotificationWeekday.Friday.maskBit, 10 * 60, now)
        assertEquals(10, occurrence.hour)
        assertEquals(zone, occurrence.zone)
    }

    private fun next(mask: Int, minutes: Int, now: ZonedDateTime) =
        NotificationScheduleCalculator.nextOccurrence(
            ScheduledNotification(daysOfWeekMask = mask, timeMinutesOfDay = minutes,
                messagePresetKey = null, customMessage = null, titlePresetKey = null,
                customTitle = null, active = true),
            now
        )
}

package com.example.simplenofap.counter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CounterCalculatorTest {
    private val start = 1_000_000L

    @Test
    fun formatsEveryDurationTierAndOmitsZeroUnits() {
        assertText(0L, "00:00")
        assertText(59L * SECOND_MILLIS, "00:59")
        assertText(59L * MINUTE_MILLIS + 59L * SECOND_MILLIS, "59:59")
        assertText(HOUR_MILLIS, "01:00:00")
        assertText(DAY_MILLIS, "1 day · 00:00:00")
        assertText(2L * DAY_MILLIS + 3L * HOUR_MILLIS, "2 days · 03:00:00")
        assertText(WEEK_MILLIS, "1 week\n00:00:00")
        assertText(2L * WEEK_MILLIS + 3L * DAY_MILLIS, "2 weeks, 3 days\n00:00:00")
        assertText(MONTH_MILLIS, "1 month\n00:00:00")
        assertText(2L * MONTH_MILLIS + 2L * WEEK_MILLIS + 3L * DAY_MILLIS, "2 months, 2 weeks, 3 days\n00:00:00")
        assertText(YEAR_MILLIS, "1 year\n00:00:00")
        assertText(YEAR_MILLIS + 2L * MONTH_MILLIS + 2L * WEEK_MILLIS + 4L * DAY_MILLIS, "1 year, 2 months, 4 days\n00:00:00")
    }

    @Test
    fun yearlyTierDiscardsCompleteLeftoverWeeks() {
        assertText(YEAR_MILLIS + 2L * MONTH_MILLIS + 2L * WEEK_MILLIS, "1 year, 2 months\n00:00:00")
        assertText(YEAR_MILLIS + 29L * DAY_MILLIS, "1 year, 1 day\n00:00:00")
    }

    @Test
    fun fixedMonthAndYearArithmeticDoesNotUseCalendarLengths() {
        assertText(30L * DAY_MILLIS, "1 month\n00:00:00")
        assertText(359L * DAY_MILLIS, "11 months, 4 weeks, 1 day\n00:00:00")
        assertText(360L * DAY_MILLIS, "1 year\n00:00:00")
    }

    @Test
    fun futureStartIsClampedToZero() {
        val result = calculateCounter(startedAtEpochMillis = start + DAY_MILLIS, nowEpochMillis = start)

        assertEquals(0L, result.elapsedMillis)
        assertEquals("00:00", result.formattedText)
        assertEquals(CounterVisualTier.Neutral, result.visualTier)
    }

    @Test
    fun visualMilestonesChangeExactlyAtBoundary() {
        val boundaries = listOf(
            HOUR_MILLIS to CounterVisualTier.Warm,
            DAY_MILLIS to CounterVisualTier.Forest,
            3L * DAY_MILLIS to CounterVisualTier.BlueMedium,
            WEEK_MILLIS to CounterVisualTier.BlueStrong,
            MONTH_MILLIS to CounterVisualTier.BlueSolid,
            YEAR_MILLIS to CounterVisualTier.Rainbow
        )

        var previousTier = CounterVisualTier.Neutral
        boundaries.forEach { (boundary, tier) ->
            assertEquals(previousTier, presentation(boundary - 1L).visualTier)
            assertEquals(tier, presentation(boundary).visualTier)
            assertEquals(tier, presentation(boundary + 1L).visualTier)
            previousTier = tier
        }
    }

    @Test
    fun boundariesAndDayRefreshesAreEpochBased() {
        val initial = presentation(0L)
        assertEquals(start + HOUR_MILLIS, initial.nextVisualBoundaryEpochMillis)
        assertNull(initial.nextStaticRefreshEpochMillis)

        val afterDay = presentation(DAY_MILLIS + HOUR_MILLIS)
        assertEquals(start + 3L * DAY_MILLIS, afterDay.nextVisualBoundaryEpochMillis)
        assertEquals(start + 2L * DAY_MILLIS, afterDay.nextStaticRefreshEpochMillis)

        assertNull(presentation(YEAR_MILLIS).nextVisualBoundaryEpochMillis)
    }

    @Test
    fun largeDurationsDoNotOverflowFormatting() {
        val result = calculateCounter(0L, Long.MAX_VALUE)

        assertEquals(CounterVisualTier.Rainbow, result.visualTier)
        assertEquals(Long.MAX_VALUE / SECOND_MILLIS, result.elapsedWholeSeconds)
    }

    private fun assertText(elapsed: Long, expected: String) {
        assertEquals(expected, presentation(elapsed).formattedText)
    }

    private fun presentation(elapsed: Long): CounterPresentation {
        return calculateCounter(start, start + elapsed)
    }
}

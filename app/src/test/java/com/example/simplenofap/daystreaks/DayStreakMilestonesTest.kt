package com.example.simplenofap.daystreaks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DayStreakMilestonesTest {
    @Test
    fun crossedTypes_useFixedTwentyFourHourBoundaries() {
        val start = 1_000L
        val beforeThreeDays = start + 3L * DayStreakMilestones.DayMillis - 1
        val atThreeDays = start + 3L * DayStreakMilestones.DayMillis

        assertEquals(emptyList<DayStreakType>(), DayStreakMilestones.crossedTypes(start, beforeThreeDays))
        assertEquals(listOf(DayStreakType.ThreeDays), DayStreakMilestones.crossedTypes(start, atThreeDays))
    }

    @Test
    fun unprocessedCrossedTypes_respectsProcessedMaskAndClockRollback() {
        val start = 0L
        val processed = DayStreakMilestones.bitFor(DayStreakType.ThreeDays)

        assertEquals(
            emptyList<DayStreakType>(),
            DayStreakMilestones.unprocessedCrossedTypes(
                startedAtEpochMillis = start,
                nowEpochMillis = 4L * DayStreakMilestones.DayMillis,
                processedMask = processed
            )
        )
    }

    @Test
    fun highestCatchUpMarksLowerMilestonesProcessed() {
        val start = 0L
        val crossed = DayStreakMilestones.unprocessedCrossedTypes(
            startedAtEpochMillis = start,
            nowEpochMillis = 22L * DayStreakMilestones.DayMillis,
            processedMask = 0
        )
        val mask = DayStreakMilestones.markProcessed(0, crossed)

        assertEquals(DayStreakType.ThreeWeeks, crossed.last())
        assertEquals(
            DayStreakMilestones.bitFor(DayStreakType.ThreeDays) or
                DayStreakMilestones.bitFor(DayStreakType.OneWeek) or
                DayStreakMilestones.bitFor(DayStreakType.ThreeWeeks),
            mask
        )
    }

    @Test
    fun nextUnprocessedFutureMilestone_skipsProcessedMilestones() {
        val processed = DayStreakMilestones.bitFor(DayStreakType.ThreeDays)
        val next = DayStreakMilestones.nextUnprocessedFutureMilestone(
            startedAtEpochMillis = 0L,
            nowEpochMillis = 1L * DayStreakMilestones.DayMillis,
            processedMask = processed
        )

        assertEquals(DayStreakType.OneWeek, next?.type)
    }

    @Test
    fun nextUnprocessedFutureMilestone_isNullWhenComplete() {
        val allMask = DayStreakMilestones.all.fold(0) { mask, metadata ->
            mask or DayStreakMilestones.bitFor(metadata.type)
        }

        assertNull(
            DayStreakMilestones.nextUnprocessedFutureMilestone(
                startedAtEpochMillis = 0L,
                nowEpochMillis = 61L * DayStreakMilestones.DayMillis,
                processedMask = allMask
            )
        )
    }
}

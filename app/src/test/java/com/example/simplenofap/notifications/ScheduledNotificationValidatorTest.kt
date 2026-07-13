package com.example.simplenofap.notifications

import org.junit.Assert.fail
import org.junit.Test

class ScheduledNotificationValidatorTest {
    @Test
    fun validate_acceptsValidBoundaryValues() {
        ScheduledNotificationValidator.validate(daysOfWeekMask = 1, timeMinutesOfDay = 0)
        ScheduledNotificationValidator.validate(daysOfWeekMask = 127, timeMinutesOfDay = 1_439)
    }

    @Test
    fun validate_rejectsInvalidDaysOfWeekMasks() {
        assertIllegalArgument {
            ScheduledNotificationValidator.validate(daysOfWeekMask = 0, timeMinutesOfDay = 0)
        }
        assertIllegalArgument {
            ScheduledNotificationValidator.validate(daysOfWeekMask = 128, timeMinutesOfDay = 0)
        }
    }

    @Test
    fun validate_rejectsInvalidTimes() {
        assertIllegalArgument {
            ScheduledNotificationValidator.validate(daysOfWeekMask = 1, timeMinutesOfDay = -1)
        }
        assertIllegalArgument {
            ScheduledNotificationValidator.validate(daysOfWeekMask = 1, timeMinutesOfDay = 1_440)
        }
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException.")
        } catch (_: IllegalArgumentException) {
        }
    }
}

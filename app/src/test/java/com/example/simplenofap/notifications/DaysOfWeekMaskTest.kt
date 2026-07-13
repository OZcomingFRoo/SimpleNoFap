package com.example.simplenofap.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DaysOfWeekMaskTest {
    @Test
    fun weekdayBits_startWithSunday() {
        assertEquals(1, NotificationWeekday.Sunday.maskBit)
        assertEquals(2, NotificationWeekday.Monday.maskBit)
        assertEquals(4, NotificationWeekday.Tuesday.maskBit)
        assertEquals(8, NotificationWeekday.Wednesday.maskBit)
        assertEquals(16, NotificationWeekday.Thursday.maskBit)
        assertEquals(32, NotificationWeekday.Friday.maskBit)
        assertEquals(64, NotificationWeekday.Saturday.maskBit)
    }

    @Test
    fun of_combinesSelectedWeekdays() {
        val mask = DaysOfWeekMask.of(
            NotificationWeekday.Sunday,
            NotificationWeekday.Tuesday,
            NotificationWeekday.Saturday
        )

        assertEquals(69, mask)
        assertTrue(DaysOfWeekMask.contains(mask, NotificationWeekday.Sunday))
        assertFalse(DaysOfWeekMask.contains(mask, NotificationWeekday.Monday))
        assertTrue(DaysOfWeekMask.contains(mask, NotificationWeekday.Tuesday))
        assertTrue(DaysOfWeekMask.contains(mask, NotificationWeekday.Saturday))
    }
}

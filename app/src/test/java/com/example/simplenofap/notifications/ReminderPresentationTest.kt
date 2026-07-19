package com.example.simplenofap.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderPresentationTest {
    @Test
    fun normalWhenFullScreenSettingIsDisabled() {
        assertEquals(
            ReminderPresentation.Normal,
            resolveReminderPresentation(
                fullScreenEnabled = false,
                canUseFullScreenIntent = true
            )
        )
    }

    @Test
    fun fullScreenWhenEnabledAndOsAllowsAccess() {
        assertEquals(
            ReminderPresentation.FullScreen,
            resolveReminderPresentation(
                fullScreenEnabled = true,
                canUseFullScreenIntent = true
            )
        )
    }

    @Test
    fun headsUpFallbackWhenEnabledAndOsBlocksAccess() {
        assertEquals(
            ReminderPresentation.HeadsUpFallback,
            resolveReminderPresentation(
                fullScreenEnabled = true,
                canUseFullScreenIntent = false
            )
        )
    }
}

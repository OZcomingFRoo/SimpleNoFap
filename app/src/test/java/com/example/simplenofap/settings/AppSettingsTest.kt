package com.example.simplenofap.settings

import org.junit.Assert.assertFalse
import org.junit.Test

class AppSettingsTest {
    @Test
    fun defaults_disableFullScreenReminderNotifications() {
        assertFalse(AppSettings().fullScreenReminderNotificationsEnabled)
    }
}

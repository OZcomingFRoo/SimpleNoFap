package com.example.simplenofap.settings

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun persistsFullScreenReminderNotificationsPreference() = runBlocking {
        val repository = SettingsRepository(ApplicationProvider.getApplicationContext())

        repository.setFullScreenReminderNotificationsEnabled(true)

        val reloaded = SettingsRepository(ApplicationProvider.getApplicationContext())
            .settings
            .first()
        assertTrue(reloaded.fullScreenReminderNotificationsEnabled)
    }
}

package com.example.simplenofap.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleNoFapMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SimpleNoFapDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test fun migrate1To2_preservesRowsAndDefaultsSound() {
        helper.createDatabase("migration-test", 1).apply {
            execSQL("INSERT INTO scheduled_notifications (createdAtEpochMillis, updatedAtEpochMillis, daysOfWeekMask, timeMinutesOfDay, messagePresetKey, customMessage, titlePresetKey, customTitle, active) VALUES (1, 1, 1, 60, NULL, 'Hi', NULL, NULL, 1)")
            close()
        }
        helper.runMigrationsAndValidate("migration-test", 2, true, SimpleNoFapDatabase.Migration1To2).use { db ->
            db.query("SELECT customMessage, soundEnabled, notificationSoundUri FROM scheduled_notifications").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Hi", cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
                assertEquals(true, cursor.isNull(2))
            }
        }
    }
}

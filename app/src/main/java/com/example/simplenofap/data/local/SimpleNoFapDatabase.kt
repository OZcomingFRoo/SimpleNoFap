package com.example.simplenofap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ScheduledNotificationEntity::class,
        DayStreakRewardEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class SimpleNoFapDatabase : RoomDatabase() {
    abstract fun scheduledNotificationDao(): ScheduledNotificationDao
    abstract fun dayStreakRewardDao(): DayStreakRewardDao

    companion object {
        private const val DatabaseName = "simple_no_fap.db"

        @Volatile
        private var instance: SimpleNoFapDatabase? = null

        fun getInstance(context: Context): SimpleNoFapDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SimpleNoFapDatabase::class.java,
                    DatabaseName
                ).addMigrations(Migration1To2, Migration2To3).build().also { instance = it }
            }
        }

        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scheduled_notifications ADD COLUMN soundEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE scheduled_notifications ADD COLUMN notificationSoundUri TEXT")
                db.execSQL("ALTER TABLE scheduled_notifications ADD COLUMN notificationSoundDisplayName TEXT")
            }
        }

        val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM day_streak_rewards WHERE streakType IN ('TwoWeeks', 'ThreeMonths')")
                db.execSQL(
                    """
                    DELETE FROM day_streak_rewards
                    WHERE sourceStreakStartAtEpochMillis IS NOT NULL
                    AND id NOT IN (
                        SELECT MIN(id)
                        FROM day_streak_rewards
                        GROUP BY streakType, sourceStreakStartAtEpochMillis
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_day_streak_rewards_streakType_sourceStreakStartAtEpochMillis` " +
                        "ON `day_streak_rewards` (`streakType`, `sourceStreakStartAtEpochMillis`)"
                )
            }
        }
    }
}

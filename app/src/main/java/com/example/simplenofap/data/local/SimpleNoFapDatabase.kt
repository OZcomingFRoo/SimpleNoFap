package com.example.simplenofap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScheduledNotificationEntity::class,
        DayStreakRewardEntity::class
    ],
    version = 1,
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
                ).build().also { instance = it }
            }
        }
    }
}

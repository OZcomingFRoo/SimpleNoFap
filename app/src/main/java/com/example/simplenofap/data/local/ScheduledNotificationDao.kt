package com.example.simplenofap.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledNotificationDao {
    @Query("SELECT * FROM scheduled_notifications ORDER BY active DESC, daysOfWeekMask ASC, timeMinutesOfDay ASC")
    fun observeAll(): Flow<List<ScheduledNotificationEntity>>

    @Query("SELECT * FROM scheduled_notifications WHERE active = 1 ORDER BY daysOfWeekMask ASC, timeMinutesOfDay ASC")
    fun observeActive(): Flow<List<ScheduledNotificationEntity>>

    @Query("SELECT * FROM scheduled_notifications WHERE id = :id")
    suspend fun getById(id: Long): ScheduledNotificationEntity?

    @Insert
    suspend fun insert(notification: ScheduledNotificationEntity): Long

    @Update
    suspend fun update(notification: ScheduledNotificationEntity)

    @Delete
    suspend fun delete(notification: ScheduledNotificationEntity)

    @Query("DELETE FROM scheduled_notifications WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("UPDATE scheduled_notifications SET active = :active, updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean, updatedAtEpochMillis: Long): Int
}

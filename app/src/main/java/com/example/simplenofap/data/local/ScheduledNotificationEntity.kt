package com.example.simplenofap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_notifications")
data class ScheduledNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val daysOfWeekMask: Int,
    val timeMinutesOfDay: Int,
    val messagePresetKey: String?,
    val customMessage: String?,
    val titlePresetKey: String?,
    val customTitle: String?,
    val active: Boolean,
    val soundEnabled: Boolean = true,
    val notificationSoundUri: String? = null,
    val notificationSoundDisplayName: String? = null
)

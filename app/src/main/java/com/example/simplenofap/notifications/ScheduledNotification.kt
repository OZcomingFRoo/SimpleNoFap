package com.example.simplenofap.notifications

data class ScheduledNotification(
    val id: Long = 0,
    val createdAtEpochMillis: Long = 0,
    val updatedAtEpochMillis: Long = 0,
    val daysOfWeekMask: Int,
    val timeMinutesOfDay: Int,
    val messagePresetKey: String?,
    val customMessage: String?,
    val titlePresetKey: String?,
    val customTitle: String?,
    val active: Boolean
)

data class ScheduledNotificationInput(
    val daysOfWeekMask: Int,
    val timeMinutesOfDay: Int,
    val messagePresetKey: String? = null,
    val customMessage: String? = null,
    val titlePresetKey: String? = null,
    val customTitle: String? = null,
    val active: Boolean = true
)

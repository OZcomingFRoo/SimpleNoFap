package com.example.simplenofap.notifications

import com.example.simplenofap.data.local.ScheduledNotificationDao
import com.example.simplenofap.data.local.ScheduledNotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(
    private val dao: ScheduledNotificationDao,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    fun observeScheduledNotifications(): Flow<List<ScheduledNotification>> {
        return dao.observeAll().map { notifications -> notifications.map { it.toDomain() } }
    }

    fun observeActiveScheduledNotifications(): Flow<List<ScheduledNotification>> {
        return dao.observeActive().map { notifications -> notifications.map { it.toDomain() } }
    }

    suspend fun getScheduledNotification(id: Long): ScheduledNotification? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun createScheduledNotification(input: ScheduledNotificationInput): Long {
        ScheduledNotificationValidator.validate(input.daysOfWeekMask, input.timeMinutesOfDay)
        val now = currentTimeMillis()
        return dao.insert(
            ScheduledNotificationEntity(
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                daysOfWeekMask = input.daysOfWeekMask,
                timeMinutesOfDay = input.timeMinutesOfDay,
                messagePresetKey = input.messagePresetKey,
                customMessage = input.customMessage,
                titlePresetKey = input.titlePresetKey,
                customTitle = input.customTitle,
                active = input.active
            )
        )
    }

    suspend fun updateScheduledNotification(notification: ScheduledNotification) {
        ScheduledNotificationValidator.validate(
            notification.daysOfWeekMask,
            notification.timeMinutesOfDay
        )
        dao.update(
            notification.copy(updatedAtEpochMillis = currentTimeMillis()).toEntity()
        )
    }

    suspend fun setScheduledNotificationActive(id: Long, active: Boolean): Boolean {
        return dao.setActive(id, active, currentTimeMillis()) > 0
    }

    suspend fun deleteScheduledNotification(id: Long): Boolean {
        return dao.deleteById(id) > 0
    }
}

private fun ScheduledNotificationEntity.toDomain(): ScheduledNotification {
    return ScheduledNotification(
        id = id,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        daysOfWeekMask = daysOfWeekMask,
        timeMinutesOfDay = timeMinutesOfDay,
        messagePresetKey = messagePresetKey,
        customMessage = customMessage,
        titlePresetKey = titlePresetKey,
        customTitle = customTitle,
        active = active
    )
}

private fun ScheduledNotification.toEntity(): ScheduledNotificationEntity {
    return ScheduledNotificationEntity(
        id = id,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        daysOfWeekMask = daysOfWeekMask,
        timeMinutesOfDay = timeMinutesOfDay,
        messagePresetKey = messagePresetKey,
        customMessage = customMessage,
        titlePresetKey = titlePresetKey,
        customTitle = customTitle,
        active = active
    )
}

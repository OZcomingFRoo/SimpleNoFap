package com.example.simplenofap.notifications

interface NotificationScheduler {
    val canScheduleExactAlarms: Boolean
    val canPostNotifications: Boolean
    fun schedule(notification: ScheduledNotification)
    fun cancel(notificationId: Long)
    suspend fun reconcile(repository: NotificationRepository)
}

class NotificationCoordinator(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) {
    val notifications = repository.observeScheduledNotifications()

    suspend fun save(original: ScheduledNotification?, input: ScheduledNotificationInput): Long {
        val id = if (original == null) {
            repository.createScheduledNotification(input)
        } else {
            repository.updateScheduledNotification(
                original.copy(
                    daysOfWeekMask = input.daysOfWeekMask,
                    timeMinutesOfDay = input.timeMinutesOfDay,
                    messagePresetKey = input.messagePresetKey,
                    customMessage = input.customMessage,
                    active = input.active,
                    soundEnabled = input.soundEnabled,
                    notificationSoundUri = input.notificationSoundUri,
                    notificationSoundDisplayName = input.notificationSoundDisplayName
                )
            )
            original.id
        }
        scheduler.cancel(id)
        repository.getScheduledNotification(id)?.takeIf { it.active }?.let(scheduler::schedule)
        return id
    }

    suspend fun setActive(id: Long, active: Boolean): Boolean {
        val changed = repository.setScheduledNotificationActive(id, active)
        if (changed) {
            scheduler.cancel(id)
            if (active) repository.getScheduledNotification(id)?.let(scheduler::schedule)
        }
        return changed
    }

    suspend fun delete(id: Long): Boolean {
        val deleted = repository.deleteScheduledNotification(id)
        if (deleted) scheduler.cancel(id)
        return deleted
    }

    suspend fun reconcile() = scheduler.reconcile(repository)
}

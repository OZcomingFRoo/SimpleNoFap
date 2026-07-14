package com.example.simplenofap.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenofap.notifications.NotificationCoordinator
import com.example.simplenofap.notifications.NotificationScheduler
import com.example.simplenofap.notifications.NotificationWeekday
import com.example.simplenofap.notifications.ScheduledNotification
import com.example.simplenofap.notifications.ScheduledNotificationInput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

data class NotificationDraft(
    val original: ScheduledNotification? = null,
    val hour: Int,
    val minute: Int,
    val initialHour: Int = hour,
    val initialMinute: Int = minute,
    val daysMask: Int = 127,
    val message: String = "",
    val active: Boolean = true,
    val soundEnabled: Boolean = true,
    val soundUri: String? = null,
    val soundDisplayName: String? = null,
    val showWeekdayError: Boolean = false
) {
    val changed: Boolean get() = original?.let {
        hour * 60 + minute != it.timeMinutesOfDay || daysMask != it.daysOfWeekMask ||
            message.trim() != it.customMessage.orEmpty() || active != it.active ||
            soundEnabled != it.soundEnabled || soundUri != it.notificationSoundUri
    } ?: (hour != initialHour || minute != initialMinute || daysMask != 127 ||
        message.isNotBlank() || !active || !soundEnabled || soundUri != null)
}

sealed interface NotificationsEvent {
    data object RequestNotificationPermission : NotificationsEvent
    data object OpenExactAlarmSettings : NotificationsEvent
}

class NotificationsViewModel(
    private val coordinator: NotificationCoordinator,
    private val scheduler: NotificationScheduler
) : ViewModel() {
    val notifications: StateFlow<List<ScheduledNotification>> = coordinator.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val draft = MutableStateFlow<NotificationDraft?>(null)
    val error = MutableStateFlow<String?>(null)
    val notificationPermissionGranted = MutableStateFlow(scheduler.canPostNotifications)
    val exactAlarmsGranted = MutableStateFlow(scheduler.canScheduleExactAlarms)
    private val _events = MutableSharedFlow<NotificationsEvent>()
    val events = _events.asSharedFlow()

    init { viewModelScope.launch { coordinator.reconcile() } }

    fun newDraft() {
        val now = LocalTime.now()
        draft.value = NotificationDraft(hour = now.hour, minute = now.minute)
    }

    fun edit(notification: ScheduledNotification) {
        draft.value = NotificationDraft(
            original = notification,
            hour = notification.timeMinutesOfDay / 60,
            minute = notification.timeMinutesOfDay % 60,
            daysMask = notification.daysOfWeekMask,
            message = notification.customMessage.orEmpty(),
            active = notification.active,
            soundEnabled = notification.soundEnabled,
            soundUri = notification.notificationSoundUri,
            soundDisplayName = notification.notificationSoundDisplayName
        )
    }

    fun updateDraft(transform: (NotificationDraft) -> NotificationDraft) {
        draft.value = draft.value?.let(transform)
    }

    fun toggleDay(day: NotificationWeekday) = updateDraft {
        it.copy(daysMask = it.daysMask xor day.maskBit, showWeekdayError = false)
    }

    fun closeEditor() { draft.value = null }

    fun save() {
        val value = draft.value ?: return
        if (value.daysMask == 0) {
            draft.value = value.copy(showWeekdayError = true)
            return
        }
        viewModelScope.launch {
            runCatching {
                coordinator.save(
                    value.original,
                    ScheduledNotificationInput(
                        daysOfWeekMask = value.daysMask,
                        timeMinutesOfDay = value.hour * 60 + value.minute,
                        messagePresetKey = if (value.message.trim() == value.original?.customMessage.orEmpty()) value.original?.messagePresetKey else null,
                        customMessage = value.message.trim().ifBlank { null },
                        titlePresetKey = value.original?.titlePresetKey,
                        customTitle = value.original?.customTitle,
                        active = value.active,
                        soundEnabled = value.soundEnabled,
                        notificationSoundUri = value.soundUri,
                        notificationSoundDisplayName = value.soundDisplayName
                    )
                )
            }.onSuccess {
                draft.value = null
                if (value.active && !scheduler.canPostNotifications) _events.emit(NotificationsEvent.RequestNotificationPermission)
            }.onFailure { error.value = it.message ?: "notification_error" }
        }
    }

    fun setActive(id: Long, active: Boolean) = viewModelScope.launch {
        runCatching { coordinator.setActive(id, active) }
            .onSuccess { if (active && !scheduler.canPostNotifications) _events.emit(NotificationsEvent.RequestNotificationPermission) }
            .onFailure { error.value = it.message ?: "notification_error" }
    }

    fun delete() {
        val id = draft.value?.original?.id ?: return
        viewModelScope.launch {
            runCatching { coordinator.delete(id) }
                .onSuccess { draft.value = null }
                .onFailure { error.value = it.message ?: "notification_error" }
        }
    }

    fun refreshPermissions() {
        notificationPermissionGranted.value = scheduler.canPostNotifications
        exactAlarmsGranted.value = scheduler.canScheduleExactAlarms
        if (scheduler.canPostNotifications) viewModelScope.launch { coordinator.reconcile() }
    }

    fun requestExactAlarmAccess() = viewModelScope.launch { _events.emit(NotificationsEvent.OpenExactAlarmSettings) }
    fun dismissError() { error.value = null }

    class Factory(
        private val coordinator: NotificationCoordinator,
        private val scheduler: NotificationScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationsViewModel(coordinator, scheduler) as T
    }
}

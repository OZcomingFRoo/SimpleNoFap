package com.example.simplenofap.notifications

import com.example.simplenofap.data.local.ScheduledNotificationDao
import com.example.simplenofap.data.local.ScheduledNotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRepositoryTest {
    @Test fun create_mapsSoundFieldsAndTrimsNothingAtRepositoryBoundary() = runBlocking {
        val dao = FakeNotificationDao()
        val repository = NotificationRepository(dao) { 100 }
        val id = repository.createScheduledNotification(
            ScheduledNotificationInput(127, 300, customMessage = "hello", soundEnabled = true,
                notificationSoundUri = "content://tone", notificationSoundDisplayName = "Tone")
        )
        val result = repository.getScheduledNotification(id)!!
        assertEquals("content://tone", result.notificationSoundUri)
        assertEquals("Tone", result.notificationSoundDisplayName)
        assertTrue(result.soundEnabled)
    }

    @Test fun defaults_useEnabledSystemSound() = runBlocking {
        val repository = NotificationRepository(FakeNotificationDao())
        val id = repository.createScheduledNotification(ScheduledNotificationInput(1, 0))
        val result = repository.getScheduledNotification(id)!!
        assertTrue(result.soundEnabled)
        assertNull(result.notificationSoundUri)
    }

    @Test fun listIsChronologicalAndActiveRetrievalFiltersDisabledRows() = runBlocking {
        val repository = NotificationRepository(FakeNotificationDao())
        repository.createScheduledNotification(ScheduledNotificationInput(1, 800, active = true))
        repository.createScheduledNotification(ScheduledNotificationInput(1, 100, active = false))
        repository.createScheduledNotification(ScheduledNotificationInput(1, 400, active = true))
        assertEquals(listOf(100, 400, 800), repository.observeScheduledNotifications().first().map { it.timeMinutesOfDay })
        assertEquals(listOf(400, 800), repository.getActiveScheduledNotifications().map { it.timeMinutesOfDay })
    }
}

private class FakeNotificationDao : ScheduledNotificationDao {
    private val rows = MutableStateFlow<List<ScheduledNotificationEntity>>(emptyList())
    private var nextId = 1L
    private fun sorted(activeOnly: Boolean = false) = rows.value.filter { !activeOnly || it.active }
        .sortedWith(compareBy<ScheduledNotificationEntity> { it.timeMinutesOfDay }.thenBy { it.id })
    override fun observeAll(): Flow<List<ScheduledNotificationEntity>> = rows.map { sorted() }
    override fun observeActive(): Flow<List<ScheduledNotificationEntity>> = rows.map { sorted(true) }
    override suspend fun getActive(): List<ScheduledNotificationEntity> = sorted(true)
    override suspend fun getById(id: Long) = rows.value.firstOrNull { it.id == id }
    override suspend fun insert(notification: ScheduledNotificationEntity): Long {
        val id = if (notification.id == 0L) nextId++ else notification.id
        rows.value += notification.copy(id = id); return id
    }
    override suspend fun update(notification: ScheduledNotificationEntity) {
        rows.value = rows.value.map { if (it.id == notification.id) notification else it }
    }
    override suspend fun delete(notification: ScheduledNotificationEntity) { deleteById(notification.id) }
    override suspend fun deleteById(id: Long): Int {
        val before = rows.value.size; rows.value = rows.value.filterNot { it.id == id }; return before - rows.value.size
    }
    override suspend fun setActive(id: Long, active: Boolean, updatedAtEpochMillis: Long): Int {
        var changed = 0
        rows.value = rows.value.map { if (it.id == id) { changed = 1; it.copy(active = active, updatedAtEpochMillis = updatedAtEpochMillis) } else it }
        return changed
    }
}

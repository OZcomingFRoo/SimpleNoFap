package com.example.simplenofap.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import java.time.ZonedDateTime

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    override val canScheduleExactAlarms: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    override val canPostNotifications: Boolean
        get() = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    override fun schedule(notification: ScheduledNotification) {
        if (!notification.active || !canPostNotifications) return
        createChannel(notification, fullScreenEnabled = false)
        val triggerAt = NotificationScheduleCalculator.nextOccurrence(
            notification, ZonedDateTime.now()
        ).toInstant().toEpochMilli()
        val operation = checkNotNull(alarmPendingIntent(notification.id, PendingIntent.FLAG_UPDATE_CURRENT))
        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    override fun cancel(notificationId: Long) {
        alarmPendingIntent(notificationId, PendingIntent.FLAG_NO_CREATE)?.let(alarmManager::cancel)
        notificationManager.cancel(notificationId.toStableInt())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notificationChannels
                .filter { it.id.startsWith("reminder_${notificationId}_") }
                .forEach { notificationManager.deleteNotificationChannel(it.id) }
        }
    }

    override suspend fun reconcile(repository: NotificationRepository) {
        if (!canPostNotifications) return
        repository.getActiveScheduledNotifications().forEach { schedule(it) }
    }

    fun channelId(notification: ScheduledNotification, fullScreenEnabled: Boolean = false): String {
        val soundKey = if (!notification.soundEnabled) "silent" else
            (notification.notificationSoundUri ?: "default").hashCode().toUInt().toString(16)
        val behaviorKey = if (fullScreenEnabled) "fullscreen" else "normal"
        return "reminder_${notification.id}_${behaviorKey}_$soundKey"
    }

    fun createChannel(notification: ScheduledNotification, fullScreenEnabled: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channelId = channelId(notification, fullScreenEnabled)
        val behaviorPrefix = "reminder_${notification.id}_${if (fullScreenEnabled) "fullscreen" else "normal"}_"
        notificationManager.notificationChannels
            .filter { it.id.startsWith(behaviorPrefix) && it.id != channelId }
            .forEach { notificationManager.deleteNotificationChannel(it.id) }
        val channel = NotificationChannel(
            channelId,
            notification.notificationSoundDisplayName ?: context.getString(com.example.simplenofap.R.string.app_name),
            if (fullScreenEnabled) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
        )
        if (!notification.soundEnabled) {
            channel.setSound(null, null)
        } else {
            val sound = resolveSound(notification.notificationSoundUri)
            channel.setSound(sound, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun resolveSound(saved: String?): Uri = try {
        saved?.let(Uri::parse)?.also {
            context.contentResolver.openAssetFileDescriptor(it, "r")?.close()
        } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    } catch (_: Exception) {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    private fun alarmPendingIntent(id: Long, lookupFlag: Int): PendingIntent? {
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
            .putExtra(NotificationAlarmReceiver.ExtraNotificationId, id)
        return PendingIntent.getBroadcast(
            context, id.toStableInt(), intent,
            lookupFlag or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

internal fun Long.toStableInt(): Int = (this xor (this ushr 32)).toInt()

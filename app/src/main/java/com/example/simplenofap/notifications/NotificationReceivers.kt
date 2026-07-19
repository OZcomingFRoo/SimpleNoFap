package com.example.simplenofap.notifications

import android.app.PendingIntent
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.simplenofap.R
import com.example.simplenofap.data.local.SimpleNoFapDatabase
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ExtraNotificationId, 0)
        if (id <= 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = context.notificationRepository()
                val notification = repository.getScheduledNotification(id) ?: return@launch
                if (!notification.active) return@launch
                val settings = SettingsRepository(context).settings.first()
                val strings = stringsFor(resolveLanguagePreference(settings.languagePreference))
                val scheduler = AndroidNotificationScheduler(context)
                val title = notification.customTitle ?: strings.appName
                val body = notification.customMessage ?: strings.notificationDefaultBody
                val presentation = resolveReminderPresentation(
                    fullScreenEnabled = settings.fullScreenReminderNotificationsEnabled,
                    canUseFullScreenIntent = context.canUseFullScreenReminderIntent()
                )
                val highPriority = presentation != ReminderPresentation.Normal
                scheduler.createChannel(notification, fullScreenEnabled = highPriority)
                val contentIntent = PendingIntent.getActivity(
                    context,
                    id.toStableInt(),
                    Intent(context, com.example.simplenofap.MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val builder = NotificationCompat.Builder(
                    context,
                    scheduler.channelId(notification, fullScreenEnabled = highPriority)
                )
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(
                        if (highPriority) {
                            NotificationCompat.PRIORITY_HIGH
                        } else {
                            NotificationCompat.PRIORITY_DEFAULT
                        }
                    )
                if (presentation == ReminderPresentation.FullScreen) {
                    builder.setFullScreenIntent(
                        PendingIntent.getActivity(
                            context,
                            id.toStableInt(),
                            Intent(context, ReminderAlertActivity::class.java)
                                .putExtra(ReminderAlertActivity.ExtraTitle, title)
                                .putExtra(ReminderAlertActivity.ExtraBody, body),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        ),
                        true
                    )
                } else if (presentation == ReminderPresentation.HeadsUpFallback) {
                    builder.addAction(
                        R.mipmap.ic_launcher,
                        strings.openAndroidSettings,
                        PendingIntent.getActivity(
                            context,
                            id.toStableInt() xor FullScreenSettingsRequestCodeSalt,
                            context.fullScreenReminderSettingsIntent(),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
                if (!notification.soundEnabled) builder.setSilent(true)
                else if (android.os.Build.VERSION.SDK_INT < 26) builder.setSound(resolveSound(context, notification.notificationSoundUri))
                context.getSystemService(NotificationManager::class.java)
                    .notify(id.toStableInt(), builder.build())
                scheduler.schedule(notification)
            } finally {
                pending.finish()
            }
        }
    }

    companion object { const val ExtraNotificationId = "scheduled_notification_id" }
}

class NotificationReconciliationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AndroidNotificationScheduler(context).reconcile(context.notificationRepository())
            } finally { pending.finish() }
        }
    }
}

internal fun Context.notificationRepository(): NotificationRepository = NotificationRepository(
    SimpleNoFapDatabase.getInstance(this).scheduledNotificationDao()
)

private fun resolveSound(context: Context, value: String?): Uri = try {
    value?.let(Uri::parse)?.also { context.contentResolver.openAssetFileDescriptor(it, "r")?.close() }
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
} catch (_: Exception) { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) }

private const val FullScreenSettingsRequestCodeSalt = 0x5F51

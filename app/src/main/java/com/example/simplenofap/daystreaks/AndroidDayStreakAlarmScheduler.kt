package com.example.simplenofap.daystreaks

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.simplenofap.MainActivity
import com.example.simplenofap.R
import com.example.simplenofap.data.local.SimpleNoFapDatabase
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AndroidDayStreakAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    val canScheduleExactAlarms: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    suspend fun reconcile(settingsRepository: SettingsRepository = SettingsRepository(context)) {
        val settings = settingsRepository.currentSettings()
        val startedAt = settings.startNoFapAtEpochMillis
        val attemptId = settings.dayStreakAttemptId
        if (startedAt == null || attemptId == null) {
            cancel()
            return
        }
        val next = DayStreakMilestones.nextUnprocessedFutureMilestone(
            startedAtEpochMillis = startedAt,
            nowEpochMillis = System.currentTimeMillis(),
            processedMask = settings.processedDayStreakMilestonesMask
        )
        if (next == null) {
            cancel()
        } else {
            schedule(next, attemptId)
        }
    }

    fun schedule(next: DayStreakNextMilestone, attemptId: Long) {
        val operation = alarmPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT, attemptId, next.type)
        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.triggerAtEpochMillis,
                operation
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.triggerAtEpochMillis,
                operation
            )
        }
    }

    fun cancel() {
        val operation = lookupPendingIntent() ?: return
        alarmManager.cancel(operation)
    }

    private fun lookupPendingIntent(): PendingIntent? {
        val intent = Intent(context, DayStreakAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            RequestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun alarmPendingIntent(
        flags: Int,
        attemptId: Long,
        type: DayStreakType
    ): PendingIntent {
        val intent = Intent(context, DayStreakAlarmReceiver::class.java)
            .putExtra(DayStreakAlarmReceiver.ExtraAttemptId, attemptId)
            .putExtra(DayStreakAlarmReceiver.ExtraMilestoneType, type.name)
        return PendingIntent.getBroadcast(
            context,
            RequestCode,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val RequestCode = 73_001
    }
}

class DayStreakAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val expectedAttemptId = intent.getLongExtra(ExtraAttemptId, 0L).takeIf { it > 0L }
        this.launchDayStreakReconciliation(context, expectedAttemptId, notifyOnGrant = true)
    }

    companion object {
        const val ExtraAttemptId = "day_streak_attempt_id"
        const val ExtraMilestoneType = "day_streak_milestone_type"
    }
}

class DayStreakSystemReconciliationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        this.launchDayStreakReconciliation(context, expectedAttemptId = null, notifyOnGrant = true)
    }
}

private fun BroadcastReceiver.launchDayStreakReconciliation(
    context: Context,
    expectedAttemptId: Long?,
    notifyOnGrant: Boolean
) {
    val pending = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val settingsRepository = SettingsRepository(context)
            val rewardRepository = context.dayStreakRepository()
            val result = DayStreakReconciler(settingsRepository, rewardRepository).reconcile(
                expectedAttemptId = expectedAttemptId
            )
            if (notifyOnGrant && result is DayStreakReconciliationResult.Granted && result.rewardId != null) {
                showDayStreakNotification(context, result.type)
            }
            AndroidDayStreakAlarmScheduler(context).reconcile(settingsRepository)
        } finally {
            pending.finish()
        }
    }
}

fun Context.dayStreakRepository(): DayStreakRepository {
    return DayStreakRepository(SimpleNoFapDatabase.getInstance(this).dayStreakRewardDao())
}

private fun showDayStreakNotification(context: Context, type: DayStreakType) {
    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                DayStreakChannelId,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
    val settings = kotlinx.coroutines.runBlocking { SettingsRepository(context).currentSettings() }
    val strings = stringsFor(resolveLanguagePreference(settings.languagePreference))
    val text = strings.dayStreakNotificationText(type)
    val contentIntent = PendingIntent.getActivity(
        context,
        DayStreakNotificationRequestCode,
        Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_OPEN_DAY_STREAKS, true)
            .putExtra(MainActivity.EXTRA_DAY_STREAK_HIGHLIGHT_TYPE, type.name),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notification = NotificationCompat.Builder(context, DayStreakChannelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(text.title)
        .setContentText(text.body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text.body))
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    notificationManager.notify(type.name.hashCode(), notification)
}

private const val DayStreakChannelId = "day_streak_achievements"
private const val DayStreakNotificationRequestCode = 73_002

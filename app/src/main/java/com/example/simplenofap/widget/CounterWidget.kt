package com.example.simplenofap.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.simplenofap.MainActivity
import com.example.simplenofap.R
import com.example.simplenofap.counter.CounterPresentation
import com.example.simplenofap.counter.CounterVisualTier
import com.example.simplenofap.counter.calculateCounter
import com.example.simplenofap.localization.ResolvedLanguage
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.SettingsRepository
import com.example.simplenofap.settings.ThemePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val ACTION_REFRESH_COUNTER_WIDGET =
    "com.example.simplenofap.action.REFRESH_COUNTER_WIDGET"
const val EXTRA_OPEN_COUNTER = "open_counter"

private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class CounterWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CounterWidgetUpdater.refresh(context, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        CounterWidgetUpdater.refreshAll(context)
    }

    override fun onDisabled(context: Context) {
        CounterWidgetUpdater.cancelScheduledRefresh(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        CounterWidgetUpdater.refresh(context, intArrayOf(appWidgetId))
    }
}

class CounterWidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REFRESH_COUNTER_WIDGET) return
        val pendingResult = goAsync()
        widgetScope.launch {
            try {
                CounterWidgetUpdater.refreshAllNow(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class CounterWidgetSystemReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        widgetScope.launch {
            try {
                CounterWidgetUpdater.refreshAllNow(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

object CounterWidgetUpdater {
    fun refreshAll(context: Context) {
        refresh(context, allWidgetIds(context))
    }

    fun refresh(context: Context, widgetIds: IntArray) {
        val applicationContext = context.applicationContext
        widgetScope.launch {
            refreshNow(applicationContext, widgetIds)
        }
    }

    suspend fun refreshAllNow(context: Context) {
        refreshNow(context.applicationContext, allWidgetIds(context))
    }

    private suspend fun refreshNow(context: Context, widgetIds: IntArray) {
        val repository = SettingsRepository(context)
        val startedAt = repository.initializeStartTimeIfAbsent()
        val settings = repository.settings.first()
        if (widgetIds.isEmpty()) {
            cancelScheduledRefresh(context)
            return
        }
        val language = resolveLanguagePreference(settings.languagePreference)
        val strings = stringsFor(language)
        val now = System.currentTimeMillis()
        val presentation = calculateCounter(startedAt, now, strings.counterUnitLabels)
        val manager = AppWidgetManager.getInstance(context)

        widgetIds.forEach { appWidgetId ->
            manager.updateAppWidget(
                appWidgetId,
                buildRemoteViews(context, presentation, language, settings.themePreference)
            )
        }
        scheduleNextRefresh(context, presentation, now)
    }

    internal fun buildRemoteViews(
        context: Context,
        presentation: CounterPresentation,
        language: ResolvedLanguage,
        themePreference: ThemePreference
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_counter)
        val chronometerState = calculateWidgetChronometerState(
            presentation = presentation,
            elapsedRealtimeMillis = SystemClock.elapsedRealtime(),
            nowEpochMillis = System.currentTimeMillis()
        )
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_COUNTER, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val clickIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val systemDark = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val dark = when (themePreference) {
            ThemePreference.Light -> false
            ThemePreference.Dark -> true
            ThemePreference.System -> systemDark
        }
        val strings = stringsFor(language)

        views.setTextViewText(R.id.widget_label, strings.myStreak)
        views.setTextViewText(
            R.id.widget_duration_summary,
            presentation.durationSummaryText
        )
        views.setViewVisibility(
            R.id.widget_duration_summary,
            if (presentation.usesStackedLayout) View.VISIBLE else View.GONE
        )
        views.setChronometer(
            R.id.widget_chronometer,
            chronometerState.baseElapsedRealtimeMillis,
            chronometerState.format,
            true
        )
        views.setTextViewTextSize(
            R.id.widget_chronometer,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            if (presentation.usesStackedLayout) 34f else 36f
        )
        views.setInt(
            R.id.widget_root,
            "setBackgroundResource",
            presentation.visualTier.widgetBackground(dark)
        )
        views.setTextColor(
            R.id.widget_label,
            presentation.visualTier.widgetTextColor(dark, secondary = true)
        )
        views.setTextColor(
            R.id.widget_chronometer,
            presentation.visualTier.widgetTextColor(dark, secondary = false)
        )
        views.setTextColor(
            R.id.widget_duration_summary,
            presentation.visualTier.widgetTextColor(dark, secondary = false)
        )
        views.setOnClickPendingIntent(R.id.widget_root, clickIntent)
        views.setContentDescription(
            R.id.widget_root,
            "${strings.myStreak}: ${presentation.formattedText}"
        )
        return views
    }

    fun cancelScheduledRefresh(context: Context) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(refreshPendingIntent(context.applicationContext))
    }

    private fun scheduleNextRefresh(
        context: Context,
        presentation: CounterPresentation,
        nowEpochMillis: Long
    ) {
        val triggerAt = calculateWidgetChronometerState(
            presentation = presentation,
            elapsedRealtimeMillis = 0L,
            nowEpochMillis = nowEpochMillis
        ).nextRefreshEpochMillis

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = refreshPendingIntent(context)
        alarmManager.cancel(pendingIntent)
        if (triggerAt == null) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, CounterWidgetRefreshReceiver::class.java).apply {
            action = ACTION_REFRESH_COUNTER_WIDGET
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun allWidgetIds(context: Context): IntArray {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, CounterWidgetProvider::class.java)
        )
    }
}

private fun CounterVisualTier.widgetBackground(dark: Boolean): Int = when (this) {
    CounterVisualTier.Neutral -> if (dark) {
        R.drawable.widget_counter_neutral_dark
    } else {
        R.drawable.widget_counter_neutral_light
    }
    CounterVisualTier.Warm -> if (dark) {
        R.drawable.widget_counter_warm_dark
    } else {
        R.drawable.widget_counter_warm_light
    }
    CounterVisualTier.Forest -> R.drawable.widget_counter_forest
    CounterVisualTier.BlueSoft -> R.drawable.widget_counter_blue_soft
    CounterVisualTier.BlueMedium -> R.drawable.widget_counter_blue_medium
    CounterVisualTier.BlueStrong -> R.drawable.widget_counter_blue_strong
    CounterVisualTier.BlueSolid -> R.drawable.widget_counter_blue_solid
    CounterVisualTier.Rainbow -> R.drawable.widget_counter_rainbow
}

private fun CounterVisualTier.widgetTextColor(dark: Boolean, secondary: Boolean): Int {
    val alpha = if (secondary) 0xD9 else 0xFF
    val rgb = when (this) {
        CounterVisualTier.Neutral -> if (dark) 0xE5EEF2 else 0x24343D
        CounterVisualTier.Warm -> 0x2E2300
        else -> 0xFFFFFF
    }
    return (alpha shl 24) or rgb
}

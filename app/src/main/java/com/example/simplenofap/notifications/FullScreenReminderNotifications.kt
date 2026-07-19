package com.example.simplenofap.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

internal enum class ReminderPresentation {
    Normal,
    FullScreen,
    HeadsUpFallback
}

internal fun resolveReminderPresentation(
    fullScreenEnabled: Boolean,
    canUseFullScreenIntent: Boolean
): ReminderPresentation {
    return when {
        !fullScreenEnabled -> ReminderPresentation.Normal
        canUseFullScreenIntent -> ReminderPresentation.FullScreen
        else -> ReminderPresentation.HeadsUpFallback
    }
}

internal fun Context.canUseFullScreenReminderIntent(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
}

internal fun Context.fullScreenReminderSettingsIntent(): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
            data = Uri.parse("package:$packageName")
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
    }
}

package com.example.simplenofap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.AppSettings
import com.example.simplenofap.settings.SettingsRepository
import com.example.simplenofap.settings.ThemePreference
import com.example.simplenofap.ui.SimpleNoFapApp
import com.example.simplenofap.ui.theme.SimpleNoFapTheme
import com.example.simplenofap.widget.CounterWidgetUpdater
import com.example.simplenofap.widget.EXTRA_OPEN_COUNTER
import kotlinx.coroutines.launch
import com.example.simplenofap.notifications.AndroidNotificationScheduler
import com.example.simplenofap.notifications.notificationRepository

class MainActivity : ComponentActivity() {
    private var openCounterRequest by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            SettingsRepository(applicationContext).initializeStartTimeIfAbsent()
            AndroidNotificationScheduler(applicationContext)
                .reconcile(applicationContext.notificationRepository())
            CounterWidgetUpdater.refreshAll(applicationContext)
        }
        handleIntent(intent)

        setContent {
            val settingsRepository = remember {
                SettingsRepository(applicationContext)
            }
            val scope = rememberCoroutineScope()
            val settings by settingsRepository.settings.collectAsState(
                initial = AppSettings()
            )
            val resolvedLanguage = resolveLanguagePreference(settings.languagePreference)
            val strings = stringsFor(resolvedLanguage)
            val darkTheme = shouldUseDarkTheme(
                themePreference = settings.themePreference,
                systemDarkTheme = isSystemInDarkTheme()
            )

            SimpleNoFapTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(
                    LocalAppStrings provides strings,
                    LocalLayoutDirection provides resolvedLanguage.layoutDirection
                ) {
                    SimpleNoFapApp(
                        userName = settings.userName,
                        startedAtEpochMillis = settings.startNoFapAtEpochMillis,
                        languagePreference = settings.languagePreference,
                        themePreference = settings.themePreference,
                        openCounterRequest = openCounterRequest,
                        onUserNameSaved = { userName ->
                            scope.launch {
                                settingsRepository.setUserName(userName)
                            }
                        },
                        onStartTimeChanged = { startedAtEpochMillis ->
                            scope.launch {
                                settingsRepository.setStartNoFapAtEpochMillis(startedAtEpochMillis)
                                CounterWidgetUpdater.refreshAll(applicationContext)
                            }
                        },
                        onResetToNow = {
                            scope.launch {
                                settingsRepository.resetStartNoFapToNow()
                                CounterWidgetUpdater.refreshAll(applicationContext)
                            }
                        },
                        onLanguagePreferenceChanged = { languagePreference ->
                            scope.launch {
                                settingsRepository.setLanguagePreference(languagePreference)
                            }
                        },
                        onThemePreferenceChanged = { themePreference ->
                            scope.launch {
                                settingsRepository.setThemePreference(themePreference)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        CounterWidgetUpdater.refreshAll(applicationContext)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_COUNTER, false) == true) {
            openCounterRequest++
            intent.removeExtra(EXTRA_OPEN_COUNTER)
        }
    }
}

private fun shouldUseDarkTheme(
    themePreference: ThemePreference,
    systemDarkTheme: Boolean
): Boolean {
    return when (themePreference) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> systemDarkTheme
    }
}
